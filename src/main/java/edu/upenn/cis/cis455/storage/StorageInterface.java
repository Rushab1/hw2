package edu.upenn.cis.cis455.storage;

import java.util.Map;

import com.sleepycat.persist.EntityStore;

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
	
	public Map<String, String> getUserData(String username, String pass);	
}
