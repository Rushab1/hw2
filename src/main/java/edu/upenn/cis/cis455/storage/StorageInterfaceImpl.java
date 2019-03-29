package edu.upenn.cis.cis455.storage;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.util.DbLoad;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis.cis455.crawler.ChannelEntity;
import edu.upenn.cis.cis455.crawler.ChannelNameEntity;
import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.crawler.HashEntity;
import edu.upenn.cis.cis455.model.User;
import spark.HaltException;
import spark.http.matching.Halt;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import spark.HaltException;

public class StorageInterfaceImpl implements StorageInterface{
    private Environment dbEnv = null;
    private EntityStore dbStore = null; //login store
    private EntityStore seenStore = null;
    private EntityStore crawlStore = null;
    private EntityStore channelStore = null;
    private EntityStore channelNameStore = null;
    
    private PrimaryIndex<String, User> pIdx;
    private PrimaryIndex<String, HashEntity> pIdxHash;
    private PrimaryIndex<String, CrawlEntity> pIdxCrawl;
    private PrimaryIndex<String, ChannelEntity> pIdxChannel;
    private PrimaryIndex<String, ChannelNameEntity> pIdxChannelName;
    
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
            channelStore = new EntityStore(dbEnv, "channelStore", storeConfig);
            channelNameStore = new EntityStore(dbEnv, "channelNameStore", storeConfig);
            
            pIdx = dbStore.getPrimaryIndex(String.class,User.class);
            pIdxCrawl = crawlStore.getPrimaryIndex(String.class, CrawlEntity.class);
            pIdxHash = seenStore.getPrimaryIndex(String.class, HashEntity.class);
            pIdxChannel = channelStore.getPrimaryIndex(String.class, ChannelEntity.class);
            pIdxChannelName = channelNameStore.getPrimaryIndex(String.class, ChannelNameEntity.class);
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
    
    public EntityStore getChannelStore(){
        return channelStore;
    }
    public EntityStore getChannelNameStore(){
        return channelNameStore;
//        return null;
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
	
	public String addChannel(String channelName, String xpathExp, String firstname){
	    System.out.println("dd channe1");
	    if(channelName != null && xpathExp != null && !channelName.equals("")){
    	   System.out.println("dd channe2: " + pIdxChannelName);
    	   
    	   try{
    	       if(pIdxChannelName.get(channelName) != null){
       	            System.out.println("dd channe3");
                    return "already_exists";
               }
           }
           catch(Exception e){
	           System.out.println("dd channe3 EXCEPTION: "+ e);
           }

	       System.out.println("dd channe3");
           pIdxChannelName.put(new ChannelNameEntity(channelName, xpathExp, firstname));
           return "successful";
       }
       System.err.println("Cannot add channel - name null or empty");
       return "unsuccessful";
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
        channelNameStore.close();
        System.out.println("___________________________________________________________________");
	    try{
	        dbStore.close();
	    }
        catch(Exception e){
            System.err.println("StorageImpl - exception 0: " + e);
        }
        
        try{
	        seenStore.close();
	    }
        catch(Exception e){
            System.err.println("StorageImpl - exception 1: " + e);
        }
        
        try{
	        crawlStore.close();
	    }
        catch(Exception e){
            System.err.println("StorageImpl - exception 2: " + e);
        }
        
        try{
	        channelStore.close();
	    }
        catch(Exception e){
            System.err.println("StorageImpl - exception 3: " + e);
        }
        
	    
        try{
	        dbEnv.close();
        }
        catch(Exception e){
            System.err.println(e);
        }
	}
	
	public PrimaryIndex<String, CrawlEntity> getPIDxCrawl(){
	    return pIdxCrawl;
	}

	public PrimaryIndex<String, HashEntity> getPIDxHash(){
	    return pIdxHash;
	}
	
	public PrimaryIndex<String, ChannelEntity> getPIDxChannel(){
	    return pIdxChannel;
	}
	
	public PrimaryIndex<String, ChannelNameEntity> getPIDxChannelName(){
	    return pIdxChannelName;
	}	
	/*
	 * Get Channel xPath expresssions
	 */
    public String[] getExpressions(){
        if(pIdxChannelName == null)
        {
            return null;
        }
        ArrayList<String> expressions = new ArrayList<>();
        
        EntityCursor<ChannelNameEntity> cursor = pIdxChannelName.entities();
        
        try{
            Iterator<ChannelNameEntity> iter = cursor.iterator();
            while(iter.hasNext()){
                ChannelNameEntity ce = iter.next();
                expressions.add(ce.xpathExp);
            }
        }
        finally{
            cursor.close();
        }

        
        Object[] arr = expressions.toArray();
        String[] strExp = Arrays.copyOf(arr, arr.length, String[].class);   
        return strExp;
    }
    
    public String[] getChannelNames(){
        try{
            ArrayList<String> names = new ArrayList<>();
            
            EntityCursor<ChannelNameEntity> cursor = pIdxChannelName.entities();
            try{
                Iterator<ChannelNameEntity> iter = cursor.iterator();
//                for(ChannelNameEntity ce: pIdxChannelName.entities()){
                while(iter.hasNext()){
                    ChannelNameEntity ce = iter.next();
                    names.add(ce.channelName);
                }
            }
            finally{
                cursor.close();
            }

            Object[] arr = names.toArray();
            String[] strNames = Arrays.copyOf(arr, arr.length, String[].class);
            return strNames;
        }
        catch(Exception e){
            System.err.println(e);
        }
        return null;
    }
        
    public String[] getChannelUsernames(){
        ArrayList<String> names = new ArrayList<>();
        for(ChannelNameEntity ce: pIdxChannelName.entities()){
            names.add(ce.userFirstname);
        }
        Object[] arr = names.toArray();
        String[] strNames = Arrays.copyOf(arr, arr.length, String[].class);   
        return strNames;
    }
}