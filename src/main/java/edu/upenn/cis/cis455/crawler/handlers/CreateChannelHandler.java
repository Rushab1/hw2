package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.storage.StorageInterface;
import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;

public class CreateChannelHandler implements Route {
    StorageInterface db;
    
    public CreateChannelHandler(StorageInterface db){
        this.db = db;
        
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        Session session = req.session(false);
        if(session == null)
            return null;

        String path = req.pathInfo();
        int lastSlash = path.lastIndexOf("/");

        String check = path.substring(0, lastSlash);
        String xpathExp = req.queryParams("xpath");

        if(!check.equals("/create") || lastSlash == path.length()-1 || xpathExp == null){
            session.attribute("message", "Invalid Channel Request - MUST be of the form /create/channelName?params=values");
        }
 
        String channelName = path.substring(lastSlash + 1);
        String firstName = req.session().attribute("firstName");
        String message = db.addChannel(channelName, xpathExp, firstName);
        
        if(message.equals("already_exists")){
            session.attribute("message", "Channel " + channelName + " already exists");
        }
        else if(message.equals("unsuccessful")){
            session.attribute("message", "Channel couldn't be created");
        }
        else{
            session.attribute("message", "Channel " + channelName + " created successfully with expression = " + xpathExp);
        }

        resp.redirect("/index.html");
        return null;
    }
}