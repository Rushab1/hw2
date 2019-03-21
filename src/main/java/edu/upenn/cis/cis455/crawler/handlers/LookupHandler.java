package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;

import java.util.Map;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.storage.StorageInterface;

public class LookupHandler implements Route {
    StorageInterface db;
    
    public LookupHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        String url= req.queryParams("url");
        System.out.println(url);
        PrimaryIndex <String, CrawlEntity> pIdx = db.getPIDxCrawl();
        
        String body = "No Url Found";
        
        if(pIdx.get(url) != null){
            body = pIdx.get(url).stringContent;
            resp.body(pIdx.get(url).stringContent);
        }
        else{
            resp.body("No url found");
            body = "No URl Found";
        }
        return body;
      }
}
