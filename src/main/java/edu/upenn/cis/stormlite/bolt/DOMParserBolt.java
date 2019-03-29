package edu.upenn.cis.stormlite.bolt;

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
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;

public class DOMParserBolt implements IRichBolt{
    final static Logger logger = LogManager.getLogger(Crawler.class);
    
    OutputCollector collector;
    Crawler crawler;
    PrimaryIndex<String, ChannelEntity> pIdxChannel;
    public DOMParserBolt(){
        crawler = SingletonCrawler.getInstance();
	    pIdxChannel = StorageFactory.getDatabaseInstance(null).getPIDxChannel();
    }
  	@Override
	public String getExecutorId() {
		return null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("xPathObj", "occurrenceEvent"));
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
	
	public void traverse(String docId, Document doc, XPathImpl x){
	    
	    doc.traverse(new NodeVisitor(){
	        public void head(Node node, int depth) {
                if(!node.nodeName().startsWith("#")){
    	            OccurrenceEvent e = new OccurrenceEvent(docId, "open", node.nodeName(), null);
                    OccurrenceEvent eText = new OccurrenceEvent(docId, "text", node.nodeName(), node.toString());
                
                    collector.emit(new Values<>(x, e));
                    SingletonCrawler.incBufferSize();
                    
                    collector.emit(new Values<>(x, eText));
                    SingletonCrawler.incBufferSize();
                    x.evaluateEvent(eText);
                }
            }
            
            public void tail(Node node, int depth) {
                if(!node.nodeName().startsWith("#")){
                    OccurrenceEvent e = new OccurrenceEvent(docId, "close", node.nodeName(), null);
//                    x.evaluateEvent(e);
                    collector.emit(new Values<>(x, e));
                    SingletonCrawler.incBufferSize();
                }
            }
        });
	}
	
	@Override
	//don't decrease buffer size here since DocumentFetcherBolt emits to link extractor as well as here
	//Don't worry this won't cause issues
	public void execute(Tuple input) {
	    try{
    	    Object objEntity = input.getObjectByField("crawlEntity");
            CrawlEntity crawlEntity = (CrawlEntity) objEntity;
            
            if(crawlEntity.stringContent == null)
            {
                //Don't decrease buffer size
                return;
            }

            Document doc;
            if(crawlEntity.contentType.equals("text/html"))
    	       doc = Jsoup.parse(crawlEntity.stringContent, crawlEntity.link);
    	    else{
    	       doc = Jsoup.parse(crawlEntity.stringContent, crawlEntity.link, Parser.xmlParser());
	       }


    	    XPathImpl x = new XPathImpl(pIdxChannel);
            String[] expressions = SingletonCrawler.getExpressions();

    	    x.setXPaths(expressions);

    	    if(crawlEntity.stringContent != null){
    	        try{
	               traverse(crawlEntity.link, doc, x);
	               }
	            catch(OutOfMemoryError e){
                   System.err.println(e);
	            }
	        }
       	    doc = null;
    	    collector.emit(new Values<>(x, new OccurrenceEvent(crawlEntity.link, "exit", null, null)));
    	    SingletonCrawler.incBufferSize();
	    }
        catch(Exception e){
            System.err.println("DOMParserBolt: " + e);
        }
//        Don't decrease buffer size
	}
}