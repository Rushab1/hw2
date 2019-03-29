package edu.upenn.cis.cis455.crawler;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class ChannelNameEntity {
    public String xpathExp;
    public String userFirstname;//First Name of the user who created the channel
    @PrimaryKey
    public String channelName;
    
    public ChannelNameEntity(){
    }
    
    public ChannelNameEntity(String channelName, String xpathExp, String firstname){
        this.xpathExp = xpathExp;
        this.channelName = channelName;
        this.userFirstname = firstname;
    }
}