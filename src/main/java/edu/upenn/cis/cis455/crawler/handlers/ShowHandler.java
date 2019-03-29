package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;

import java.util.Arrays;

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
        
        
        System.out.println(Arrays.toString(db.getExpressions()));
        
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
                    "</title><br></header></div><br>";

        for(String docId: pIdxChannel.keys() ){
            CrawlEntity ce = pIdxCrawl.get(docId);
            System.out.println(index + " : " + Arrays.toString(pIdxChannel.get(docId).valid) +" : " + pIdxChannel.get(docId).valid[index]);
            
            if(pIdxChannel.get(docId).valid[index]){
                webpage += "Crawled on: " + ce.last_downloaded + "<br>";
                webpage += "Location: " + docId + "<br>";
                webpage += "<div class=\"document\">\n" + ce.stringContent + "</div><br><br>";
            }
        }
    
        return webpage;
    }
}