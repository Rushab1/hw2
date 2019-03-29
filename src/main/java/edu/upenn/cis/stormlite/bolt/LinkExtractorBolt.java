package edu.upenn.cis.stormlite.bolt;

import java.util.ArrayList;
import java.util.Map;

import com.sleepycat.persist.PrimaryIndex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.crawler.CrawlTask;
import edu.upenn.cis.cis455.crawler.CrawlWorker;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.HashEntity;
import edu.upenn.cis.cis455.crawler.SingletonCrawler;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

public class LinkExtractorBolt implements IRichBolt{
//    OutputCollector collector;
    Crawler crawler;
    final static Logger logger = LogManager.getLogger(CrawlWorker.class);
    public PrimaryIndex <String, CrawlEntity> pIdxCrawl = null;
    public PrimaryIndex <String, HashEntity> pIdxHash = null;    

    public LinkExtractorBolt(){
        crawler = SingletonCrawler.getInstance();
        pIdxCrawl = StorageFactory.getDatabaseInstance(null).getPIDxCrawl();    
        pIdxHash = StorageFactory.getDatabaseInstance(null).getPIDxHash();
    }

	@Override
	public String getExecutorId() {
		return null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

	@Override
	public void setRouter(IStreamRouter router) {
//		this.collector.setRouter(router);
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
	}

	@Override
	public Fields getSchema() {
		return null;
	}

	@Override
	public void execute(Tuple input) {
	    try{
    	    Object objEntity = input.getObjectByField("crawlEntity");
    	    CrawlEntity crawlEntity = (CrawlEntity) objEntity;	    
    	    
    	    if(crawlEntity.stringContent == null){
    	        SingletonCrawler.decBufferSize();
    	        return;
    	    }
    
            if( pIdxHash.get(crawlEntity.link) == null
                    || 
                !pIdxHash.get(crawlEntity.link).md5.equals(crawlEntity.md5) 
                    ||
                pIdxCrawl.get(crawlEntity.link) == null
                    ||
                pIdxCrawl.get(crawlEntity.link).crawlerRunTimeStamp == null
                    ||
                !pIdxCrawl.get(crawlEntity.link).crawlerRunTimeStamp.equals(crawler.getStartTime())
                ){}
                
    	    else if(pIdxCrawl.get(crawlEntity.link).crawlerRunTimeStamp == crawler.getStartTime()){
                logger.info(crawlEntity.link +": NOT Indexed: Already indexed in this crawler run - not indexing links from this file" );
                SingletonCrawler.decBufferSize();
                return;
            }
            else{
                logger.info(crawlEntity.link +": NOT Indexed: md5 exists - Saving to file (update last accessed) but not indexing links"  );
                SingletonCrawler.decBufferSize();
                return;
            }

            crawler.incCount();
            pIdxHash.put(new HashEntity(crawlEntity.link, crawlEntity.md5));
            logger.info(crawlEntity.link +": Indexed" );
            crawlEntity.crawlerRunTimeStamp = crawler.getStartTime();
            pIdxCrawl.put(crawlEntity);
            
    	    CrawlTask task = new CrawlTask(crawlEntity.link);
    	    ArrayList<CrawlTask> newTasks = CrawlWorker.getTasks(crawlEntity.stringContent, task);
    	    
    	    for(CrawlTask newTask: newTasks){
        	    URLInfo urlInfo = new URLInfo(newTask.host, newTask.port, newTask.path);
        	    if( crawler.isOKtoParse(urlInfo, newTask.protocol, crawler.pIdxCrawl.get(newTask.raw)) ){
        	        crawler.queue.add(newTask);
        	    }
            }
    
            SingletonCrawler.indexed.add(crawlEntity.link);
        }
        catch(Exception e){
            System.err.println("LinkExtractorBOlt: " + e);
        }
        SingletonCrawler.decBufferSize();
	}

	@Override
	public void cleanup() {
		
	}
}
