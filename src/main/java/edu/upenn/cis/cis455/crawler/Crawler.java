package edu.upenn.cis.cis455.crawler;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import javax.net.ssl.HttpsURLConnection;

import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class Crawler implements CrawlMaster {
    final static Logger logger = LogManager.getLogger(Crawler.class);
    static final int NUM_WORKERS = 50;
    public Queue<CrawlTask> queue = new LinkedList<>();
    Integer count = 0;
    Integer max_count = 0;
    
    Boolean isDone = true;
    ArrayList<CrawlWorker> workerList = new ArrayList<>();
    
    Queue <Integer> freeWorkerIndices = new LinkedList<>();
    Long max_size = new Long(-1);
    
    EntityStore CrawlStore;
    EntityStore seenStore;
    
    String startUrl;
    Long startTime = System.currentTimeMillis();
    
    HashSet <String> currentRunningLinks = new HashSet<>();
    public PrimaryIndex <String, CrawlEntity> pIdxCrawl = null;
    
    public Crawler(){}
    
    public Crawler(String startUrl, StorageInterface db, int size, int count) {
        this.startUrl = startUrl;
        this.seenStore = db.getSeenStore();
        this.CrawlStore = db.getCrawlStore();
        this.max_size = new Long(size) * 1024 * 1024;
        this.max_count = count;
        
        for(int i=0; i<NUM_WORKERS; i++){
            CrawlWorker c = new CrawlWorker(this, this.CrawlStore, this.seenStore, max_size, i);
            freeWorkerIndices.add(i);
            workerList.add(c);
        }
        
        CrawlTask task = new CrawlTask(startUrl);
        queue.add(task);
        
        pIdxCrawl = workerList.get(0).pIdxCrawl;
    }

    ///// TODO: you'll need to flesh all of this out.  You'll need to build a thread
    // pool of CrawlerWorkers etc. and to implement the functions below which are
    // stubs to compile
    
    /**
     * Main thread
     */
    public void start() {}
    
    /**
     * Returns true if it's permissible to access the site right now
     * eg due to robots, etc.
     */
    public boolean isOKtoCrawl(RobotsTxtInfo rti, CrawlEntity ce) {
        Integer crawlDelay;
        if(ce == null)
            return true;
            
        crawlDelay = rti.getCrawlDelay("cis455crawler");
        
        if(crawlDelay == null)
            crawlDelay = rti.getCrawlDelay("*");
        
        if(crawlDelay == null)
            return true;
        
        if(-ce.lastAccessed + System.currentTimeMillis() >= crawlDelay*1000)
            return true; 
            
        System.out.println("False by CRAWL DELAY");
        return false;
    }


    /**
     * Returns true if the crawl delay says we should wait
     */
    public boolean deferCrawl(String site) { return false; }
    
    public RobotsTxtInfo getRobotsTxtInfo(URLInfo url, String protocol){
        String host = url.getHostName();
        int port = url.getPortNo();
        URL link = null;
        try{
            link = new URL( protocol, host, -1, "/robots.txt");
        }
        catch(MalformedURLException e){
            logger.error("URL malformed: " + protocol + "://" + host + ":" + port + "/robots.txt");
            return null;
        }
        

        HttpURLConnection con = null;
        String robotstxt = null;
//        System.out.println(link.toString());
        try{

            con = (HttpURLConnection) link.openConnection();
            con.setRequestProperty("User-Agent", "cis455crawler");
            HttpURLConnection.setFollowRedirects(true); 
            con.setInstanceFollowRedirects(true); 

            if(con.getResponseCode() != HttpURLConnection.HTTP_OK){
                logger.error("robots.txt: " + link.toString() + ":" + con.getResponseCode() );
                return null;
            }


            robotstxt = CrawlWorker.inputStreamToString(con.getInputStream());

        }
        catch(Exception e){
            logger.error("Cannot open connection to robots.txt: " + e + link.toString());
            return null;
        }
        
        String[] robotlines = robotstxt.split("\n");
        ArrayList <String> curUserAgents = null;
        RobotsTxtInfo robotsInfo = new RobotsTxtInfo();
        boolean userAgentFlag = false; //for multiple user agents together
        
        for(String line: robotlines){
            line = line.trim();
            
            if(line.length() == 0) continue;
            if(line.startsWith("#")) continue;
            
            line = line.split("#")[0];
            String [] spl = line.split(":");
            if(spl.length == 0) continue;
            
            if(spl[0].trim().toLowerCase().equals("user-agent")){
                String agent = spl[1].trim();
                if(!userAgentFlag){
                    curUserAgents = new ArrayList<>();
                    userAgentFlag = true;
                }
                
                curUserAgents.add(agent);
                robotsInfo.addUserAgent(agent);
                continue;
            }
            userAgentFlag = false;
            
            if(spl[0].trim().toLowerCase().equals("allow")){
                String path;
                if(spl.length == 1)
                    path = "";
                else
                    path = spl[1].trim();
                    
                for(String agent: curUserAgents)
                    robotsInfo.addAllowedLink(agent, path);
            }
            
            if(spl[0].trim().toLowerCase().equals("disallow")){
                String path;
                if(spl.length == 1)
                    path = "";
                else
                    path = spl[1].trim();
                for(String agent: curUserAgents){
                    if(path.equals(""))
                        path = "/";
                    robotsInfo.addDisallowedLink(agent, path);
                }
            }
            
            if(spl[0].trim().toLowerCase().equals("crawl-delay")){
                try{
                    String crawlDelay = spl[1].trim();
                    for(String agent: curUserAgents){
                        robotsInfo.addCrawlDelay(agent, Integer.parseInt(crawlDelay));
                    }
                }
                catch(Exception e){}
            }
        }
        return robotsInfo;
    }
    
    
    public boolean matchRobotsTxtLine(String path, String regex){
        String refPath = regex.replaceAll("\\*", ".*?");
        if(refPath.endsWith("/")){
            refPath += ".*";
        }
        Pattern p = Pattern.compile(refPath);
        Matcher m = p.matcher(path);
        return m.matches();
    }
    /**
     * Returns true if it's permissible to fetch the content,
     * eg that it satisfies the path restrictions from robots.txt
     */
    public boolean isOKtoParse(URLInfo url, String protocol, CrawlEntity ce) {
        RobotsTxtInfo robotsInfo= getRobotsTxtInfo(url, protocol);
        
        if(!isOKtoCrawl(robotsInfo, ce)) 
            return false;
            
        if(robotsInfo == null)
            return false;

        if(!url.getFilePath().startsWith("/"))
            url.setFilePath("/" + url.getFilePath());

        
        String userAgent = "cis455crawler";
        if(!robotsInfo.containsUserAgent(userAgent)){
            userAgent = "*";
            if(!robotsInfo.containsUserAgent(userAgent)){
                return true;
            }
        }

        if(robotsInfo.getAllowedLinks(userAgent) != null)
            for(String allowed: robotsInfo.getAllowedLinks(userAgent)){
                if(matchRobotsTxtLine(url.getFilePath(), allowed));
                    return true;
            }

        if(robotsInfo.getDisallowedLinks(userAgent) != null)
            for(String disallowed: robotsInfo.getDisallowedLinks(userAgent)){
                if(matchRobotsTxtLine(url.getFilePath(), disallowed))
                    {
                        return false;
                    }
            }
        return true; 
    }
    
    /**
     * Returns true if the document content looks worthy of indexing,
     * eg that it doesn't have a known signature
     */
    public boolean isIndexable(String content) { return true; }
    
    /**
     * We've indexed another document
     */
    public void incCount() {
        count += 1;
    }
     
    /**
     * Workers should notify when they are processing an URL
     */
    public void setWorking(boolean working) {}
    
    /**
     * Workers should call this when they exit, so the master
     * knows when it can shut down
     */
    public synchronized void notifyThreadExited(CrawlWorker worker) {
        freeWorkerIndices.add(worker.index);
        currentRunningLinks.remove(worker.task.raw);
    }
    
    /**
     * Workers can poll this to see if they should exit, ie the
     * crawl is done
     */
    public boolean isDone(){
        if(count >= max_count){
            System.out.println("Count Exceeded: " + count);
            return true;
        }
        
//        if(getFreeWorkerSize() == NUM_WORKERS && queue.size() == 0){
//            System.out.println("EXIT PATH 2: " + getFreeWorkerSize() +": " + queue.size());
//            return true;
//        }
        return false;
    }
    
    public int getFreeWorkerSize(){
        int cnt = 0;
        freeWorkerIndices.clear();
        for(CrawlWorker w: workerList){
            if(w.isFree) 
            {
                freeWorkerIndices.add(w.index);
                cnt+=1;
            }
        }
        return cnt;
    }
    /**
     * Main program:  init database, start crawler, wait
     * for it to notify that it is done, then close.
     */
    public static void main(String args[]) {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);

        if (args.length < 3 || args.length > 5) {
            System.out.println("Usage: Crawler {start URL} {database environment path} {max doc size in MB} {number of files to index}");
            System.exit(1);
        }
        
        System.out.println("Crawler starting" + args[1]);
        String startUrl = args[0];
        String envPath = args[1];
        Integer size = Integer.valueOf(args[2]);
        Integer count = args.length == 4 ? Integer.valueOf(args[3]) : 100;
        
        StorageInterface db = StorageFactory.getDatabaseInstance(envPath);
        
        Crawler crawler = new Crawler(startUrl, db, size, count);
        
        System.out.println("Starting crawl of " + count + " documents, starting at " + startUrl);
        crawler.start();
        
        while(!crawler.isDone()){
            int abc = 0;
            try {

                Thread.sleep(10);
//                System.out.println(crawler.getFreeWorkerSize() + " :" + crawler.queue.size());
                if(crawler.getFreeWorkerSize() == Crawler.NUM_WORKERS && crawler.queue.size()==0)
                {
                    System.out.println("BREAKING");
                    break;
                }
                if(crawler.queue.size() == 0){

                    continue;
                }

                while(crawler.getFreeWorkerSize() == 0){
//                    System.out.println("NO free workers");

                    try{Thread.sleep(10);}catch(Exception e){}
                }
                
                CrawlWorker worker ;

                try{
                    worker = crawler.workerList.get(crawler.freeWorkerIndices.poll());

                }
                catch(Exception e){
                    System.out.println(crawler.freeWorkerIndices.size());
                    continue;
                }

                CrawlTask task = crawler.queue.poll();
                    
                if(crawler.currentRunningLinks.contains(task.raw)){
                    crawler.currentRunningLinks.remove(task.raw);
                    continue;
                }

                URLInfo urlInfo = new URLInfo(task.host, task.port, task.path);
//                urlInfo = new URLInfo(task.raw);
                
                boolean flag = true;
                if(!crawler.isOKtoParse(urlInfo, task.protocol, crawler.pIdxCrawl.get(task.raw))){
//                    System.out.println("FALSE: " + task.host+"/" + task.path);
                    flag = false;
                }
                
                if(flag){
                    worker.isFree = false;
                    crawler.currentRunningLinks.add(task.raw);
                    worker.setTask(task);
                    Thread t = new Thread(worker);
                    t.start();
                }    
            }             
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            
        // TODO: final shutdown
        System.out.println("Crawling Done - waiting for current requests to finish");
        while(crawler.getFreeWorkerSize() != Crawler.NUM_WORKERS){     
            int cnt = 0;
            for(CrawlWorker w: crawler.workerList)
                if( w.isFree) cnt+=1;
            System.out.println("NOT EXITING: " + crawler.freeWorkerIndices.size() +"::" + cnt);
            
            try{
                Thread.sleep(100);
            }
            catch(InterruptedException e){}

        }
        
        //Close the databases so that they autoSave
        StorageFactory.getDatabaseInstance(envPath).close();
        System.out.println("Done crawling!");
    }
    
    //get crawler timestamp
    public Long getStartTime(){
        return startTime;
    }
}