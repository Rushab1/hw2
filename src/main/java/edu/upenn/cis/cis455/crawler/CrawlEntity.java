package edu.upenn.cis.cis455.crawler;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import java.text.SimpleDateFormat;

@Entity
public class CrawlEntity {
    public Integer responseCode;
    public String contentEncoding;
    public String contentType;
    public int contentLength;
    public String last_downloaded = null;
    public int port;
    public byte[] content;
    public Boolean successful = false;
    public String md5 = null;
    public String stringContent = null;
    public Long crawlerRunTimeStamp = new Long(-1);
    public Long lastAccessed = new Long(-1);
    @PrimaryKey
    public String link;
    
    public CrawlEntity(){
    }
    
    public CrawlEntity(String link, int port, Long crawlerRunTimeStamp){
        this.link = link;
        this.port = port;
        this.crawlerRunTimeStamp = crawlerRunTimeStamp;
    }
//    
//    public void setAll(int responseCode, String contentType, String contentEncoding, int contentLength, byte[] content){
//        this.responseCode = responseCode;
//        this.contentEncoding = contentEncoding;
//        this.contentLength = contentLength;
//        this.contentType = contentType;
//        this.content = content;
//        this.successful = true;
//    }
    
    public void setCrawlerRunTimeStamp(Long time){
        this.crawlerRunTimeStamp = time;
    }
    
    public void successful(boolean successful){
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        this.successful = successful;
        this.last_downloaded = sdf.format(System.currentTimeMillis());
        this.lastAccessed = System.currentTimeMillis();
    }
}