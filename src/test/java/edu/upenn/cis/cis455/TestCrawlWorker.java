package edu.upenn.cis.cis455;

import org.junit.Test;

import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.crawler.CrawlMaster;
import edu.upenn.cis.cis455.crawler.CrawlTask;
import edu.upenn.cis.cis455.crawler.CrawlWorker;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import static org.junit.Assert.*;

import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;;

public class TestCrawlWorker{

    @Test
    public void test()throws IOException, MalformedURLException{
        //This test sends an http HEAD request via the required the worker function
        //It asserts that the output response code, content-type and content-length are working correctly
        
        CrawlWorker w = new CrawlWorker();
        w.max_size = 1024*1024*5L;
        String link ="https://dbappserv.cis.upenn.edu/crawltest.html";
        URL url = new URL("https://dbappserv.cis.upenn.edu/crawltest.html");
        HttpURLConnection con = (HttpURLConnection) url.openConnection(); 
        con.setRequestProperty("User-Agent", "cis455crawler");
        CrawlEntity ce = new CrawlEntity();
        
        boolean successfulHead = w.sendHeadRequest(url, con, ce);

        assertTrue(successfulHead == true);
        assertTrue(ce.contentType.equals("text/html"));
        assertTrue(ce.contentLength == 1186);
    }
}
