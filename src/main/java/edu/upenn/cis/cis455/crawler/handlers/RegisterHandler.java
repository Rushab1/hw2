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
        String user = req.queryParams("username");
        String pass = req.queryParams("password");
//        String first = req.queryParams("firstName");
//        String last = req.queryParams("lastName");
        Integer id = -1;
        
        System.out.println("======>Register request for " + user + " and " + pass );
        try{
                    id = db.addUser(user, pass);
        }    
        catch(HaltException e){
            System.out.println("User Already Exists");
        }
        
        System.out.println("User added!");
        if(id != -1){
            LoginHandler loginHandler = new LoginHandler(db);
            loginHandler.handle(req, resp);
        }
        
        return id;
    }
}
