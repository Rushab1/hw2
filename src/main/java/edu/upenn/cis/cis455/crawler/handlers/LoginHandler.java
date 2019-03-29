package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;

import java.util.Map;

import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.storage.StorageInterface;

public class LoginHandler implements Route {
    private int MAX_INAC_INTERVAL = 300; // Five Minutes
    StorageInterface db;
    
    public void setMaxInacInterval(int seconds){
        this.MAX_INAC_INTERVAL = seconds;
    }
    
    public LoginHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        String username = req.queryParams("username");
        String pass = req.queryParams("password");
        
        try{
            System.out.println(db.getSessionForUser(username, pass));
        }
        catch(Exception e){
            System.out.println("EXception in LoginHandler-1: " + e);
        }
        if (db.getSessionForUser(username, pass)) {
            System.out.println("Logged in!");
            
            Session session = req.session();
            session.maxInactiveInterval(MAX_INAC_INTERVAL);
            
            Map<String, String> data = db.getUserData(username, pass);
            
            session.attribute("user", username);
            session.attribute("password", pass);
            session.attribute("firstName", data.get("firstName"));
            session.attribute("lastName", data.get("lastName"));
            
            resp.redirect("/index.html");
        } else {
            System.err.println("Invalid credentials");
            resp.redirect("/login-form.html");
        }
        return "";
    }
}