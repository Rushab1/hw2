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
        String first = req.session().attribute("firstName");
        String last = req.session().attribute("lastName");

        String body = ReadUtils.createHomePage(req.session().attribute("message"), first,last);
        resp.body(body);

        resp.status(200);
        resp.header("Content-Type", "text/html");
        resp.header("Content-Length", Integer.toString(body.length()));
        resp.header("Set-Cookie", req.cookie("JSESSIONID"));
        return body;
    }
}
