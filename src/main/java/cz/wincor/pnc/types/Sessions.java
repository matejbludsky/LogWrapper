package cz.wincor.pnc.types;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import cz.wincor.pnc.cache.LevelDBCache;
import cz.wincor.pnc.cache.LogWrapperCacheItem;
import cz.wincor.pnc.gui.component.DragAndDropPanel;
import cz.wincor.pnc.gui.component.LogWrapperTableModel;
import cz.wincor.pnc.gui.jframe.MessagesReviewJFrame;


/**
 * @author bruno.gonzalez   
 * 
 *         Class contains the list of sessions that we'll use to populate the MessagesReviewJFrame
 * 
 */



public class Sessions {

    private static final Logger LOG = Logger.getLogger(MessagesReviewJFrame.class);
    
    private static List<Session> sessions = new LinkedList<Session>();

    public static List<Session> getSessions() {
        return sessions;
    }

    public static void setSessions(List<Session> sessions) {
        Sessions.sessions = sessions;
    }

    public static boolean createSessions()  throws ClassNotFoundException, IOException {
        //this method creates the data structures from the DB
        //The messages in the DB are not in sequence. So, I'll load the DB in a list, sort the list based on SErvertime, then use the list to create the sessions data structure
        //and put them in the JTable model.
        //This is only the same session if I have the same Workstaion ID. I need to create one session for each time I get a new message with a new Workstiation ID.
        //This means I need a list of sessions.
        DB db = null;
        DBIterator dbIterator = null;
        List<TransactionMessage> itemsList = new LinkedList<TransactionMessage>();
        try {
            db = LevelDBCache.getInstance().openDatabase();
            dbIterator = db.iterator();          
            for (dbIterator.seekToFirst(); dbIterator.hasNext(); dbIterator.next()) {
                //we are creating ONE Transaction object to hold every message of a session, later I'll create transactions based
                //on the actual transactions carried out by the messages.
                //create a session object
               
                String key = new String(dbIterator.peekNext().getKey());
                //get the message
                LogWrapperCacheItem item = LevelDBCache.getInstance().convertFromBytes(dbIterator.peekNext().getValue());
                TransactionMessage transactionMessage = new TransactionMessage(key,item );
                itemsList.add(transactionMessage);
            }
           
        } catch (Exception e) {
            LOG.error("Cannot close connection to DB", e);
            return false;
        } finally {
            dbIterator.close();
            db.close();
        }
        
        //sort the list using a comparison with no Lambda expression.
        Collections.sort(itemsList, new Comparator<TransactionMessage> () {
            public int compare(TransactionMessage transactionMessage1, TransactionMessage transactionMessage2) {
                //compare two TransactionMessages
                return transactionMessage1.getCacheItem().getServerDate().compareTo(transactionMessage2.getCacheItem().getServerDate());
            }
        });
        
        //now we have the sorted list, so fill up the JTable model.
        Session session = new Session();
        Transaction transaction = new Transaction();
        session.addTransaction(transaction);
        sessions.add(session);
        for (TransactionMessage transactionMessage : itemsList) {
            //we are creating ONE Transaction object to hold every message of a session, later I'll create transactions based
            //on the actual transactions carried out by the messages.
             
            //get the message to parse the message
            LogWrapperCacheItem item = transactionMessage.getCacheItem();
            addToSession(item, sessions);
        }
        return true;
    }
    
    
    
private static void addToSession(LogWrapperCacheItem item, List<Session> sessions) {
    //This method adds the message to one session, based on workstation ID, and the type of message. All messages are supoused to be in order 
    
    
    //look for workstationID in the sessions
    
    //                                                                                                                                                                                                                                                                                                                                                                       
    
    
        
    }

/*    for (TransactionMessage transactionMessage : itemsList) {
        //we are creating ONE Transaction object to hold every message of a session, later I'll create transactions based
        //on the actual transactions carried out by the messages.
         
        //get the message to parse the message
        LogWrapperCacheItem item = transactionMessage.getCacheItem();
        //get the workstation ID
        workStIdSession = item.getATMId();
        
        //now we have to parse and create the structure
        //while not a Login or Logout
        //WARNNING, we are just creating one transaction per session, but I'm laying out the structure for having several transactions per session.
        if (!isEndOfSession(item))  {
            //attach the message to current session
            //here we should have some magic to parse the messages and create transactions if we were doing it.
            //we are adding all messages to the same transaction for now. Later we will parse the transactions and get several transactions per session.
            transaction.addTransactionMessage(transactionMessage);             
        }
        
            else if (item.getMessage().contains("LogoutRequest")) {
            //attach message to current session, which means attach it to the current transaction
            transaction.addTransactionMessage(transactionMessage);             
            
            //for simplicity sake, just create a session and a transaction and add it. We make sure that any malformed sequence
            //gets the non end of session message (anything else than logout and login) in the next session.
            session = new Session();
            transaction = new Transaction();
            session.addTransaction(transaction);
            sessions.add(session); 
        }
      
        else if (item.getMessage().contains("LoginRequest")) {
            //again, for simplicity sake, I check if I have a session with a transaction with an empty list of messages, and then start adding them there.
            //if not, I just create a new session, transasction, and message and add it to sessions, just in case
            //correct way to solve all of this is to use state machines to keep track of the grammar
            if (transaction.getTransactionMessages().isEmpty()) {
                //we can use this transaction
                transaction.addTransactionMessage(transactionMessage);     
                session.addTransaction(transaction);
                sessions.add(session);                
            }
            else {
                session = new Session();
                transaction = new Transaction();
                transaction.addTransactionMessage(transactionMessage);     
                session.addTransaction(transaction);
                sessions.add(session);
            }
        }      
    }
    return true;
}*/
    
    
    
    private static boolean isEndOfSession(LogWrapperCacheItem item) {
        //this method seeks a Login or Logout message to mean that the session has finished. A Login means, that we didn't get a Logout message prior to it.
        //LOG.info("::Message::" + item.getMessage());
        System.out.println("MESSAGE" + item.getMessage());
        //if ( item.getMessage().contains("Login") || item.getMessage().contains("Logout")) {
        if ( item.getMessage().contains("LoginRequest")) {
            return true;
            
        }
        return false;
    }

 

}
