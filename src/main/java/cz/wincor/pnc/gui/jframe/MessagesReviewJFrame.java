package cz.wincor.pnc.gui.jframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.text.PlainDocument;

import org.apache.log4j.Logger;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLStyleConstants;
import org.imgscalr.Scalr;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import cz.wincor.pnc.cache.LevelDBCache;
import cz.wincor.pnc.cache.LogWrapperCacheItem;
import cz.wincor.pnc.common.ILogWrapperUIRenderer;
import cz.wincor.pnc.error.UIRenderException;
import cz.wincor.pnc.gui.component.DragAndDropPanel;
import cz.wincor.pnc.gui.component.ImageJLabel;
import cz.wincor.pnc.gui.component.LogWrapperTableModel;
import cz.wincor.pnc.types.MessageTypeManager;
import cz.wincor.pnc.types.MessageTypeManager.Message;
import cz.wincor.pnc.types.SessionsModelAdapter;
import cz.wincor.pnc.types.TransactionMessage;
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

public class MessagesReviewJFrame extends JFrame implements ILogWrapperUIRenderer, WindowListener, ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(MessagesReviewJFrame.class);

    private String[] columnNames = { "Included", "SEQNumber", "ServerTimeID", "ATMTime", "ATMID", "MessageType", "MessageName", "InfoTransaction", "ID" };

    private double zoom = 1.0; // zoom factor
    private JTable resultTable;
    private JEditorPane dataPreview;
    private JPanel imageView;
    private List<String> activeImages = new ArrayList<>();

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
        try {

            DragAndDropPanel.getInstance().logToTextArea("Initializing Preview", true);

            addMenu();
            setTitle("Message Review");
            setPreferredSize(new Dimension(1300, 800));
            setDefaultLookAndFeelDecorated(true);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(new Point((int) screenSize.getWidth() / 2 - 650, (int) screenSize.getHeight() / 2 - 400));

            renderTable();

            JScrollPane previewScrollablePane = new JScrollPane(RenderPreviewPanel());
            previewScrollablePane.getVerticalScrollBar().setUnitIncrement(20);

            JScrollPane tableScrollablePane = new JScrollPane(resultTable);
            tableScrollablePane.getVerticalScrollBar().setUnitIncrement(40);


            loadContentFromSessionList();

            //This is  the call from before, I'm using the call loadContentFromSessionList()
            //loadContentFromCache();


            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollablePane, previewScrollablePane);
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
     * Method is rendering preview area panel
     * 
     * @return
     */
    private JPanel RenderPreviewPanel() {
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BorderLayout());

        JPanel functionPanel = new JPanel();
        functionPanel.setLayout(new BorderLayout());

        JPanel actionPanel = new JPanel();
        actionPanel.setPreferredSize(new Dimension(450, 60));
        actionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Action"), BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JButton reset = new JButton("Discard Changes");
        reset.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!dataPreview.getText().isEmpty()) {
                    reloadPreviewContent(resultTable.getSelectedRow(), false);
                }
            }
        });

        JButton save = new JButton("Save Altered XML");
        save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String key = (String) resultTable.getValueAt(resultTable.getSelectedRow(), 7);
                LogWrapperCacheItem item = LevelDBCache.getInstance().get(key);
                item.setMessage(dataPreview.getText());
                try {
                    LevelDBCache.getInstance().put(key, item);
                } catch (IOException e1) {
                    LOG.error("Cannot save", e1);
                    JOptionPane.showMessageDialog(DragAndDropPanel.getInstance(), "Cannot save altered XML", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        actionPanel.add(reset);
        actionPanel.add(save);

        functionPanel.add(actionPanel, BorderLayout.NORTH);

        imageView = new JPanel();
        imageView.setBackground(new Color(240, 240, 240));
        imageView.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Image Preview"), BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        functionPanel.add(imageView, BorderLayout.CENTER);

        dataPreview = new JEditorPane();

        final XMLEditorKit kit = new XMLEditorKit();
        kit.setTagCompletion(true);
        kit.setAutoIndentation(true);

        dataPreview.setFont(new Font("Monospace", Font.PLAIN, 14));
        dataPreview.getDocument().putProperty(PlainDocument.tabSizeAttribute, new Integer(4));
        // Set style
        kit.setStyle(XMLStyleConstants.ATTRIBUTE_NAME, Color.GREEN.darker(), Font.PLAIN);
        kit.setStyle(XMLStyleConstants.ATTRIBUTE_VALUE, Color.MAGENTA.darker(), Font.PLAIN);
        kit.setStyle(XMLStyleConstants.COMMENT, Color.GRAY, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.DECLARATION, Color.DARK_GRAY, Font.BOLD);
        kit.setStyle(XMLStyleConstants.ELEMENT_NAME, Color.BLUE, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.ELEMENT_PREFIX, Color.BLUE, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.ELEMENT_VALUE, Color.BLACK, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.NAMESPACE_NAME, Color.GREEN.darker(), Font.PLAIN);
        kit.setStyle(XMLStyleConstants.NAMESPACE_VALUE, Color.MAGENTA.darker(), Font.PLAIN);
        kit.setStyle(XMLStyleConstants.NAMESPACE_PREFIX, Color.GREEN.darker(), Font.PLAIN);
        kit.setStyle(XMLStyleConstants.SPECIAL, Color.BLACK, Font.PLAIN);

        dataPreview.setEditorKit(kit);
        dataPreview.setEditable(true);

        dataPreview.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                dataPreview.setBackground(new Color(240, 240, 240));
            }

            @Override
            public void focusGained(FocusEvent e) {
                dataPreview.setBackground(Color.WHITE);

            }
        });

        dataPreview.setBackground(new Color(240, 240, 240));
        dataPreview.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Preview"), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        previewPanel.add(functionPanel, BorderLayout.WEST);
        previewPanel.add(dataPreview, BorderLayout.CENTER);

        return previewPanel;
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
                    ExportJFrame export = new ExportJFrame(resultTable.getModel());
                    export.renderUI();
                } catch (UIRenderException e1) {
                    LOG.error("Cannot render SOAPUIConversionJFrame");
                    DragAndDropPanel.getInstance().logToTextArea("Cannot render SOAPUIConversionJFrame", true);
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
                    if ((boolean) resultTable.getValueAt(row, 0)) {
                        text.append(SystemUtil.formatXML(LevelDBCache.getInstance().get(resultTable.getValueAt(row, 7).toString()).getMessage()));
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
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        LogWrapperTableModel model = (LogWrapperTableModel) resultTable.getModel();
                        for (int row = 0; row < model.getRowCount(); row++) {
                            model.setValueAt(true, row, 0);
                        }
                        model.fireTableDataChanged();
                    }
                });
            }
        });

        JMenuItem selectSection = new JMenuItem("Select Current Section");
        selectSection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
        selectSection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        LogWrapperTableModel model = (LogWrapperTableModel) resultTable.getModel();
                        int[] selectedRows = resultTable.getSelectedRows();
                        for (int i = 0; i < selectedRows.length; i++) {
                            model.setValueAt(true, resultTable.convertRowIndexToModel(selectedRows[i]), 0);
                        }
                        model.fireTableDataChanged();
                    }
                });
            }
        });

        JMenuItem unSelectAll = new JMenuItem("Unselect All");
        unSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
        unSelectAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        LogWrapperTableModel model = (LogWrapperTableModel) resultTable.getModel();
                        for (int row = 0; row < model.getRowCount(); row++) {
                            model.setValueAt(false, row, 0);
                        }
                        model.fireTableDataChanged();

                    }
                });
            }
        });

        select.add(selectAll);
        select.add(selectSection);
        select.add(unSelectAll);
        menu.add(select);

        setJMenuBar(menu);
    }

    /**
     * Reloads preview panel with row content
     * 
     * @param row
     * @param saveImage
     */
    private void reloadPreviewContent(int row, boolean saveImage) {
        // a right click does not select anything in the table, but it still gets us here so we cannot use:
        // resultTable.getSelectedRow()
        if (row >= 0) {
            //this was the issue that a right click would not work, I'm not sure which call is the correct one.
            //String keyID = resultTable.getValueAt(row, 7).toString();

            String keyID = resultTable.getValueAt(resultTable.getSelectedRow(), 8).toString();

            prettyPrintMessageTextArea(keyID);
            if (saveImage) {
                activeImages = ImageUtil.saveImages(keyID);

                if (!activeImages.isEmpty()) {
                    loadImagesToPreviewArea();
                }
            }
            LOG.debug("Row selected : " + keyID);
        }
    }

    /**
     * Method is loading images from file and constructing preview for images
     */
    private void loadImagesToPreviewArea() {

        imageView.removeAll();

        int counter = 0;
        for (String imagePath : activeImages) {
            counter++;
            LOG.debug("Previewing image : " + imagePath);
            try {
                FileInputStream in = new FileInputStream(imagePath);
                FileChannel channel = in.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
                channel.read(buffer);
                BufferedImage image = ImageUtil.loadTIFFImage(buffer.array());
                BufferedImage thumbnail = Scalr.resize(image, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, 400, 200, Scalr.OP_ANTIALIAS);
                BufferedImage thumbnail2 = Scalr.resize(image, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, 800, 600, Scalr.OP_ANTIALIAS);

                ImageJLabel imageLabel = new ImageJLabel(thumbnail2, new ImageIcon(thumbnail));

                imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {

                        ImageJLabel originalLabel = (ImageJLabel) evt.getSource();
                        ImageJLabel imageLabel = new ImageJLabel(originalLabel.getImage(), new ImageIcon(originalLabel.getImage()));
                        imageLabel.addMouseWheelListener(new MouseWheelListener() {
                            public void mouseWheelMoved(MouseWheelEvent e) {
                                ImageJLabel label = (ImageJLabel) e.getSource();
                                int notches = e.getWheelRotation();
                                double temp = zoom - (notches * 0.2);
                                // minimum zoom factor is 1.0
                                temp = Math.max(temp, 1.0);
                                if (temp != zoom) {
                                    zoom = temp;
                                    label.setIcon(new ImageIcon(ImageUtil.resizeImage(zoom, label.getImage())));
                                }
                            }
                        });

                        JFrame imageFrame = new JFrame("Image Detail");

                        JScrollPane jScrollPane = new JScrollPane(imageLabel);

                        imageLabel.addMouseListener(new MouseAdapter() {

                            @Override
                            public void mousePressed(MouseEvent e) {
                                imageLabel.setInitialX(e.getX());
                                imageLabel.setInitialY(e.getY());
                            }
                        });

                        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {

                            @Override
                            public void mouseDragged(MouseEvent e) {
                                int thisX = imageLabel.getLocation().x;
                                int thisY = imageLabel.getLocation().y;

                                int xMoved = (thisX + e.getX()) - (thisX + imageLabel.getInitialX());
                                int yMoved = (thisY + e.getY()) - (thisY + imageLabel.getInitialY());

                                int X = thisX + xMoved;
                                int Y = thisY + yMoved;

                                imageLabel.setLocation(X, Y);
                                imageLabel.repaint();
                            }
                        });

                        imageFrame.getContentPane().add(jScrollPane, BorderLayout.CENTER);
                        imageFrame.pack();
                        imageFrame.setLocationRelativeTo(null);
                        imageFrame.setVisible(true);

                    }
                });

                imageLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Integer.toString(counter)), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

                BoxLayout imageViewLayout = new BoxLayout(imageView, BoxLayout.Y_AXIS);
                imageView.setLayout(imageViewLayout);
                imageView.add(imageLabel);

            } catch (Exception e) {
                LOG.error("Cannot preview image", e);
            }

        }

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
                case 7:
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
                    String key = (String) resultTable.getValueAt(rowsSelected[i], 7);
                    builder.append(SystemUtil.formatXML(LevelDBCache.getInstance().get(key).getMessage()));
                }

                SystemUtil.copyToClipboard(builder.toString());
                LOG.debug("Rows " + Arrays.toString(rowsSelected) + " copied into clipboard");
            }
        });
        resultTable.setFillsViewportHeight(true);

        // create the popup menu and add the listener
        JMenuItem menuItem;

        // Create the popup menu.
        JPopupMenu popup = new JPopupMenu();
        menuItem = new JMenuItem("Show the session of this message");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramActionEvent) {
                // We handle the pop up session listener
                // get the menu item clicked.
                //JMenuItem source = (JMenuItem) (paramActionEvent.getSource());
                //get the session for this message from the table
                int rowSelected = resultTable.getSelectedRow();
                //int rowAtPoint = resultTable.rowAtPoint(SwingUtilities.convertPoint(popup, new Point(0, 0), resultTable));
                String keyID = resultTable.getValueAt(rowSelected, 7).toString();
                LOG.debug("Row selected : " + keyID);
                //open a dialog to show the session in a tree.GUI.
                TreeJFrame treeFrame = new TreeJFrame();
                treeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                treeFrame.pack();
                treeFrame.setVisible(true);
                
            }
        });
        popup.add(menuItem);
        menuItem = new JMenuItem("Show the transaction of this message");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramActionEvent) {
                // We handle the pop up session listener
                // get the menu item clicked.
                //JMenuItem source = (JMenuItem) (paramActionEvent.getSource());
                //get the session for this message from the table
                int rowSelected = resultTable.getSelectedRow();
                //int rowAtPoint = resultTable.rowAtPoint(SwingUtilities.convertPoint(popup, new Point(0, 0), resultTable));
                //get the item cache key
                String keyID = resultTable.getValueAt(rowSelected, 7).toString();
                LOG.debug("Row selected : " + keyID);
                //open a dialog to show the session in a tree.GUI.
                TreeJFrame treeFrame = new TreeJFrame();
                treeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                treeFrame.pack();
                treeFrame.setVisible(true);
            }
        });
        popup.add(menuItem);

        MouseListener popupListener = new PopupListener(popup);
        resultTable.addMouseListener(popupListener);

        resultTable.getColumnModel().getColumn(0).setPreferredWidth(15);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(20);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        resultTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        resultTable.getColumnModel().getColumn(5).setPreferredWidth(10);
        resultTable.getColumnModel().getColumn(6).setPreferredWidth(300);
        resultTable.getColumnModel().getColumn(8).setWidth(0);
        resultTable.getColumnModel().getColumn(8).setMinWidth(0);
        resultTable.getColumnModel().getColumn(8).setMaxWidth(0);

        TableRowSorter<LogWrapperTableModel> sorter = new TableRowSorter<LogWrapperTableModel>(model);
        // add a comparator to the TableRowSorter to sort by dates. Right now it sorts in ascending, but there is no
        // date related sort.
        // We assume that column 2 is date.
      
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));

        //on 2 columns set  
        sorter.setComparator(2, (final Date o1, final Date o2) -> Long.compare(o1.getTime(), o2.getTime()) );  

        sorter.setSortKeys(sortKeys);

        resultTable.setRowSorter(sorter);
        resultTable.setDefaultRenderer(Date.class, new TableRenderer());
        resultTable.setDefaultRenderer(String.class, new TableRenderer());

        resultTable.getColumnModel().getColumn(6).setCellRenderer(new ColorRenderer());
        resultTable.getColumnModel().getColumn(1).setCellRenderer(new ColorRenderer());

        TableFilterHeader filterHeader = new TableFilterHeader(resultTable, AutoChoices.ENABLED);
    }

    /**
     * Adds message to the text area using pretty xml print
     * 
     * @param key
     */
    private void prettyPrintMessageTextArea(String key) {
        String messageToPrint = LevelDBCache.getInstance().get(key).getMessage();
        dataPreview.setText(SystemUtil.formatXML(messageToPrint.trim()));
        dataPreview.setCaretPosition(0);
    }

    /**
     * Loads cache into the JTable
     * 
     * @param cache
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void loadContentFromCache() throws ClassNotFoundException, IOException {

        DragAndDropPanel.getInstance().logToTextArea("Loading data into preview", true);

        LogWrapperTableModel model = (LogWrapperTableModel) resultTable.getModel();
        int increment = 0;
        DB db = null;
        DBIterator iterator = null;
        try {
            db = LevelDBCache.getInstance().openDatabase();
            iterator = db.iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = new String(iterator.peekNext().getKey());
                LogWrapperCacheItem item = LevelDBCache.getInstance().convertFromBytes(iterator.peekNext().getValue());
                model.addRow(item, key);
                increment++;
                if (increment > 50) {
                    DragAndDropPanel.getInstance().logToTextArea(".", false);
                    increment = 0;
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot close connection to DB", e);
            JOptionPane.showMessageDialog(this, "Cannot import messages " + e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);

        } finally {
            iterator.close();
            db.close();
        }

    }

    /**
     * Loads the messages on the JTable
     * 
     * @param cache
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void loadContentFromSessionList() throws ClassNotFoundException, IOException {

        DragAndDropPanel.getInstance().logToTextArea("Loading data into preview", true);

        LogWrapperTableModel model = (LogWrapperTableModel) resultTable.getModel();
        try {
            // use recursion to create it from the session list
            // we use a SessionsModelAdapter to handle the specifics of the JTable without interfiering with the actual
            // data structure.
            // It will deal also with showing the message at the DragAndDropPanel when we hit the increment of 50.
            model = SessionsModelAdapter.getTableModel(resultTable);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Cannot import messages " + e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);

        } finally {

        }

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
        setVisible(false);
        dispose();
        LOG.info("MessagesReviewJFrame window closed");
        LogWrapperUIJFrame.getInstance().display();
    }

    @Override
    public void windowClosed(WindowEvent e) {

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
                valueCopy = new String(new SimpleDateFormat(TraceStringUtils.DATE_FORMAT_OUTPUT).format(value));

            }

            super.setValue(valueCopy);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);

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
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

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
            case 6:

                if (TraceStringUtils.isRequestMessage((String) value)) {
                    Message settings = MessageTypeManager.fromString((String) value);
                    setBackground(settings.getBackground());
                    setForeground(settings.getForeground());
                } else {
                    // response
                    setBackground(new Color(153, 255, 255));
                    setForeground(Color.BLACK);
                    Font font = new Font("Verdana", Font.PLAIN, 12);
                    Map attributes = c.getFont().getAttributes();
                    //attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                    c.setFont(new Font(attributes));
                }
                break;

            default:
                break;
            }
            setHorizontalAlignment(CENTER);
            return c;
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

    @Override
    public void actionPerformed(ActionEvent paramActionEvent) {
        // We handle the pop up menu events here.
        // get the menu item clicked.
        JMenuItem source = (JMenuItem) (paramActionEvent.getSource());
        String s = source.getName();
        LOG.info("Source: " + s);

    }

    class PopupListener extends MouseAdapter {
        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            int row = resultTable.rowAtPoint(evt.getPoint()); 
            reloadPreviewContent(row, true);
        }
        
        @Override
        public void mousePressed(MouseEvent evt) {
            int row = resultTable.rowAtPoint(evt.getPoint());
            // Get the ListSelectionModel of the JTable
            ListSelectionModel model = resultTable.getSelectionModel();
            // set the selected interval of rows. Using the "row"
            // variable for the beginning and end selects only that one row.
            model.setSelectionInterval( row, row );     
            maybeShowPopup(evt);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            int row = resultTable.rowAtPoint(evt.getPoint());
            // Get the ListSelectionModel of the JTable
            ListSelectionModel model = resultTable.getSelectionModel();
            // set the selected interval of rows. Using the "row"
            // variable for the beginning and end selects only that one row.
            model.setSelectionInterval( row, row );     
            maybeShowPopup(evt);
        }

        private void maybeShowPopup(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                popup.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }

}
