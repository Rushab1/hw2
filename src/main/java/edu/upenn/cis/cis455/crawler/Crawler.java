package edu.upenn.cis.cis455.crawler;

/*
 * TODO: 
 * redirect directly writes to queue
 * In XPathImpl the Map needs to be updated on close
 */

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedList;
import java.util.Queue;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.storage.StorageInterface;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Crawler implements CrawlMaster {
    final static Logger logger = LogManager.getLogger(Crawler.class);
    static final int NUM_WORKERS = 50;
    public Queue<CrawlTask> queue = new LinkedList<>();
    Integer count = 0;
    public Integer max_count = 0;
    
    Boolean isDone = true;
    ArrayList<CrawlWorker> workerList = new ArrayList<>();
    
    Queue <Integer> freeWorkerIndices = new LinkedList<>();
    public Long max_size = new Long(-1);
    
    public String envPath;
    public StorageInterface db;
    public EntityStore CrawlStore;
    public EntityStore channelStore;
    public EntityStore seenStore;
    
    String startUrl;
    Long startTime = System.currentTimeMillis();
    
    HashSet <String> currentRunningLinks = new HashSet<>();
    public PrimaryIndex <String, CrawlEntity> pIdxCrawl = null;
    public PrimaryIndex <String, ChannelEntity> pIdxChannel = null; 

    /**
     * Main program:  init database, start crawler, wait
     * for it to notify that it is done, then close.
     */
    public static void main(String args[]) {
        Executable executable = new Executable();
        Thread thread = new Thread(executable);
        thread.start();
        Executable.crawlerMain(args);
    }
        
    public Crawler(){}
    
    public Crawler(String startUrl, StorageInterface db, int size, int count) {
        this.startUrl = startUrl;
        this.db = db;
        this.seenStore = db.getSeenStore();
        this.CrawlStore = db.getCrawlStore();
        this.channelStore = db.getChannelStore();
        
        this.pIdxChannel = db.getPIDxChannel();
        this.pIdxCrawl = db.getPIDxCrawl();
        
        this.max_size = new Long(size) * 1024 * 1024;
        this.max_count = count;
        
        CrawlTask task = new CrawlTask(startUrl);
        boolean isOkToParse = isOKtoParse(new URLInfo(task.host, task.port, task.path), task.protocol, new CrawlEntity());
        if(isOkToParse){
            queue.add(task);
        }
        else{
            logger.error(": startUrl cannot be crawled: robots.txt: Permission denied");
        }
    }
    
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
            
        System.out.println(ce.link + ": Not parsing due to CRAWL DELAY: " + (-ce.lastAccessed + System.currentTimeMillis()) +" : "  + (crawlDelay*1000));
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
        
    //get crawler timestamp
    public Long getStartTime(){
        return startTime;
    }
    
    public int getCnt(){
        return count;
    }
}