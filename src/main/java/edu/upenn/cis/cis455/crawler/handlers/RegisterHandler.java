package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import edu.upenn.cis.cis455.storage.StorageInterface;

public class RegisterHandler implements Route {
    StorageInterface db;
    
    public RegisterHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public Integer handle(Request req, Response resp) throws HaltException {
        String user=null, pass=null, firstName = null, lastName = null;
        try{
            user = req.queryParams("username");
            pass = req.queryParams("password");
            
            if(user.length() == 0 || pass.length() == 0){
                System.err.println("Username and password cannot be empty");
                resp.redirect("register.html");
                return -1;
            }
            firstName = req.queryParams("firstName");
            lastName = req.queryParams("lastName");
        }
        catch(Exception e){
            System.out.println("Excception in RegisterHandler-1: "+ e);
        }

        Integer id = -1;
        try{
            id = db.addUser(user, pass, firstName, lastName);
        }
        catch(Exception e){
            id = -1;
            System.out.println("ERROR" + e +": Redirecting");
            resp.redirect("/register.html");
            return -1;
        }

        if(id != -1){
            System.out.println("User added!");
            LoginHandler loginHandler = new LoginHandler(db);
            loginHandler.handle(req, resp);
        }
        else{
            System.err.println("User Already Exists");
            resp.redirect("/register.html");
        }
        
        return id;
    }
}