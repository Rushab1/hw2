package edu.upenn.cis.cis455.crawler;

import java.net.URI;
import java.util.Date;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
public class CrawlTask {
    public String protocol = null;
    public String host = null;
    public String path = null;
    public Integer port = -1;
    public String raw = null;
//    public String query = null;
    
    public CrawlTask(String raw){
        this.raw = raw;
        extractFields();
    }
    
    public CrawlTask(String protocol, String host, Integer port, String path){
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
//        this.query = query;
        
        this.raw = this.protocol + "://" + this.host;
        if(port != -1 && port != null)
            this.raw += ":" + this.port;
        
        if(! path.startsWith("/"))
            this.path = "/" + path;
        
        if(path.startsWith("./"))
        {
            this.path = path.replaceAll("\\.\\/", "/");
        }
        
        this.raw += this.path;
    }
    
    public void extractFields(){
        URLInfo urlInfo = new URLInfo(raw);
        
        if(urlInfo.isSecure()){
                try{
                    URI uri = new URI(raw);
                    protocol = "https";
                    host = uri.getHost();
                    path = uri.getRawPath();
                    port = uri.getPort();
//                    query = uri.getRawQuery();
                }catch(Exception e){}
        }
        else{
            protocol = "http";
            host = urlInfo.getHostName();
            path = urlInfo.getFilePath();
            port = urlInfo.getPortNo();
        }
    }
    
}
