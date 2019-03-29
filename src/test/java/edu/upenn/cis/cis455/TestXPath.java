package edu.upenn.cis.cis455;

import java.util.Arrays;

import org.junit.Test;

import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.XPathImpl;

public class TestXPath {
    @Test
    public void test(){
        XPathImpl x = new XPathImpl();
        String[] expressions = {
                            "/a/b/c",
                            "/a/b[contains(text(),\"someSubstring\")]",
                            "/a/b[contains(text(),\"someSubstring\")][contains(text(),\"blah\")][contains(text(),\"haaha\")]",
                            "/a/b/c[text()=\"theEntireText\"]",
                            "/a/b/c/foo[contains(text(),\"something\")][contains(text()=\"Rushab\")]/d[text()==\"lol\"]",
                            "/a/b/c[text() =   \"whiteSpacesShouldNotMatter\"]"
                            };
        
        x.setXPaths(expressions);
        
        String text = null;
        OccurrenceEvent[] ocStream = {
                                        new OccurrenceEvent("docID", "open", "a", text),
                                        new OccurrenceEvent("docID", "open", "b", text),
                                        new OccurrenceEvent("docID", "open", "c", text),
                                        new OccurrenceEvent("docID", "text", "c", "theEntireText"),
                                        new OccurrenceEvent("docID", "close", "c", text),
                                        new OccurrenceEvent("docID", "close", "b", text),
                                        
                                        new OccurrenceEvent("docID", "open", "b", text),
                                        new OccurrenceEvent("docID", "text", "b", "blah blah blah someSubstring   blah haaha"),
                                        new OccurrenceEvent("docID", "open", "c", text),
                                        new OccurrenceEvent("docID", "text", "c", "whiteSpacesShouldNotMatter"),
                                        new OccurrenceEvent("docID", "open", "foo", text),
                                        new OccurrenceEvent("docID", "text", "foo", "something of Rushab Munot "),
                                        
                                        new OccurrenceEvent("docID", "open", "d", text),
                                        new OccurrenceEvent("docID", "text", "d", "not lol"),
                                        new OccurrenceEvent("docID", "close", "d", text),
                                                                                
                                        new OccurrenceEvent("docID", "open", "d", text),
                                        new OccurrenceEvent("docID", "text", "d", "lol"),
                                        new OccurrenceEvent("docID", "close", "d", text),

                                        new OccurrenceEvent("docID", "close", "foo", text),
                                        new OccurrenceEvent("docID", "close", "c", text),
                                        new OccurrenceEvent("docID", "close", "b", text),
                                        new OccurrenceEvent("docID", "close", "a", text),
                                        };
        
        int cnt = 0;
        for(OccurrenceEvent e: ocStream){
            boolean[] valid = x.evaluateEvent(e);
//            Integer validInt[] = new Integer[expressions.length];
                 
            if(cnt < 2)
                assert(!valid[0] && !valid[1]  && !valid[2] && !valid[3] && !valid[4] && !valid[5]);
            else if(cnt < 3)
                assert(valid[0] && !valid[1]  && !valid[2] && !valid[3] && !valid[4] && !valid[5]);
            else if (cnt < 7)
                assert(valid[0] && !valid[1]  && !valid[2] && valid[3] && !valid[4] && !valid[5]);
            else if(cnt < 9)
                assert(valid[0] && valid[1]  && valid[2] && valid[3] && !valid[4] && !valid[5]);
            else if(cnt < 16)
                assert(valid[0] && valid[1]  && valid[2] && valid[3] && !valid[4] && valid[5]);
            else if(cnt >= 16)
                assert(valid[0] && valid[1]  && valid[2] && valid[3] && valid[4] && valid[5]);
                
//            for(int i=0; i<expressions.length;i++)
//            {
//                if(valid[i])
//                    validInt[i] = 1;
//                else
//                    validInt[i] = 0;
//                    
//            }
//            System.out.println(cnt+": "+Arrays.toString(validInt) + " : " + e.tag + "---" + e.eventType + "---" + e.text);
            cnt += 1;
        }
    }
}
