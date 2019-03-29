package edu.upenn.cis.cis455.crawler.handlers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;

public class ReadUtils {
    public static String readFile(String path, Charset encoding) throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    
    public static String createHomePage(String message, String firstName, String lastName){
        String homepage = "<html><title> Index </title>";
        
        //Welcome message
        homepage += "<body> <h3> Welcome, " + firstName + " " + lastName + "</h3>";
        
        //Messages
        if(message != null){
            homepage += "<br><br><b>Message:</b> " + message ;
        }
        
        StorageInterface db = StorageFactory.getDatabaseInstance(null);
        System.out.println(db +":" + db.getChannelNames().length);

        if(db != null){
            homepage += "<br><b>Available Channels</b><ul>";
            for(String channelName: db.getChannelNames()){
                homepage += "<li><a href=\"/show?channel=" + channelName + "\">" + channelName + "</a></li>";
                homepage += "</ul>";
            }
        }
        
        homepage += "<br>" +
                      "<form method=\"POST\" action=\"/logout\">" +
                      "<input type=\"Submit\" value=\"Log out\"/>" +
                      "</form>";
        
        homepage += "</body></html>";

        return homepage;
    }
}
