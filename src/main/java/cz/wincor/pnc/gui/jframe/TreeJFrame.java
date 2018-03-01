package cz.wincor.pnc.gui.jframe;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import cz.wincor.pnc.cache.LevelDBCache;
import cz.wincor.pnc.cache.LogWrapperCacheItem;
import cz.wincor.pnc.gui.component.DragAndDropPanel;

import org.apache.log4j.Logger;

import javax.swing.JTree;

public class TreeJFrame extends JFrame {

    private JPanel contentPane;

    private String mode = null;
    private String keyID = null;

    private JTree tree = new JTree();

    private LevelDBCache cache;

    private static final Logger LOG = Logger.getLogger(MessagesReviewJFrame.class);

    /**
     * Create the frame.
     */
    public TreeJFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
    }

    private void createSessionTree(String keyID) {
        // create a tree with the transaction for this message

        // To test, just create a Jtree with one element, the message from the messages list
        // find the element in the DB.
        DefaultMutableTreeNode root = null;

        root = new DefaultMutableTreeNode("Session");
        // get the list of messages for now:
        try {
            List<DefaultMutableTreeNode> messages = getMessages(keyID);
        } catch (IOException e) {
            // we could not get the ATM ID, so exit
            LOG.error("", e);
            JOptionPane.showMessageDialog(this, "Cannot create the session tree " + e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        }

        tree = new JTree(root);
        // getItemFromCache(keyID).getMessage()
        contentPane.add(tree, BorderLayout.CENTER);
    }

    private List<DefaultMutableTreeNode> getMessages(String keyID) throws IOException {
        // getItemFromCache(keyID).getMessage()
        // construct a list of the messages in the session of this message
        // I need the Workstation ID, the session starts at the message: LOGIN on this workstation ID prior to this
        // message.

        // construct an iterator
        DBIterator iterator = null;

        // point the iterator on this message
        DB db = cache.openDatabase();
        DBIterator iteratorForward = db.iterator();
        DBIterator iteratorBackwards = db.iterator();

        // positioning to item before the selected one JUST TO GET the ATM ID, no way to get the value of the current
        // without using next()
        iteratorBackwards.seek(keyID.getBytes());
        iteratorBackwards.prev();
        LogWrapperCacheItem dbItem;
        String workstationId;
        try {
            dbItem = LevelDBCache.getInstance().convertFromBytes(iteratorBackwards.peekNext().getValue());
            workstationId = dbItem.getATMId();

            // go backwards adding any messages until I get a LOGIN or LOGOUT for this Workstation ID in a ListArray the
            // messages to be able to go in inverse order after it.
            // careful if it is already a LOGIN message
            for (iteratorBackwards.seek(keyID.getBytes()); !isStartOfSession(iteratorBackwards, workstationId); iteratorBackwards.prev()) {
                // we are positioning now

            }

            // got forward until I get a LOGUT or a LOGIN message for this workstation.
            for (iteratorForward.seek(keyID.getBytes()); !isStartOfSession(iteratorForward, workstationId); iteratorForward.next()) {
                // we are positioning now

            }

            // the list of messages is to be displayed.
            return null;

        } catch (ClassNotFoundException | IOException e) {
            // we could not get the ATM ID, so exit
            LOG.error("", e);
            JOptionPane.showMessageDialog(this, "Cannot create the session tree " + e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            return null;

        } finally {
            iteratorForward.close();
            iteratorBackwards.close();
            db.close();
        }

    }

    private boolean isStartOfSession(DBIterator iteratorBackwards, String workstationId) {
        return rootPaneCheckingEnabled;
        // returns true only if we have a LOGIN or LOGOUT message for this workstation ID

    }

    private void createTansactionTree(String keyID) {
        // Create a tree with the session for this message.

        // To test, just create a Jtree with one element, the message from the messages list
        // find the element in the list
        DefaultMutableTreeNode root = null;
        try {
            root = new DefaultMutableTreeNode(getItemFromCache(keyID).getMessage());
            tree = new JTree(root);
            contentPane.add(tree, BorderLayout.CENTER);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LOG.error("Cannot create the tree", e);
        }

    }

    public void showTree(String keyID, String mode, LevelDBCache cache) {
        this.mode = mode;
        this.keyID = keyID;
        this.cache = cache;

        // we need to look for either session or transaction for this message
        if (mode.equalsIgnoreCase("session")) {
            createSessionTree(keyID);
        } else if (mode.equalsIgnoreCase("transaction")) {
            createTansactionTree(keyID);
        }
    }

    private LogWrapperCacheItem getItemFromCache(String keyID) throws IOException {
        return LevelDBCache.getInstance().get(keyID);
    }

    
    
}
