package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;

public class LogoutHandler implements Route {
    @Override
    public String handle(Request req, Response resp) throws HaltException {
        Session session = req.session(false);
        session.invalidate();
        resp.redirect("/login-form.html");
        return "Succesfully Logged Out";
    }
}
