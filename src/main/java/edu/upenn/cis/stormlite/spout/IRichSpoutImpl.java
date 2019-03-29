package edu.upenn.cis.stormlite.spout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import edu.upenn.cis.cis455.crawler.CrawlTask;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.SingletonCrawler;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;

public class IRichSpoutImpl implements IRichSpout{
    SpoutOutputCollector collector;
    Crawler crawler;
    
    public IRichSpoutImpl(){
        crawler = SingletonCrawler.getInstance();
    }

    
  	@Override
	public String getExecutorId() {
		return null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	    declarer.declare(new Fields("url"));
	}

	@Override
	public void setRouter(IStreamRouter router) {
	    this.collector.setRouter(router);
	}

	@Override
	public void open(Map<String, String> config, TopologyContext topo, SpoutOutputCollector collector) {
	    this.collector = collector;
	}

	@Override
	public void nextTuple(){
	    System.err.println("Buffer Size: " + SingletonCrawler.getbufferSize() + ":" + SingletonCrawler.getInstance().queue.size() + ":" + crawler.getCnt());
	    if(crawler.queue.size() > 0){
	       collector.emit( new Values<>(crawler.queue.poll()));
	       SingletonCrawler.incBufferSize();
        }

	    try{Thread.sleep(100);}
	    catch(InterruptedException e){}
	}

	@Override
	public void close() {
	}
}