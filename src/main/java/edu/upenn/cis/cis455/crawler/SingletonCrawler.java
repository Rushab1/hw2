package edu.upenn.cis.cis455.crawler;

import java.util.ArrayList;

import edu.upenn.cis.cis455.storage.StorageInterface;

public class SingletonCrawler {
    private static Crawler crawler = null;
    public static ArrayList<String> indexed = new ArrayList<>();
    public static StorageInterface db;
    public static String[] expressions=null; // cannot change during crawler run
    
    private static int bufferSize = 0;
    
    public static void initializeCrawler(String startUrl, StorageInterface database, int size, int count){
        if(crawler == null){
            db = database;
            crawler = new Crawler(startUrl, db, size, count);
        }
    }
    
    public static Crawler getInstance(){
            return crawler;
    }
    
    public static String[] getExpressions(){
        if(expressions == null)
            return db.getExpressions();
        return expressions;
    }
    
    public static int getbufferSize(){
        return bufferSize;
    }
    
    public static void incBufferSize(){
        bufferSize = bufferSize + 1;
    }
    
    public static void decBufferSize(){
        bufferSize = bufferSize - 1;
    }
}
