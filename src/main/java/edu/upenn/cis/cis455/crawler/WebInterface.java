package edu.upenn.cis.cis455.crawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;

import edu.upenn.cis.cis455.crawler.handlers.IndexHandler;
import edu.upenn.cis.cis455.crawler.handlers.LoginFilter;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.crawler.handlers.LoginHandler;
import edu.upenn.cis.cis455.crawler.handlers.LogoutHandler;
import edu.upenn.cis.cis455.crawler.handlers.RegisterHandler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class WebInterface {
    final static Logger logger = LogManager.getLogger(WebInterface.class);
    public static void main(String args[]) {
        if (args.length < 1 || args.length > 2) {
            logger.info("Syntax: WebInterface {path} {root}");
            System.exit(1);
        }
        
        if (!Files.exists(Paths.get(args[0]))) {
            try {
                Files.createDirectory(Paths.get(args[0]));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        port(8080);
        StorageInterface database = StorageFactory.getDatabaseInstance(args[0]);
        
        LoginFilter testIfLoggedIn = new LoginFilter(database);
        
        if (args.length == 2) {
            staticFiles.externalLocation(args[1]);
            staticFileLocation(args[1]);
        }

            
        before("/*", "POST", testIfLoggedIn);
        // TODO:  add /register, /logout, /index.html, /, /lookup
        logger.info("Using Database Directory: " + database);
        
        LoginHandler loginHandler = new LoginHandler(database);
        loginHandler.setMaxInacInterval(300);//5 minutes
        
        post("/login", loginHandler);
        post("/register", "POST", new RegisterHandler(database));
        get("/index.html", new IndexHandler());
        get("/logout", new LogoutHandler());
        post("/logout", new LogoutHandler());
        get("/closeDB", "GET", (request, response)->{
                                database.close(); 
                                String resp = new String("Database Closed");
                                return resp;
                                }
                                );
        awaitInitialization();
    }
}
