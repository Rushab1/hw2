package edu.upenn.cis.cis455;

import org.junit.Test;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;


public class TestCrawlRobots {
    //This test checks whether the crawler correctly checks for robots.txt
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
