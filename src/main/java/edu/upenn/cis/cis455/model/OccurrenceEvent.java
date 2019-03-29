package edu.upenn.cis.cis455.model;

import java.util.ArrayList;

/**
 * TODO: this class encapsulates the data from a keyword "occurrence"
 */
public class OccurrenceEvent {
    public String documentId;
    public String eventType;
    public String tag;
    public String text;
    
    public OccurrenceEvent(String documentId, String eventType, String tag, String text){
        this.documentId = documentId;
        this.tag = tag;
        this.eventType = eventType;
        this.text = text;
    }
}
