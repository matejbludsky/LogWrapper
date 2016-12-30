package cz.wincor.pnc.cache;

import java.util.Map;

import javax.swing.JTable;

/**
 * @author matej.bludsky
 * 
 * 
 */

public class DataCache {

    public static DataCache instance = null;
    JTable table = null;
    private Map<String, String> cache;

    private DataCache() {
        super();
    }

    /**
     * returns instance of MessagesReviewJFrame
     * 
     * @return
     */
    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;

    }

    public JTable getTable() {
        return table;
    }

    public void setTable(JTable table) {
        this.table = table;
    }

    public Map<String, String> getCache() {
        return cache;
    }

    public void setCache(Map<String, String> cache) {
        this.cache = cache;
    }

}
