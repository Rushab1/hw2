package edu.upenn.cis.cis455;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase 
{
  public static Test suite() 
  {
    try {
      Class[]  testClasses = {
        /* TODO: Add the names of your unit test classes here */
         Class.forName("TestCrawlWorker"),
         Class.forName("TestCrawlRobots"),
         Class.forName("TestBolts"),
         Class.forName("TestXPath")
//         , Class.forName("TestStorageForCrawl"), Class.forName("TestStorageForLogin")
         
      };   
      
      return new TestSuite(testClasses);
    } catch(Exception e){
      e.printStackTrace();
    } 
    
    return null;
  }
}
