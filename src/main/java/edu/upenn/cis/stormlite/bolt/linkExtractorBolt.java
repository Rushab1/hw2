package edu.upenn.cis.stormlite.bolt;

import java.util.Map;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

public class linkExtractorBolt implements IRichBolt{
	@Override
	public String getExecutorId() {
		return null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		
	}

	@Override
	public void setRouter(IStreamRouter router) {
		this.router = router;
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
	    		
	}

	@Override
	public void cleanup() {
		
	}
}
