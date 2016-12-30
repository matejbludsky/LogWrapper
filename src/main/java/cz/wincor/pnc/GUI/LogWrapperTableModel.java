package cz.wincor.pnc.GUI;

import javax.swing.table.DefaultTableModel;

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

}
