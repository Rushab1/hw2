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
        String user=null, pass=null;
        try{
            user = req.queryParams("username");
            pass = req.queryParams("password");
        }
        catch(Exception e){
            System.out.println("Excception in RegisterHandler-1: "+ e);
        }

//        String first = req.queryParams("firstName");
//        String last = req.queryParams("lastName");

        Integer id = -1;
        id = db.addUser(user, pass);

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