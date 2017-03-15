package cz.wincor.pnc.types;

import java.io.IOException;
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
        DB db = null;
        DBIterator iterator = null;
        try {
            db = LevelDBCache.getInstance().openDatabase();
            iterator = db.iterator();          
            Session session = new Session();
            Transaction transaction = new Transaction();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                //we are creating ONE Transaction object to hold every message of a session, later I'll create transactions based
                //on the actual transactions carried out by the messages.
                //create a session object
               
                String key = new String(iterator.peekNext().getKey());
                //get the message
                LogWrapperCacheItem item = LevelDBCache.getInstance().convertFromBytes(iterator.peekNext().getValue());
                
                //now we have to parse and create the structure
                //while not a Login or Logout
                //WARNNING, we are just creating one transaction per session, but I'm laying out the structure for having several transactions per session.
                while (!isEndOfSession(item) && iterator.next() != null) {
                    //attach the message to current session
                    //here we should have some magic to parse the messages and create transactions if we were doing it.
                    //we are adding all messages to the same transaction for now. Later we will parse the transactions and get several transactions per session.
                    //we need to add a key and the message
                    TransactionMessage transactionMessage = new TransactionMessage(key, item);
                    transaction.addTransactionMessage(transactionMessage);                             
                    //get new message
                     key = new String(iterator.peekNext().getKey());
                    //get the message
                     item = LevelDBCache.getInstance().convertFromBytes(iterator.peekNext().getValue());      
                     
                }
                
                if (item.getMessage().contains("Logout")) {
                    //if Logout
                    //attach message to current session, which means attach it to the current transaction
                    //we need to add the key and the message
                    TransactionMessage transactionMessage = new TransactionMessage(key, item);
                    transaction.addTransactionMessage(transactionMessage);             
                    session.addTransaction(transaction);
                    sessions.add(session);
                    session = new Session();
                    transaction = new Transaction();
                }
              
                else if (item.getMessage().contains("Login")) {
                    //if Login
                    //create a new Session and attach the message to it, which means attach it to the current transaction             
                    //we add the current transaction to the sessions list and start all over again.
                    session.addTransaction(transaction);
                    sessions.add(session);    
                    session = new Session();
                    transaction = new Transaction();
                    //we need to add the key and the message
                    TransactionMessage transactionMessage = new TransactionMessage(key, item);
                    transaction.addTransactionMessage(transactionMessage);             
                }
                
                else if (iterator.next() == null) {
                    //we got a message with no closing logout message
                    TransactionMessage transactionMessage = new TransactionMessage(key, item);
                    transaction.addTransactionMessage(transactionMessage);             
                    session.addTransaction(transaction);
                    sessions.add(session);
                    session = new Session();
                    transaction = new Transaction();
                }
                
            }
            return true;
        } catch (Exception e) {
            LOG.error("Cannot close connection to DB", e);
            return false;
        } finally {
            iterator.close();
            db.close();
        }
    }
    
    private static boolean isEndOfSession(LogWrapperCacheItem item) {
        //this method seeks a Login or Logout message to mean that the session has finished. A Login means, that we didn't get a Logout message prior to it.
        //LOG.info("::Message::" + item.getMessage());
        System.out.println("MESSAGE" + item.getMessage());
        if ( item.getMessage().contains("Login") || item.getMessage().contains("Logout")) {
            return true;
            
        }
        return false;
    }

    public static LogWrapperTableModel getTableModel() {
        //this method creates a LogWrapperTableModel on one go, using recursion and gives it back.
        return null;
    }

}
