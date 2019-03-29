package edu.upenn.cis.cis455.xpathengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis.cis455.crawler.ChannelEntity;
import edu.upenn.cis.cis455.model.OccurrenceEvent;

public class XPathImpl implements XPathEngine{
    boolean[] valid;
    String[] channelNames;
    String[] expressions;
    String curExpression = "";
    
    ArrayList<String> curTags;
    ArrayList<String> curText;
    
    Map <String, Map<Integer, Boolean>> previousMatchesOfCurExp = new HashMap<>();
    PrimaryIndex<String, ChannelEntity> pIdxChannel;
    
    public XPathImpl(PrimaryIndex<String, ChannelEntity> pIdxChannel){
        this.pIdxChannel = pIdxChannel;
    }
    
    public XPathImpl(){}
    
    public void setXPaths(String[] expressions){
        this.expressions = expressions;
        
        if(expressions == null){
            this.expressions = new String[0];
            return;
        }
        valid = new boolean[expressions.length];
        previousMatchesOfCurExp = new HashMap<>();
        
        for(String expression: expressions){
            previousMatchesOfCurExp.put(expression.toLowerCase(), new HashMap<>());
        }
    }
    
    public boolean isValid(int i){
        if(valid[i])
            return true;
        return false;
    }
    
    public Boolean matchTextAndContains(String text, String exp){
        String[] t = exp.split("\"");
        
        t[0] = t[0].replaceAll("[ ]+", "");
        
        if(t[0].startsWith("text")){
            if(t.length != 2){
                return null;
            }
//            System.out.println("MATCH TEXT: " + exp + "--" + t[1]  + "--" + text.equals(t[1]) + "--" + "-----" + text);
            if(text.equals(t[1]))
                return true;
            return false;
                
        }
        else if(t[0].startsWith("contains")){
            if(t.length != 3){
                return null;
            }
//            System.out.println("MATCH CONTAINS: " + exp + "--" + t[1] + "--" + text.contains(t[1])+ "-----" + text);
            if(text.contains(t[1]))
                return true;
            return false;
        }
        return null;
    }
    
    /*
    *Match text with expressions of the type 
    *[exp]+ 
    *exp ->  text() = "something"
    *exp -> contains[text(), "somthing"]
    */
    public boolean matchText(String text, String expression){
        int i;
        char c;
        for(i=0; i<expression.length();i++){
            c = expression.charAt(i);
            if(c == '[')
                break;
        }
        
        i += 1;
        Boolean b = true;
        while(i < expression.length()){
            String curExp = "";
            c = expression.charAt(i);
            while(i<expression.length()-1 && c!=']'){
                curExp += c;
                i += 1;
                c = expression.charAt(i);
            }
            Boolean tmp = matchTextAndContains(text, curExp);
            
            if (tmp == null){
                System.out.println(expression + " Invalid Expression");
                return false;
            }
            b = b & tmp;
            
            if( i== expression.length()-1)
                return b;
            else{
                i +=1;
                c = expression.charAt(i);
            }

            
            while(c == ' '){
                i += 1;
                c = expression.charAt(i);
            }
            
            if(i < expression.length() && c != '['){
                return false;
            }

            i +=1;
        }
        return b;
    }

public boolean matchXPath(String hypo, String xpathExp, String text, String eventType){
//        System.out.println("1===" + eventType + "==" + hypo );
        String[] spl = xpathExp.split("/");
        String last = spl[spl.length-1];
        String ref;

        ref = xpathExp.replaceAll("\\[.*?\\]", "").trim();
        hypo = hypo.trim();        

        
        int hypoDepth = hypo.split("/").length;
        
        if( eventType.equals("text") && ref.startsWith(hypo) && ref.length() > hypo.length())
        {
            String refShort = String.join("/", Arrays.copyOfRange(xpathExp.split("/"), 0, hypoDepth) );
            String[] splShort = refShort.split("/");
            String lastShort = splShort[splShort.length - 1];
            
            if(text != null){
                previousMatchesOfCurExp.get(xpathExp).put(hypoDepth, matchText(text, lastShort));
            }
            return false;
        }
        
        if(eventType.equals("text") && last.contains("[") && last.contains("]")){
            ref = xpathExp.replaceAll("\\[.*?\\]", "").trim();
            hypo = hypo.trim();

            if(!ref.equals(hypo))
                return false;

            if(text != null){
                boolean tmp = true;
                for(Integer i: previousMatchesOfCurExp.get(xpathExp).keySet()){
                    if(i <= hypoDepth && previousMatchesOfCurExp.get(xpathExp).get(i) != null){
                        tmp = tmp & previousMatchesOfCurExp.get(xpathExp).get(i);
                    }
                }

                    
                return tmp & matchText(text, last);
            }    
            return false;
        }
        else if(!last.contains("[") && !last.contains("]")){
            ref = xpathExp.replaceAll("\\[.*?\\]", "").trim();
            boolean tmp = true;
                        
            for(Integer i: previousMatchesOfCurExp.get(xpathExp).keySet()){
                if(i <= hypoDepth && previousMatchesOfCurExp.get(xpathExp).get(i) != null){
                    tmp = tmp & previousMatchesOfCurExp.get(xpathExp).get(i);
                }
            }
            return tmp & ref.equals(hypo);
        }
        
        return false;
    }
    
    /*
     * Case insensitive matches for OccurenceEvent
     */
    public boolean[] evaluateEvent(OccurrenceEvent e){
        if(e.eventType == "open"){
            curExpression += "/" + e.tag;
        }
        else if(e.eventType == "close"){
            int lastSlash = curExpression.lastIndexOf("/");
            if(lastSlash != -1)
                curExpression = curExpression.substring(0, lastSlash);
        }
        else if(e.eventType == "text"){
        }
        
//        System.out.println("3=====" + curExpression + "-" + e.eventType +"-" + e.tag + "-" +expressions.length);

        for(int i = 0 ; i < expressions.length; i++){
            String expression = expressions[i];
            if(! valid[i]){
                String text = e.text;
                if(text != null){
                    text = text.toLowerCase();
                }
                valid[i] = valid[i] || matchXPath(curExpression.toLowerCase(), expression.toLowerCase(), text, e.eventType);
            }

        }
        return valid;
    }
    
    public void updateDB(String docId, boolean[] valid){
        ChannelEntity ce = new ChannelEntity(docId, valid);
        pIdxChannel.put(ce);
    }
    
    public boolean[] getValid(){
        return valid;
    }
}
