package cz.wincor.pnc.types;

import java.io.IOException;
import java.util.List;

import javax.swing.JTable;

import cz.wincor.pnc.cache.LevelDBCache;
import cz.wincor.pnc.cache.LogWrapperCacheItem;
import cz.wincor.pnc.gui.component.DragAndDropPanel;
import cz.wincor.pnc.gui.component.LogWrapperTableModel;

/**
 * @author bruno.gonzalez
 * 
 *         This class is the adapter between the Sessions list and the model for the JTable. Basically we deal with
 *         everything GUI dependent here, so that the data structure of sessions deals with keeping the messages.
 * 
 */

public class SessionsModelAdapter {


    public static LogWrapperTableModel getTableModel(JTable resultTable) {
        // this method creates the table model for the table showing the messages and shows a message on the
        // DragAndDropPanel when we hit the 50 mark

        LogWrapperTableModel model = null;
        // create the Sessions data structure
        try {
            if (Sessions.createSessions()) {
                // produce the model, each time we hit the 50 mark, show a message to DragAndDropPanel.
                //I was going to have a traverser class with a next() here, but I ended up wasting a 
                //lot of time trying to hold position across method calls  with 4 pointers. This is easier.
                model = (LogWrapperTableModel) resultTable.getModel();
                int increment = 0;
                for (Session session : Sessions.getSessions()) {
                    // new session to process
                    // set transaction list to the start of the sessions
                    for (Transaction transaction : session.getTransactions()) {
                        // new transaction to process
                        // set the messages list to the start of the messages list on this transaction
                        for (TransactionMessage transactionMesage : transaction.getTransactionMessages()) {
                            // I have ONE message, so process it.
                            String key = new String(transactionMesage.getKey());
                            LogWrapperCacheItem item = transactionMesage.getCacheItem();
                            model.addRow(item, key);
                            increment++;
                            if (increment > 50) {
                                DragAndDropPanel.getInstance().logToTextArea(".", false);
                                increment = 0;
                            }

                        }

                    }
                }

            }
        } catch (ClassNotFoundException e) {

            e.printStackTrace();
            return null;
        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
        return model;
    }

}
