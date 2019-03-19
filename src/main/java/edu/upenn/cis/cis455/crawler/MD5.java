package edu.upenn.cis.cis455.crawler;

import java.math.BigInteger; 
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException; 

public class MD5 {
    //Taken from Geeks-to-Geeks
    public static String hash(byte[] b){
        try { 
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            byte[] messageDigest = md.digest(b); 
            BigInteger no = new BigInteger(1, messageDigest); 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            } 
            return hashtext; 
        }  
        catch (NoSuchAlgorithmException e) { 
            throw new RuntimeException(e); 
        } 
    }
}
