package edu.upenn.cis.cis455.crawler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.storage.StorageFactory;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

import java.io.InputStream;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class CrawlWorker implements Runnable{
    final static Logger logger = LogManager.getLogger(CrawlWorker.class);
    public Crawler crawlMaster;
    public Boolean isFree = true;
    public CrawlTask task = null;
    public EntityStore CrawlStore = null;
    public EntityStore seenStore = null;
    public Long max_size =  null;
    public PrimaryIndex <String, CrawlEntity> pIdxCrawl = null;
    public PrimaryIndex <String, HashEntity> pIdxHash = null;    
    public int index=-1;
    
    public CrawlWorker(){
    }
    
    public CrawlWorker(Crawler crawlMaster, EntityStore CrawlStore, EntityStore seenStore, Long max_size, int index){
        this.crawlMaster = crawlMaster;
        this.CrawlStore = CrawlStore;
        this.seenStore = seenStore;
        pIdxCrawl = StorageFactory.getDatabaseInstance(null).getPIDxCrawl();
        pIdxHash = StorageFactory.getDatabaseInstance(null).getPIDxHash();
        this.max_size = max_size;
        this.index = index;
    }
    
    public void setTask(CrawlTask task){
        this.task = task;
    }
    
    public void done(){
        this.isFree=true;
    }
    
    public void run(){
        execute();
    }

    public CrawlEntity execute(){
        this.isFree = false;
        HttpURLConnection con;
        URL url;
        
        CrawlEntity crawlEntity = null;
        if( pIdxCrawl != null)
              crawlEntity = pIdxCrawl.get(task.raw);
        
        if(crawlEntity == null){
            Long time;
            
            if(crawlMaster != null)
                 time = crawlMaster.getStartTime();
            else
                 time = 0L;
            
            crawlEntity = new CrawlEntity(task.raw, task.port, time);
        }

        try{
            url = new URL(task.protocol, task.host, task.port, task.path);
            con = (HttpURLConnection) url.openConnection(); 
            con.setRequestProperty("User-Agent", "cis455crawler");
            
            if( crawlEntity.last_downloaded != null &&
                pIdxCrawl.get(crawlEntity.link).crawlerRunTimeStamp.equals(crawlMaster.getStartTime())){
                con.setRequestProperty("If-Modified-Since", crawlEntity.last_downloaded);
            }
        }
        catch(MalformedURLException e){
            crawlEntity.successful(false);
            logger.error("Exception in Crawl Worker-1: " + e +": url="+task.raw);
            crawlMaster.notifyThreadExited(this);
            done();
//            return;
            return crawlEntity;
        }
        catch(IOException e){
            crawlEntity.successful(false);
            logger.error("Exception in Crawl Worker-2: " + e +": url="+task.raw);
            crawlMaster.notifyThreadExited(this);
            done();
            return crawlEntity;
//            return;
        }
            
            
        HttpURLConnection.setFollowRedirects(true); 
        con.setInstanceFollowRedirects(false);  
        
        boolean successfulHead = sendHeadRequest(url, con, crawlEntity);
        boolean successfulGet = false;

        if(! successfulHead)
        {
//            logger.info(task.raw + ": " + crawlEntity.responseCode + ": NOT Indexed: Download UNSuccessful");
//            logger.error(crawlEntity.responseCode + ": "+ task.raw);
            if(crawlEntity.responseCode == 301 || crawlEntity.responseCode == 302){
                String newLink = con.getHeaderField("Location");
                
                CrawlTask newTask = new CrawlTask(newLink);
                URLInfo urlInfo = new URLInfo(newTask.host, newTask.port, newTask.path);
                if( crawlMaster.isOKtoParse(urlInfo, newTask.protocol, crawlMaster.pIdxCrawl.get(newTask.raw)) ){
//                    System.out.println("OK TO PARSE: " + newTask.raw);
                    crawlMaster.queue.add(newTask);
                }
                logger.info(task.raw + ": " + crawlEntity.responseCode + ": NOT Indexed" );
            }
            else{
                logger.info(task.raw + ": " + crawlEntity.responseCode + ": NOT Indexed: Download UNSuccessful");
            }
            done();
            crawlMaster.notifyThreadExited(this);
            return crawlEntity;
//            return;
        }
   
        if(successfulHead){
//            logger.info(task.raw +": Downloading"  );
            if(crawlEntity.responseCode != HttpURLConnection.HTTP_NOT_MODIFIED){
                successfulGet = sendGetRequest(task, crawlEntity);
            }
        }
        
        if(successfulGet){//this is not entered if not_modified is the response code
            crawlEntity.md5 = MD5.hash(crawlEntity.content);
            
            //Add new links if md5 not presentor is different
            //OR Add new links if this is a new crawler run
            if( pIdxHash.get(crawlEntity.link) == null || 
                !pIdxHash.get(crawlEntity.link).md5.equals(crawlEntity.md5) 
                ||
                pIdxCrawl.get(crawlEntity.link) == null
                ||
                pIdxCrawl.get(crawlEntity.link).crawlerRunTimeStamp == null
                ||
                !pIdxCrawl.get(crawlEntity.link).crawlerRunTimeStamp.equals(crawlMaster.getStartTime())
                ){
                    

//                pIdxHash.put(new HashEntity(crawlEntity.link, crawlEntity.md5));
//                if(crawlEntity.contentType.equals("text/html")){
//                    ArrayList<CrawlTask> newTasks = getTasks(crawlEntity.stringContent, task);
                
//                    for(CrawlTask task: newTasks){
//                        crawlMaster.queue.add(task);
//                    }
                    
//                    logger.info(task.raw +": Indexed" );
//                }
//                crawlMaster.incCount();
            }
            else{
                if(pIdxHash.get(crawlEntity.link).md5.equals(crawlEntity.md5))
                    logger.info(task.raw +": NOT Indexed: md5 exists - not saving to file or indexing links from this page"  );
                else{}
            }
        }
        
//        crawlEntity.crawlerRunTimeStamp = crawlMaster.getStartTime();
//        pIdxCrawl.put(crawlEntity);

        done();
        crawlMaster.notifyThreadExited(this);
        return crawlEntity;
	}
	
	public static ArrayList<CrawlTask> getTasks(String content, CrawlTask task){
	    Document html = Jsoup.parse(content, task.raw);
	    ArrayList <CrawlTask> newTasks = new ArrayList<>();
	    
	    for(Element e: html.select("a[href]")){
	        String link = e.toString();
	        
	        Pattern p = Pattern.compile("href=\"(.*?)\"");
            Matcher m = p.matcher(link);
            
            if (m.find()) {
                link = m.group(1); // this variable should contain the link URL
            }
	        
	        URLInfo urlInfo = new URLInfo(link);
	        CrawlTask newTask = null;
	        
	        if(urlInfo.isSecure() == false && urlInfo.getHostName()==null)
	        {//This means that the host is same as current page
	               newTask = new CrawlTask(task.protocol, task.host, task.port, link);
	           
	           //get task filepath directory
	           int index = task.raw.lastIndexOf("/");
	           
	           if(index == task.raw.length()){
	               if(link.startsWith("/")){
	                  if(link.length() == 1)
	                    link = "";
                      else
                        link = link.substring(1);
	               }
       	           newTask = new CrawlTask(task.protocol, task.host, task.port, link);
	           }
	           else if(index != -1){
	               if(!link.startsWith("/"))
	                   link = "/" + link;
	               
	               String modifiedLink = task.raw.substring(0, index) + link;
       	           newTask = new CrawlTask(modifiedLink);
	           }
	           else{//index = -1 ie "/" not in task.raw
       	           newTask = new CrawlTask(task.protocol, task.host, task.port, link);
	           }
	        }
	        else{
	            newTask = new CrawlTask(link);
            }
            newTasks.add(newTask);
        }
	    return newTasks;
	}
	
	//////////////////////////////////
	/////////////////////////////////
	public boolean sendHeadRequest(URL url, HttpURLConnection con, CrawlEntity crawlEntity){
        try{
            con.setRequestMethod("HEAD");
        }
        catch(Exception e){
            crawlEntity.successful(false);
            logger.error("Exception in Crawl Worker-3: " + e);
            return false;
        }
        
        Integer responseCode = null;
        
        try{
            responseCode = con.getResponseCode();
            crawlEntity.responseCode = responseCode;
        }
        catch(Exception e){
            logger.error("Exception in CrawlWorker-4:" + e);
            crawlEntity.successful(false);
            con.disconnect();
            return false;
        }
        
        if(responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED){
            crawlEntity.successful(false);
            con.disconnect();
            return false;
        }
        
        if(responseCode == HttpURLConnection.HTTP_NOT_MODIFIED){
            logger.info(task.raw + ": NOT MODIFIED");
            crawlEntity.successful(true);
            con.disconnect();
            return true;
        }
        
        String contentType;
        String contentEncoding;
        int contentLength;
        
        try{
            contentType = con.getContentType();
            
            String[] split = contentType.split(";");
            if(split.length > 1){
                contentType = split[0].trim();
                contentEncoding = split[1].trim().split("=")[1].trim();
            }
            else{
                contentEncoding = con.getContentEncoding();
                if(contentEncoding == null){
                    contentEncoding = "UTF-8";
                }
            }
            
            contentLength = con.getContentLength();
            crawlEntity.contentType = contentType;
            crawlEntity.contentLength = contentLength;
            crawlEntity.contentEncoding = contentEncoding;
        }
        catch(Exception e){
            crawlEntity.successful(false);
            con.disconnect();
            logger.error("Exception in CrawlWorker-7: " + e);
            return false;
        }

        
        if(! (
               contentType.equals("text/html") || 
               contentType.equals("text/xml" ) ||
               contentType.equals("application/xml") ||
               contentType.endsWith("+xml")
               )){
                   con.disconnect();
                   crawlEntity.successful(false);
                   logger.error("EXCEP HERE: " + contentType);
                   return false;
        }
        
        if(contentLength > max_size){
            logger.error("EXCEP HERE: " + max_size + " : " + contentLength );
            crawlEntity.successful(false);
            con.disconnect();
            return false;
        }   
        
        crawlEntity.successful(true);
        return true;
	}
	
	//////////////////////////////////////////
	/////////////////////////////////////////
	public boolean sendGetRequest(CrawlTask task, CrawlEntity crawlEntity){
	     /*
         * All set - HEAD request successful
         * Time for GET 
         */
        
        HttpURLConnection con;
        URL url;
        try{
            url = new URL(task.protocol, task.host, task.port, task.path);
            con = (HttpURLConnection) url.openConnection(); 
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "cis455crawler");
            HttpURLConnection.setFollowRedirects(false); 
            con.setInstanceFollowRedirects(false); 
            con.getResponseCode();
        }
        catch(MalformedURLException e){
            crawlEntity.successful(false);
            logger.error("Exception in Crawl Worker-8: " + e +": url="+task.raw);
            return false;
        }
        catch(IOException e){
            crawlEntity.successful(false);
            logger.error("Exception in Crawl Worker-9: " + e +": url="+task.raw);
            return false;
        }
	    InputStream inputStream = null;

        try{
            inputStream = con.getInputStream();
        }
        catch(Exception e){
            logger.error("Exception in CrawlWorker-5:" + e);
            crawlEntity.successful(false);
            con.disconnect();
            return false;
        }
        
        crawlEntity.stringContent = inputStreamToString(inputStream);
        crawlEntity.content = crawlEntity.stringContent.getBytes();
        
        crawlEntity.successful(true);
        return true;
	}
	
	public static String inputStreamToString(InputStream inputStream){
        String result = null;
        StringBuffer sb = new StringBuffer();
        InputStream is = null;
        
        try {
            is = new BufferedInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine +"\n");
            }
            result = sb.toString();
            }
            catch (Exception e) {
                result = null;
            }
            finally {
                if (is != null) {
                    try {is.close();} 
                    catch (IOException e) {}
                }   
            }
            return result;
	}
}