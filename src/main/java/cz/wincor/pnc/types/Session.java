package cz.wincor.pnc.types;

import java.util.LinkedList;
import java.util.List;


/**
 * @author bruno.gonzalez   
 * 
 *         Class contains the list of transactions that make up a session.
 * 
 */


public class Session {

    private List<Transaction> transactions;
    
    public Session() {    
        transactions = new LinkedList<Transaction>();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
       transactions.add(transaction);
    }
    
    
    
}
