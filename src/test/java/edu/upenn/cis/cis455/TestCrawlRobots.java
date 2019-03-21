package edu.upenn.cis.cis455;

import org.junit.Test;

import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.crawler.CrawlMaster;
import edu.upenn.cis.cis455.crawler.CrawlTask;
import edu.upenn.cis.cis455.crawler.CrawlWorker;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
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
import java.net.URL;

public class TestCrawlRobots {
    @Test
    public void test(){
        Crawler crawler = new Crawler();
        RobotsTxtInfo rti = crawler.getRobotsTxtInfo(new URLInfo("dbappserv.cis.upenn.edu", "/robots.txt"), "https");
        
        assert(rti.containsUserAgent("*"));
        assert(rti.containsUserAgent("cis455crawler"));
        assert(rti.containsUserAgent("evilcrawler"));
        
        assert(rti.getDisallowedLinks("cis455crawler").size() == 5);
        assert(rti.getCrawlDelay("cis455crawler") == 5);
        assert(rti.getCrawlDelay("*") == 10);
    }
}
