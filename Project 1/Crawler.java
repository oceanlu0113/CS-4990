package webCrawler;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class Crawler {

    private static final int MAX_PAGES = 20;
    private Set<String> pagesVisited = new HashSet<>();
    private List<String> links = new LinkedList<>();
    private Queue<String> qList = new LinkedList<>();
    private File csvFile = new File("report.csv");
    private ArrayList<String[]> data = new ArrayList<>();

    public Crawler(){}

    private void crawl(String url) throws IOException {

        Crawler crawl = new Crawler();

        while(this.pagesVisited.size() < MAX_PAGES){
            String currentUrl;
            if(this.qList.isEmpty()){
                currentUrl = url;
                this.pagesVisited.add(url);
            }
            else {
                currentUrl = this.getNextUrl();
            }
            crawl.connect(currentUrl);

            this.qList.addAll(crawl.getLinks());
        }
        crawl.toCSV();
    }

    private boolean connect(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            Document htmlDocument = connection.get();
            String html = Jsoup.connect(url).get().html();

            url = url.replace("http://", "").replace("https://","").replaceAll("/", "-") ;
            File repository = new File("repository");
            repository.mkdir();

            File htmlFile = new File(repository + "/" + url + ".html") ;
            FileOutputStream out = new FileOutputStream(htmlFile) ;
            try{
                BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(htmlFile));
                htmlWriter.write(html);
                htmlWriter.close();
            }catch (IOException e){
                e.printStackTrace();
            }

            Elements linksOnPage = htmlDocument.select("a[href]");
            int countLinks = linksOnPage.size();

            String[] temp = new String[2];
            temp[0] = url;
            temp[1] = "number of outlinks: " + Integer.toString(countLinks);
            data.add(temp);

            System.out.println(linksOnPage.size() + " links");

            for(Element link : linksOnPage){
                this.links.add(link.absUrl("href"));
            }

            return true;

        } catch(IOException ioe){
            return false;
        }
    }

    private String getNextUrl() {
        String nextUrl;
        nextUrl = this.qList.poll();
        if(this.pagesVisited.contains(nextUrl))
            nextUrl = this.qList.poll();
        this.pagesVisited.add(nextUrl);
        return nextUrl;
    }

    private List<String> getLinks(){
        return this.links;
    }

    private void toCSV() throws IOException{
        FileWriter outputFile = new FileWriter(csvFile);
        try(PrintWriter pw = new PrintWriter(outputFile)){
            data.stream().map(this::convert).forEach(pw::println);
        }
    }

    private String convert(String[] data){
        return Stream.of(data).collect(Collectors.joining(","));
    }

    public static void main(String[] args) throws IOException {
        Crawler test1 = new Crawler(), test2 = new Crawler(), test3 = new Crawler();
        test1.crawl("https://www.simpleweb.org");
        //test2.crawl("https://www.cpp.edu");
        //test3.crawl("https://www.apple.com");
    }

}
