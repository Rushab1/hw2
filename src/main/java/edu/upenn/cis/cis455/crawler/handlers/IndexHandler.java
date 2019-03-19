package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;

public class IndexHandler implements Route {
    
    public IndexHandler() {}

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        String user = req.session().attribute("firstName");
        String pass = req.session().attribute("lastName");

        
        String body = "<html><title> Index </title>" + 
                      "<body> <h3> Welcome, " + user + " " + pass + "</h3>" +  
                      "<br><br>" +
                      "<form method=\"POST\" action=\"/logout\">" +
                      "<input type=\"Submit\" value=\"Log out\"/>" +
                      "</form>" + 
                      "</body></html>";
        resp.body(body);

        resp.status(200);
        resp.header("Content-Type", "text/html");
        resp.header("Content-Length", Integer.toString(body.length()));
        resp.header("Set-Cookie", req.cookie("JSESSIONID"));
        
        return body;
    }
}
