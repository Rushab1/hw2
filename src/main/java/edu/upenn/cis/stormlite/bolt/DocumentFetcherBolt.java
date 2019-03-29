package edu.upenn.cis.stormlite.bolt;

import java.util.Map;
import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.crawler.CrawlTask;
import edu.upenn.cis.cis455.crawler.CrawlWorker;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.SingletonCrawler;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;


public class DocumentFetcherBolt implements IRichBolt{
    OutputCollector collector;
    Crawler crawler;
    
    public DocumentFetcherBolt(){
        crawler = SingletonCrawler.getInstance();
    }
    
	@Override
	public String getExecutorId() {
		return null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	    declarer.declare(new Fields("crawlEntity"));
	}

	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
	   this.collector = collector;
	}

	@Override
	public Fields getSchema() {
		return null;
	}

	@Override
	public void execute(Tuple input) {
	    try{
    		Object objTask = input.getObjectByField("url");
    		CrawlTask task = (CrawlTask)objTask;
    		
    		CrawlWorker worker = new CrawlWorker(crawler, crawler.CrawlStore, crawler.seenStore, crawler.max_size, -1);
    		worker.setTask(task);
    		
    		CrawlEntity crawlEntity = worker.execute();
    		collector.emit(new Values<>(crawlEntity));
    		SingletonCrawler.incBufferSize();
	   }
	   catch(Exception e){
	       System.err.println("DocumentFetcherBolt: " + e);
	   }
	   SingletonCrawler.decBufferSize();
	}

	@Override
	public void cleanup() {
	}
}