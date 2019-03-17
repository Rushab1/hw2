package edu.upenn.cis.cis455.storage;

public class StorageFactory {
    static StorageInterface storageInterface=null;
    
    public static StorageInterface getDatabaseInstance(String directory) {
	// TODO: factory object, instantiate your storage server
	   if( storageInterface == null){
	       storageInterface = new StorageInterfaceImpl(directory);
	   }
        
	   return storageInterface; 
    }
}
