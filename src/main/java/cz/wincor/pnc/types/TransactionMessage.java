package cz.wincor.pnc.types;

import cz.wincor.pnc.cache.LogWrapperCacheItem;

/**
 * @author bruno.gonzalez   
 * 
 *         Class contains the pair <key, message> that make up a line on the DB used for storing these.
 * 
 */

public class TransactionMessage {

    
    private String key;
    private LogWrapperCacheItem cacheItem;
    
    public TransactionMessage (String key, LogWrapperCacheItem item) {
        this.key=key;
        cacheItem=item;
    
    }

    public String getKey() {
        return key;
    }

   

    public LogWrapperCacheItem getCacheItem() {
        return cacheItem;
    }

  
    
    
    
    
    
}
