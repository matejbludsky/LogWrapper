package cz.wincor.pnc.types;

import java.util.LinkedList;
import java.util.List;

import cz.wincor.pnc.cache.LogWrapperCacheItem;


/**
 * @author bruno.gonzalez   
 * 
 *         Class contains the list of TransactionMessages that make up a transaction.
 * 
 */





public class Transaction {

    private List<TransactionMessage> transactionMessages;
    
    public Transaction() {
        transactionMessages = new LinkedList<TransactionMessage>();
    }

    public void addTransactionMessage(TransactionMessage item) {
        transactionMessages.add(item);
        
    }
    
    public List<TransactionMessage> getTransactionMessages() {
        return transactionMessages;
    }
   
    
    
}
