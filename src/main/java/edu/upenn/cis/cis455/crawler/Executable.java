package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class Executable implements Runnable{
    final static Logger logger = LogManager.getLogger(Executable.class);
    
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
                System.err.println("Buffer Size: " + SingletonCrawler.getbufferSize() + "\n" +
                                   "Queue Size: " + SingletonCrawler.getInstance().queue.size() + "\n" +
                                   "Documents Indexed: " + crawler.getCnt());
                System.out.println("Shutting Down");
                closeDB();
                System.exit(0);
            }
        }
        
    }
    
    public static void crawlerMain(String[] args){
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        
        if (args.length < 3 || args.length > 5) {
            System.out.println("Usage: Crawler {start URL} {database environment path} {max doc size in MB} {number of files to index}");
            System.exit(1);
        }
        
        System.out.println("Crawler starting" + args[1]);
        String startUrl = args[0];
        String envPath = args[1];
        Integer size = Integer.valueOf(args[2]);
        Integer count = args.length == 4 ? Integer.valueOf(args[3]) : 100;
        StorageInterface db = StorageFactory.getDatabaseInstance(envPath);
        
        SingletonCrawler.initializeCrawler(startUrl, db, size, count);
        Crawler crawler = SingletonCrawler.getInstance();
        
        System.out.println("Starting crawl of " + count + " documents, starting at " + startUrl);
        crawler.start();

        executeBolts(crawler);
        
        //Cluster shutdown wait for Threads to complete
        System.out.println("Cluster shutdown wait for Threads to complete");
        try{
                Thread.sleep(10000);
            }
        catch(InterruptedException e){}
        closeDB();
        System.out.println("Done crawling!");
    }
    
    public static void executeBolts(Crawler crawler){
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
        builder.setBolt(DOC_FET_BOLT, documentFetcherBolt, 1).shuffleGrouping(QUEUE_SPOUT);
        
        // A single printer bolt (and officially we round-robin)
        builder.setBolt(LINK_EXT_BOLT, linkExtractorBolt, 1).shuffleGrouping(DOC_FET_BOLT);
        builder.setBolt(DOM_PAR_BOLT, domParserBolt, 10).shuffleGrouping(DOC_FET_BOLT);
        builder.setBolt(PATH_MAT_BOLT, pathMatcherBolt, 1).shuffleGrouping(DOM_PAR_BOLT);

        LocalCluster cluster = new LocalCluster();
        
        cluster.submitTopology("crawler", config, builder.createTopology());

        try{
            Thread.sleep(100000);
        }
        catch(InterruptedException e){
            
        }
        
        cluster.killTopology("crawler");
        cluster.shutdown();
        System.exit(0);
    }
    
    public static void closeDB(){
         System.out.println("Shutting down database");

        //Close the databases so that they autoSave
        StorageFactory.getDatabaseInstance(null).close();
    }
}
