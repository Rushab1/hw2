package edu.upenn.cis.cis455.crawler;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class ChannelEntity {
    public boolean[] valid;
    
    @PrimaryKey
    public String docId;
    
    public ChannelEntity(){
    }
    
    public ChannelEntity(String docId, boolean[] valid){
        this.valid = valid;
        this.docId = docId;
    }
}