package edu.upenn.cis.stormlite.bolt;

import java.util.Arrays;
import java.util.Map;

import com.sleepycat.persist.PrimaryIndex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeVisitor;

import edu.upenn.cis.cis455.crawler.ChannelEntity;
import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.SingletonCrawler;
import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.xpathengine.XPathImpl;
import edu.upenn.cis.stormlite.IStreamSource;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

public class PathMatcherBolt implements IRichBolt{
    final static Logger logger = LogManager.getLogger(Crawler.class);
    
    OutputCollector collector;
    Crawler crawler;
    PrimaryIndex<String, ChannelEntity> pIdxChannel;
    
    public PathMatcherBolt(){
        crawler = SingletonCrawler.getInstance();
	    pIdxChannel = StorageFactory.getDatabaseInstance(null).getPIDxChannel();
    }
  	@Override
	public String getExecutorId() {
		return null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("docId", "content"));
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
	public void cleanup() {
		
	}
	
	@Override
	public void execute(Tuple input) {
	    try{
    	    Object objEntity = input.getObjectByField("xPathObj");
            XPathImpl x = (XPathImpl) objEntity;
            
            Object obj = input.getObjectByField("occurrenceEvent");
            OccurrenceEvent e = (OccurrenceEvent)obj;
            
            if(e.eventType.equals("exit")){
                ChannelEntity ce = new ChannelEntity(e.documentId, x.getValid());
                pIdxChannel.put(ce);
        	    logger.info(ce.docId +": " + Arrays.toString(x.getValid()));

        	    SingletonCrawler.decBufferSize();
                return;
            }
            x.evaluateEvent(e);
        }
        catch(Exception e){
            System.err.println(e);
        }
        SingletonCrawler.decBufferSize();
     }
}