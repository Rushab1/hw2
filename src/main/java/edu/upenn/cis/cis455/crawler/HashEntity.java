package edu.upenn.cis.cis455.crawler;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class HashEntity {
    public String md5;
    
    @PrimaryKey
    public String link;
    
    public HashEntity(){
    }
    
    public HashEntity(String link, String md5){
        this.link = link;
        this.md5 = md5;
    }
}