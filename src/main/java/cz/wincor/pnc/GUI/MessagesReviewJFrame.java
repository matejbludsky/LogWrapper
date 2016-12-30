package cz.wincor.pnc.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultCaret;

import org.apache.log4j.Logger;

import cz.wincor.pnc.GUI.MessageTypeManager.Message;
import cz.wincor.pnc.cache.DataCache;
import cz.wincor.pnc.common.ILogWrapperUIRenderer;
import cz.wincor.pnc.error.UIRenderException;
import cz.wincor.pnc.util.ImageUtil;
import cz.wincor.pnc.util.SystemUtil;
import cz.wincor.pnc.util.TraceStringUtils;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;

/**
 * @author matej.bludsky
 * 
 *         Singleton JFrame class for review of logs
 * 
 */

public class MessagesReviewJFrame extends JFrame implements ILogWrapperUIRenderer, WindowListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(MessagesReviewJFrame.class);

    private String[] columnNames = { "Included", "SEQNumber", "ServerTimeID", "ATMTime", "ATMID", "MessageName", "InfoTransaction", "ID" };
    private Map<String, String> cache;

    private JTable resultTable;
    private JTextPane dataPreview;

    public MessagesReviewJFrame() throws HeadlessException {
        LOG.info("Starting MessagesPanel");
        try {
            addWindowListener(this);
        } catch (Exception e) {
            LOG.error("Cannot render MessagesPanel", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void renderUI(Object... parameters) throws UIRenderException {
        if (parameters == null || parameters[0] == null) {
            LOG.error("No results to display");
            setVisible(false);
            throw new UIRenderException("No Results to display");
        }

        try {

            addMenu();
            setTitle("Message Review");
            cache = (Map<String, String>) parameters[0];
            setPreferredSize(new Dimension(1300, 800));
            setDefaultLookAndFeelDecorated(true);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(new Point((int) screenSize.getWidth() / 2 - 650, (int) screenSize.getHeight() / 2 - 400));

            renderTable();

            dataPreview = new JTextPane();
            dataPreview.setBackground(new Color(240, 240, 240));

            JScrollPane sp = new JScrollPane(dataPreview);
            DefaultCaret caret = (DefaultCaret) dataPreview.getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

            JScrollPane scrollPane = new JScrollPane(resultTable);

            loadContentFromCache(cache);
            DataCache.getInstance().setTable(resultTable);
            DataCache.getInstance().setCache(cache);

            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, sp);
            split.setDividerLocation(400);
            split.setResizeWeight(0.5);

            add(split, BorderLayout.CENTER);
            repaint();
            pack();

            setVisible(true);
            LOG.info("MessagesReviewJFrame Rendered and displayed");
        } catch (Exception e) {
            LOG.error("Cannot render MessagesReviewJFrame", e);
            throw new UIRenderException("Cannot render MessagesReviewJFrame", e);
        }

    }

    /**
     * Adds menu into JFrame
     */
    private void addMenu() {
        JMenuBar menu = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem imagesFolder = new JMenuItem("Open Image/s directory");
        imagesFolder.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        imagesFolder.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SystemUtil.openImagesLocation();
            }
        });

        JMenuItem exportSOAPUI = new JMenuItem("Export");
        exportSOAPUI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
        exportSOAPUI.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ExportJFrame.getInstance().renderUI();
                } catch (UIRenderException e1) {
                    LOG.error("Cannot render SOAPUIConversionJFrame");
                    DragAndDropPanel.logToTextArea("Cannot render SOAPUIConversionJFrame", true);
                }
            }
        });

        JMenuItem clipboard = new JMenuItem("Copy selected to clipboard");
        clipboard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
        clipboard.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                StringBuilder text = new StringBuilder();
                for (int row = 0; row < resultTable.getModel().getRowCount(); row++) {
                    if ((boolean) resultTable.getModel().getValueAt(row, 0)) {
                        text.append(SystemUtil.formatXML(cache.get(resultTable.getModel().getValueAt(row, 7))));
                    }
                }

                SystemUtil.copyToClipboard(text.toString());
            }
        });

        file.add(imagesFolder);
        file.add(exportSOAPUI);
        file.add(clipboard);
        menu.add(file);

        JMenu select = new JMenu("Select");
        JMenuItem selectAll = new JMenuItem("Select All");
        selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
        selectAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int row = 0; row < resultTable.getModel().getRowCount(); row++) {
                    resultTable.getModel().setValueAt(true, row, 0);
                }
                resultTable.repaint();
            }
        });

        JMenuItem selectSection = new JMenuItem("Select Current Section");
        selectSection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
        selectSection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int row = 0; row < resultTable.getRowCount(); row++) {
                    resultTable.getModel().setValueAt(true, resultTable.convertRowIndexToModel(row), 0);

                }
            }
        });

        JMenuItem unSelectAll = new JMenuItem("Unselect All");
        unSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
        unSelectAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int row = 0; row < resultTable.getModel().getRowCount(); row++) {
                    resultTable.getModel().setValueAt(false, row, 0);
                }
                resultTable.repaint();
            }
        });

        select.add(selectAll);
        select.add(selectSection);
        select.add(unSelectAll);
        menu.add(select);

        setJMenuBar(menu);
    }

    /**
     * Creates instance of the resultTable and renders its content
     */
    private void renderTable() {

        resultTable = new JTable() {
            private static final long serialVersionUID = 1L;

            /*
             * @Override public Class getColumnClass(int column) { return getValueAt(0, column).getClass(); }
             */
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                case 0:
                    return Boolean.class;
                case 2:
                    return Date.class;
                case 3:
                    return Date.class;
                case 6:
                    return Boolean.class;
                default:
                    return String.class;
                }
            }

        };
        LogWrapperTableModel model = new LogWrapperTableModel();
        model.setColumnIdentifiers(columnNames);
        resultTable.setModel(model);

        resultTable.getActionMap().put("copy", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rowsSelected = resultTable.getSelectedRows();
                StringBuilder builder = new StringBuilder();

                for (int i = 0; i < rowsSelected.length; i++) {
                    String key = (String) resultTable.getModel().getValueAt(i, 7);
                    builder.append(SystemUtil.formatXML(cache.get(key)));
                }

                SystemUtil.copyToClipboard(builder.toString());
                LOG.debug("Rows " + rowsSelected.toString() + " copied into clipboard");
            }
        });
        resultTable.setFillsViewportHeight(true);

        resultTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (resultTable.getSelectedRow() > -1 && !event.getValueIsAdjusting()) {
                    String keyID = resultTable.getValueAt(resultTable.getSelectedRow(), 7).toString();
                    prettyPrintMessageTextArea(keyID);
                    ImageUtil.saveImages();
                    LOG.debug("Row selected : " + keyID);
                    DragAndDropPanel.logToTextArea("Row selected : " + keyID, true);
                }
            }
        });

        resultTable.getColumnModel().getColumn(0).setPreferredWidth(15);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(20);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        resultTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        resultTable.getColumnModel().getColumn(5).setPreferredWidth(300);

        resultTable.getColumnModel().getColumn(7).setWidth(0);
        resultTable.getColumnModel().getColumn(7).setMinWidth(0);
        resultTable.getColumnModel().getColumn(7).setMaxWidth(0);

        TableRowSorter<LogWrapperTableModel> sorter = new TableRowSorter<LogWrapperTableModel>(model);
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));

        sorter.setSortKeys(sortKeys);
        resultTable.setRowSorter(sorter);
        resultTable.setDefaultRenderer(Date.class, new TableRenderer());
        resultTable.setDefaultRenderer(String.class, new TableRenderer());

        resultTable.getColumnModel().getColumn(5).setCellRenderer(new ColorRenderer());
        resultTable.getColumnModel().getColumn(1).setCellRenderer(new ColorRenderer());

        TableFilterHeader filterHeader = new TableFilterHeader(resultTable, AutoChoices.ENABLED);
    }

    /**
     * Adds message to the text area using pretty xml print
     * 
     * @param key
     */
    private void prettyPrintMessageTextArea(String key) {
        String messageToPrint = cache.get(key);
        dataPreview.setText(SystemUtil.formatXML(messageToPrint));
    }

    /**
     * Loads cache into the JTable
     * 
     * @param cache
     */
    private void loadContentFromCache(Map<String, String> cache) {

        DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
        for (Map.Entry<String, String> entry : cache.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            String key = entry.getKey();
            String message = entry.getValue();

            Object[] data = new Object[] { true, TraceStringUtils.extractSEQNumber(key, message), TraceStringUtils.getDateFormatDate(key), TraceStringUtils.extractDateDate(key, message), TraceStringUtils.extractATMID(message), TraceStringUtils.extractMessageType(key, message), TraceStringUtils.isInfoTransaction(key, message), key };

            model.addRow(data);
            LOG.debug("Row added : " + data.toString());
            DragAndDropPanel.logToTextArea("Message : " + data[1] + " detected", true);
        }

    }

    public Map<String, String> getCache() {
        return cache;
    }

    public void setCache(Map<String, String> cache) {
        this.cache = cache;
    }

    public JTable getResultTable() {
        return resultTable;
    }

    public void setResultTable(JTable resultTable) {
        this.resultTable = resultTable;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosing(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosed(WindowEvent e) {
        setVisible(false);
        LOG.info("MessagesReviewJFrame window closed");
        LogWrapperUIJFrame.getInstance().display();

        if (resultTable != null) {
            resultTable.removeAll();
        }
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowActivated(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * Custom table renderer that is setting horizontal alligment for some of the columns
     * 
     * @author matej.bludsky
     *
     */
    public class TableRenderer extends DefaultTableCellRenderer {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
        protected void setValue(Object value) {

            Object valueCopy = value;

            if (valueCopy == null) {
                valueCopy = "-";
            }
            if (value instanceof Date) {
                valueCopy = new String(TraceStringUtils.dateFormatter.format(value));

            }

            super.setValue(valueCopy);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);

            Date ATMDate = (Date) table.getModel().getValueAt(row, 3);
            if (ATMDate == null) {
                table.getModel().setValueAt(false, row, 0);
            }

            if (column == 5) {
                setHorizontalAlignment(LEFT);
            }

            return this;
        }

    }

    /**
     * 
     * @author matej.bludsky
     *
     *         Custom Table cell renderer thats adding color to rows specific
     */
    public class ColorRenderer extends DefaultTableCellRenderer {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private Map<String, Color> SEQNumber = new HashMap<>();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value == null) {
                return this;
            }

            switch (column) {
            case 1:

                if (value.toString().equals("-1") || value.toString().equals("-")) {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                    break;
                }

                if (SEQNumber.get((String) value) == null) {
                    SEQNumber.put((String) value, randomColor());
                }

                setBackground(SEQNumber.get((String) value));
                setForeground(Color.BLACK);
                break;
            case 5:
                Message settings = MessageTypeManager.fromString((String) value);
                setBackground(settings.getBackground());
                setForeground(settings.getForeground());
                break;

            default:
                break;
            }

            setHorizontalAlignment(CENTER);

            return this;
        }
    }

    /**
     * Returns random color thats not dark
     * 
     * @return
     */
    private Color randomColor() {
        return new Color(Color.HSBtoRGB((float) Math.random(), (float) Math.random(), 0.5F + ((float) Math.random()) / 2F));
    }

    @Override
    public void display() {
        repaint();
        setVisible(true);
    }

}
