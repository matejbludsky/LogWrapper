package cz.wincor.pnc.gui.component;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import cz.wincor.pnc.cache.LogWrapperCacheItem;

/**
 * @author matej.bludsky
 * 
 *         custom table model that allows edit only for first column
 */

public class LogWrapperTableModel extends DefaultTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isCellEditable(int row, int column) {

        if (column == 0) {
            // only include column editable
            return true;
        }
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        Vector rowVector = (Vector) dataVector.elementAt(row);
        rowVector.setElementAt(aValue, column);
    }

    public void addRow(LogWrapperCacheItem item, String key) {
        Object[] data = new Object[] { false, item.getSEQNumber(), item.getServerDate(), item.getClientDate(), item.getATMId(),item.getHostTrxMessageType(), item.getMessageType(), false, key };

        super.addRow(data);
    }

}
