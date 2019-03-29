package edu.upenn.cis.cis455.storage;

public class StorageFactory {
    static StorageInterface storageInterface=null;
    
    //Pass directory = null if already initialized
    public static StorageInterface getDatabaseInstance(String directory, boolean IN_MEMORY) {
	// TODO: factory object, instantiate your storage server
	   if( storageInterface == null){
	       storageInterface = new StorageInterfaceImpl(directory, IN_MEMORY);
	   }
	   return storageInterface; 
    }
    
    //Pass directory = null if already initialized
    public static StorageInterface getDatabaseInstance(String directory) {
	// TODO: factory object, instantiate your storage server
	   if( storageInterface == null){
	       storageInterface = new StorageInterfaceImpl(directory);
	   }
	   return storageInterface; 
    }
}
