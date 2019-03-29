package edu.upenn.cis.cis455;

import org.junit.Test;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.SingletonCrawler;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.LocalCluster;
import edu.upenn.cis.stormlite.TopologyBuilder;
import edu.upenn.cis.stormlite.bolt.DOMParserBolt;
import edu.upenn.cis.stormlite.bolt.DocumentFetcherBolt;
import edu.upenn.cis.stormlite.bolt.LinkExtractorBolt;
import edu.upenn.cis.stormlite.bolt.PathMatcherBolt;
import edu.upenn.cis.stormlite.spout.IRichSpoutImpl;

public class TestBolts {
    LocalCluster cluster = null;
    
    public void run(){
        Crawler crawler = SingletonCrawler.getInstance();

        while(true){
            try{
                Thread.sleep(100);
                if(crawler == null){
                   crawler = SingletonCrawler.getInstance();
                   if(crawler == null)
                        continue;
                }
            }
            catch(InterruptedException e){}
            
            if( 
            crawler.queue.size() == 0 && SingletonCrawler.getbufferSize() == 0 ||
            crawler.getCnt() == crawler.max_count
            ){
                System.out.println("Buffer Size: " + SingletonCrawler.getbufferSize() + "\n" +
                                   "Queue Size: " + SingletonCrawler.getInstance().queue.size() + "\n" +
                                   "Documents Indexed: " + crawler.getCnt());
                System.out.println("Shutting Down");
                StorageFactory.getDatabaseInstance(null).close();
                cluster.killTopology("crawler");
                cluster.shutdown();
                break;
            }
        }
    }

    @Test
    public void test(){
        System.out.println(System.getProperty("user.dir"));
        StorageInterface db = StorageFactory.getDatabaseInstance(System.getProperty("user.dir") + "/www/testDB/");
        SingletonCrawler.initializeCrawler("https://dbappserv.cis.upenn.edu/crawltest/nytimes/", db, 5, 50);

        
        // ************************************************************** */
        // ************************************************************** */
        // ************************************************************** */
        //Create topology
        String QUEUE_SPOUT = "QUEUE_SPOUT";
        String DOC_FET_BOLT = "DOC_FET_BOLT";
        String LINK_EXT_BOLT = "LINK_EXT_BOLT";
        String DOM_PAR_BOLT = "DOM_PAR_BOLT";
        String PATH_MAT_BOLT = "PATH_MAT_BOLT";
        
        Config config = new Config();

        IRichSpoutImpl spout = new IRichSpoutImpl();
        DocumentFetcherBolt documentFetcherBolt = new DocumentFetcherBolt();
        LinkExtractorBolt linkExtractorBolt = new LinkExtractorBolt();
        DOMParserBolt domParserBolt = new DOMParserBolt();
        PathMatcherBolt pathMatcherBolt = new PathMatcherBolt();
        
        TopologyBuilder builder = new TopologyBuilder();

        // Only one source ("spout") for the words
        builder.setSpout(QUEUE_SPOUT, spout, 1);
        
        // Four parallel word counters, each of which gets specific words
        builder.setBolt(DOC_FET_BOLT, documentFetcherBolt, 4).shuffleGrouping(QUEUE_SPOUT);
        
        // A single printer bolt (and officially we round-robin)
        builder.setBolt(LINK_EXT_BOLT, linkExtractorBolt, 4).shuffleGrouping(DOC_FET_BOLT);
        builder.setBolt(DOM_PAR_BOLT, domParserBolt, 10).shuffleGrouping(DOC_FET_BOLT);
        builder.setBolt(PATH_MAT_BOLT, pathMatcherBolt, 1).shuffleGrouping(DOM_PAR_BOLT);

        cluster = new LocalCluster();
        
        cluster.submitTopology("crawler", config, builder.createTopology());
        run();
        
        assert(SingletonCrawler.getInstance().getCnt() == 12);
    }
}