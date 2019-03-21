package edu.upenn.cis.stormlite.spout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import edu.upenn.cis.cis455.crawler.CrawlTask;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;

public class IRichSpoutImpl implements IRichSpout{
    IStreamRouter router;
    TopologyContext topo;
    SpoutOutputCollector collector;
    
    Queue <CrawlTask> queue = new LinkedList<>();
    
  	@Override
	public String getExecutorId() {
		return null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	    declarer.declare(new tupl);
	}

	@Override
	public void setRouter(IStreamRouter router) {
	    this.router = router;
	}

	@Override
	public void open(Map<String, String> config, TopologyContext topo, SpoutOutputCollector collector) {
	    this.topo = topo;
	    this.collector = collector;
	}

	@Override
	public void nextTuple(){
	    ArrayList <Object> tuple = new ArrayList<>();
	    
	    if(queue.size() > 0)
	       tuple.add(queue.poll());
	       collector.emit( tuple);
	}

	@Override
	public void close() {
	}
}