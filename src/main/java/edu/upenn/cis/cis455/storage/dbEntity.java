package edu.upenn.cis.cis455.storage;

import java.util.Map;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class dbEntity {
    private String encryptedPassword;
    private Map<String, String> data;
    private int id;
    
    @PrimaryKey
    private String username;
    
    public dbEntity(){}
    
    public dbEntity(int id, String username, String encryptedPassword){
        this.username = username;
        this.encryptedPassword = encryptedPassword;
//        data.put("firstName", firstName);
//        data.put("lastName", lastName);
        this.id = id;
    }
    
    public Map<String, String> getData(){
        return data;
    }
    
    public boolean checkPassword(String password){
        return password.equals(encryptedPassword);
    }
    
    public int getId(){
        return id;
    }
}
