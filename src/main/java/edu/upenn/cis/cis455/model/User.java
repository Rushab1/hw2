package edu.upenn.cis.cis455.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis.cis455.crawler.SHA_256;

@Entity
public class User implements Serializable {
    private String userName, firstName, lastName;
    private String encryptedPassword;
    private Map<String, String> data = new HashMap<String,String>();
    private int id;
    
    @PrimaryKey
    private String username;
    
    public User(){}
    
    public User(int id, String username, String encryptedPassword, String firstName, String lastName){
        try{
            this.username = username;
            this.encryptedPassword = SHA_256.hash(encryptedPassword);

            data.put("firstName", firstName);
            data.put("lastName", lastName);

            
            this.id = id;

            
        }
        catch(Exception e){
            System.out.println("USER : " + e);
        }

    }
    
    public Map<String, String> getData(){
        return data;
    }
    
    public boolean checkPassword(String password){
        String pass = SHA_256.hash(password);
        return pass.equals(encryptedPassword);
    }
    
    public Integer getUserId() {
        return id;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getPassword() {
        return encryptedPassword;
    }
}
