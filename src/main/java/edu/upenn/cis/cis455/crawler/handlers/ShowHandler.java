package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis.cis455.crawler.ChannelEntity;
import edu.upenn.cis.cis455.crawler.ChannelNameEntity;
import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.storage.StorageInterface;

public class ShowHandler implements Route {
    StorageInterface db;
    
    public ShowHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        PrimaryIndex<String, ChannelNameEntity> pIdxChannelName=  db.getPIDxChannelName();
        PrimaryIndex<String, ChannelEntity> pIdxChannel=  db.getPIDxChannel();
        PrimaryIndex<String, CrawlEntity> pIdxCrawl= db.getPIDxCrawl();
        
        String queryChannelName = req.queryParams("channel");
        if(queryChannelName==null){
            req.session(false).attribute("message", "Invalid show request");
            resp.redirect("/index.html");
            return null;
        }
        
        String webpage = "";
        
        int cnt = 0;
        int index = -1;
        
        for(String channelName: pIdxChannelName.keys()){
            if(channelName.equals(queryChannelName)){
                index = cnt;
            }
            cnt += 1;
        }
        
        if(index==-1){
            req.session(false).attribute("message", "Channel does not exist");
            resp.redirect("/index.html");
            return null;
        }
        
        ChannelNameEntity cne =  pIdxChannelName.get(queryChannelName);

        webpage += "<div class=\"channelheader\"><br>";
        webpage +=  "<header><br><title>Channel name: " + queryChannelName + 
                    ", created by: " + cne.userFirstname + 
                    "</title><br>" +
                    "Channel name: " + queryChannelName + 
                    ", created by: " + cne.userFirstname + 
                    "</header></div><br>";

        String pattern = "yyyy-MM-dd'T'hh:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        
        SimpleDateFormat sdfOrig = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        sdfOrig.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        for(String docId: pIdxChannel.keys() ){
            CrawlEntity ce = pIdxCrawl.get(docId);

            
            if(pIdxChannel.get(docId).valid[index]){
                String dateTime = ce.last_downloaded;
                try{
//                    Matcher m = Pattern.compile("(..., .. ... ....) (.*)").matcher(dateTime);
//                    m.find();
//                    dateTime = m.replaceAll(m.group(1) + "T" + m.group(2));
                    
                    Date crawledOnDate = sdfOrig.parse(dateTime);
                    
                    System.out.println(crawledOnDate);
                    dateTime = sdf.format(crawledOnDate);
                }
                catch(Exception e){
                    System.err.println("Incorrect Date Format: " + e);
                }
            
                webpage += "Crawled on: " + dateTime + "<br>";
                webpage += "Location: " + docId + "<br>";
                webpage += "<div class=\"document\">\n" + ce.stringContent + "</div><br><br>";
            }
        }
    
        return webpage;
    }
}