package edu.upenn.cis.cis455.storage;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.util.DbLoad;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.crawler.HashEntity;
import edu.upenn.cis.cis455.model.User;
import spark.HaltException;
import spark.http.matching.Halt;

import java.io.File;
import java.util.Map;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import spark.HaltException;

public class StorageInterfaceImpl implements StorageInterface{
    private Environment dbEnv = null;
    private EntityStore dbStore = null; //login store
    private EntityStore seenStore = null;
    private EntityStore crawlStore = null;

    private PrimaryIndex<String, User> pIdx;
    private PrimaryIndex<String, User> pIdxHash;
    private PrimaryIndex<String, CrawlEntity> pIdxCrawl;
    
    private int userCount = 0;
    
    public StorageInterfaceImpl(String directory){
        try {
            EnvironmentConfig envConfig = new EnvironmentConfig();
            StoreConfig storeConfig = new StoreConfig();
            
            envConfig.setAllowCreate(true);
            storeConfig.setAllowCreate(true);
            
            dbEnv = new Environment(new File(directory), envConfig);
            dbStore = new EntityStore(dbEnv, "EntityStore", storeConfig);
            seenStore =  new EntityStore(dbEnv, "EntityStore", storeConfig);
            crawlStore = new EntityStore(dbEnv, "EntityStore", storeConfig);
            
            pIdx = dbStore.getPrimaryIndex(String.class,User.class);
            pIdxCrawl = crawlStore.getPrimaryIndex(String.class, CrawlEntity.class);
        }
        catch (DatabaseException dbe) {
            System.err.println("Error opening environment and store: " + dbe.toString());
        } 
    }
    
    
    public EntityStore getSeenStore(){
        return seenStore;
    }
    
    public EntityStore getCrawlStore(){
        return crawlStore;
    }
    
    /**
     * How many documents so far?
     */

	public int getCorpusSize(){
	    return 0;
	}
	
	/**
	 * Add a new document, getting its ID
	 */
	public int addDocument(String url, String documentContents){
	    return 0;
	}
	
	/**
	 * How many keywords so far?
	 */
	public int getLexiconSize(){
	    return 0;
	}
	
	/**
	 * Gets the ID of a word (adding a new ID if this is a new word)
	 */
	public int addOrGetKeywordId(String keyword){
	    return 0;
	}
	
	/**
	 * Adds a user and returns an ID
	 */
	public int addUser(String username, String password, String firstName, String lastName){
	    int id = 0;
	    if(pIdx.contains(username)){
	        return -1;
	    }
	    userCount += 1;	 
	    id = userCount;
	    User entity = new User(id, username, password, firstName, lastName);
	    pIdx.put(entity);
	    return userCount;
	}
	
	/**
	 * Tries to log in the user, or else throws a HaltException
	 */
	public boolean getSessionForUser(String username, String password){
	    boolean exists = false;
	    try{
    	    exists = pIdx.get(username).checkPassword(password);
        }
        catch(Exception e){
            System.out.println("Exception in StorageInterfaceImpl - 2: " + e);
        }
    
	    return exists;
	}
	
	public Map<String, String> getUserData(String username, String pass){
	    if(getSessionForUser(username, pass)){
	        return pIdx.get(username).getData();
	    }
        return null;
	}
	
	/**
	 * Retrieves a document's contents by URL
	 */
	public String getDocument(String url){
	    return null;
	}
	
	/**
	 * Shuts down / flushes / closes the storage system
	 */
	public void close(){
	    try{
	        dbStore.close();
	        seenStore.close();
	        crawlStore.close();
	        dbEnv.close();
        }
        catch(Exception e){
        }
	}
	
	public PrimaryIndex<String, CrawlEntity> getPIDxCrawl(){
	    return pIdxCrawl;
	}
}