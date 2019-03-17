package edu.upenn.cis.cis455.storage;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import spark.HaltException;
import spark.http.matching.Halt;

import java.io.File;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import spark.HaltException;

public class StorageInterfaceImpl implements StorageInterface{
    private Environment dbEnv = null;
    private EntityStore dbStore = null;
    private PrimaryIndex<String, dbEntity> pIdx;
    private int userCount = 0;
    
    public StorageInterfaceImpl(String directory){
        try {
            EnvironmentConfig envConfig = new EnvironmentConfig();
            StoreConfig storeConfig = new StoreConfig();
            
            envConfig.setAllowCreate(true);
            storeConfig.setAllowCreate(true);
            
            dbEnv = new Environment(new File(directory), envConfig);
            dbStore = new EntityStore(dbEnv, "EntityStore", storeConfig);
            pIdx = dbStore.getPrimaryIndex(String.class, dbEntity.class);    
        }
        catch (DatabaseException dbe) {
            System.err.println("Error opening environment and store: " + dbe.toString());
        } 
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
	public int addUser(String username, String password){
	    int id = 0;
	    System.out.println("User added1");
	    if(pIdx.contains(username)){
//            throw HaltException(200);
	    }
	    System.out.println("User added2");
	    userCount += 1;	 
	    id = userCount;
	    System.out.println("User added3");
	    dbEntity entity = new dbEntity(id, username, password);
	    System.out.println("User added4");
	    pIdx.put(entity);
	    System.out.println("User added5");
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
            System.out.println("Exception in StorageInterfaceImpl - 1: " + e);
        }
    
	    return exists;
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
	        dbEnv.close();
        }
        catch(Exception e){
        }
	}
}