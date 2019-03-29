package edu.upenn.cis.cis455.storage;

import java.nio.channels.Channels;
import java.util.Map;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis.cis455.crawler.ChannelEntity;
import edu.upenn.cis.cis455.crawler.ChannelNameEntity;
import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.crawler.HashEntity;

public interface StorageInterface {
    /**
     * How many documents so far?
     */
	public int getCorpusSize();
	
	/**
	 * Add a new document, getting its ID
	 */
	public int addDocument(String url, String documentContents);
	
	/**
	 * How many keywords so far?
	 */
	public int getLexiconSize();
	
	/**
	 * Gets the ID of a word (adding a new ID if this is a new word)
	 */
	public int addOrGetKeywordId(String keyword);
	
	/**
	 * Adds a user and returns an ID
	 */
	public int addUser(String username, String password, String firstName, String lastName);
	
	public String addChannel(String channelName, String xpathExp, String firstName);
	
	/**
	 * Tries to log in the user, or else throws a HaltException
	 */
	public boolean getSessionForUser(String username, String password);
	
	/**
	 * Retrieves a document's contents by URL
	 */
	public String getDocument(String url);
	
	/**
	 * Shuts down / flushes / closes the storage system
	 */
	public void close();
	
	public EntityStore getSeenStore();
	
	public EntityStore getCrawlStore();
	
	public EntityStore getChannelStore();
	
	public EntityStore getChannelNameStore();
	
	public Map<String, String> getUserData(String username, String pass);	
	
	public PrimaryIndex<String, CrawlEntity> getPIDxCrawl();
	public PrimaryIndex<String, HashEntity> getPIDxHash();
	/*
	 * docId + valid bits
	 */
	public PrimaryIndex<String, ChannelEntity> getPIDxChannel(); 
	
	/*
	 * channel name + xpath Expression + first name of user who created the channel
	 */
	public PrimaryIndex<String, ChannelNameEntity> getPIDxChannelName();
	
	public String[] getExpressions();
    public String[] getChannelNames();
    public String[] getChannelUsernames();
}
