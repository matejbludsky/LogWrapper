package cz.wincor.pnc.GUI;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import cz.wincor.pnc.cache.DataCache;
import cz.wincor.pnc.common.ILogWrapperUIRenderer;
import cz.wincor.pnc.error.UIRenderException;
import cz.wincor.pnc.export.SOAPUIExporter;
import cz.wincor.pnc.settings.LogWrapperSettings;
import cz.wincor.pnc.util.TraceStringUtils;
import cz.wincor.pnc.util.ImageUtil;
import cz.wincor.pnc.util.SystemUtil;

/**
 * @author matej.bludsky
 * 
 *         Singleton class JFrame for Export page
 * 
 */

// TODO add endpoint URL default value from settings file
public class ExportJFrame extends JFrame implements ILogWrapperUIRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static ExportJFrame instance = null;
    private static final Logger LOG = Logger.getLogger(ExportJFrame.class);

    private JProgressBar progressBar = new JProgressBar();

    private JCheckBox WAS;
    private JCheckBox JBOSS;

    /**
     * Returns instance of SOAPUIConversionJFrame
     * 
     * @return
     */
    public static ExportJFrame getInstance() {
        if (instance == null) {
            instance = new ExportJFrame();
        }
        return instance;

    }

    private ExportJFrame() {
        LOG.info("Starting SOAPUIConversionJFrame");
    }

    @Override
    public void renderUI(Object... parameters) throws UIRenderException {
        LOG.info("Rendering SOAPUIConversionJFrame");

        setPreferredSize(new Dimension(400, 300));
        setResizable(false);
        setTitle("Export");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setLocation(new Point((int) screenSize.getWidth() / 2 - 200, (int) screenSize.getHeight() / 2 - 125));

        final JPanel mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(450, 300));

        BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS);

        mainPanel.add(getSettingsPanel(), layout);

        JTextField urlTextFiled = new JTextField();
        urlTextFiled.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField field = (JTextField) e.getSource();
                LogWrapperSettings.ENDPOINT_URL = field.getText();
                LOG.info("Endpoint URL changed to " + field.getText());
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });

        urlTextFiled.setUI(new HintTextField(" Default " + LogWrapperSettings.ENDPOINT_URL, true));
        urlTextFiled.setPreferredSize(new Dimension(350, 35));
        Border urlTextFiledBorder = BorderFactory.createTitledBorder("Endpoint URL");
        urlTextFiled.setBorder(urlTextFiledBorder);

        mainPanel.add(urlTextFiled);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(350, 60));
        progressBar.setValue(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
        Border border = BorderFactory.createTitledBorder("Transforming...");
        progressBar.setBorder(border);
        mainPanel.add(progressBar, layout);
        mainPanel.add(Box.createRigidArea(new Dimension(450, 20)));
        mainPanel.add(getButtonPanel(), layout);

        getContentPane().add(mainPanel);
        pack();
        setVisible(true);
        LOG.info("Rendered SOAPUIConversionJFrame");
    }

    /**
     * Creates panel that has radio buttons on it for WAS or JBOSS option
     * 
     * @return
     */
    private JPanel getSettingsPanel() {

        final JPanel settings = new JPanel();
        settings.setPreferredSize(new Dimension(390, 80));
        settings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Settings"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        BoxLayout layoutMain = new BoxLayout(settings, BoxLayout.LINE_AXIS);

        WAS = new JCheckBox("WAS");
        WAS.setName("WAS");
        WAS.setPreferredSize(new Dimension(100, 20));
        WAS.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                LogWrapperSettings.ENDPOINT_WAS = box.isSelected();
                if (box.isSelected()) {
                    JBOSS.setSelected(false);
                }
                LOG.debug("Endpoint selected WAS : " + box.isSelected());
            }
        });

        JBOSS = new JCheckBox("JBOSS");
        JBOSS.setName("JBOSS");
        JBOSS.setPreferredSize(new Dimension(100, 20));
        JBOSS.setSelected(true);
        JBOSS.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                LogWrapperSettings.ENDPOINT_JBOSS = box.isSelected();
                if (box.isSelected()) {
                    WAS.setSelected(false);
                }

                LOG.debug("Endpoint selected JBOSS : " + box.isSelected());
            }
        });

        settings.add(WAS, layoutMain);
        settings.add(JBOSS, layoutMain);

        return settings;
    }

    /**
     * Creates panel with Buttons for functionality trigger
     * 
     * @return
     */
    private JPanel getButtonPanel() {
        final JPanel buttons = new JPanel();
        buttons.setPreferredSize(new Dimension(390, 100));

        BoxLayout layout = new BoxLayout(buttons, BoxLayout.LINE_AXIS);

        JButton create = new JButton("Create SOAPUI project");
        create.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateProgress(0, false);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e2) {
                            // TODO Auto-generated catch block
                            e2.printStackTrace();
                        }
                        String filePath = null;
                        /* if soapui.project.location set do not display file chooser */
                        if (LogWrapperSettings.SOAPUI_FINAL_LOCATION.isEmpty()) {
                            JFileChooser chooser = new JFileChooser();
                            chooser.setAcceptAllFileFilterUsed(false);
                            chooser.setFileFilter(new FileFilter() {

                                public String getDescription() {
                                    return "XML files (*.xml)";
                                }

                                public boolean accept(File f) {
                                    if (f.isDirectory()) {
                                        return true;
                                    } else {
                                        String filename = f.getName().toLowerCase();
                                        return filename.endsWith(".xml");
                                    }
                                }
                            });
                            FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files", "xml");
                            chooser.setFileFilter(filter);
                            int returnVal = chooser.showOpenDialog(instance);
                            if (returnVal == JFileChooser.APPROVE_OPTION) {

                                if (chooser.getSelectedFile() == null) {
                                    LOG.info("File not supported");
                                    DragAndDropPanel.logToTextArea("Cannot save to selected file", true);
                                    return;
                                }
                                filePath = chooser.getSelectedFile().getAbsolutePath();
                            }
                        }

                        LOG.debug("Using final path for soap ui :" + filePath);
                        LOG.debug("loading cache");
                        // load cache from table
                        List<String> messagesCache = prepareCacheForSoapUIProcessor();
                        // transform to soap ui

                        Thread thread = new Thread(new SOAPUIExporter(messagesCache));
                        thread.start();
                        SystemUtil.openSoapUIFinalLocation();
                    }
                });
            }
        });

        buttons.add(create, layout);
        JButton images = new JButton("Extract Images");
        images.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateProgress(0, false);
                        Thread thread = new Thread(new ImageThread());
                        thread.start();
                    }
                });
            }
        });

        buttons.add(images, layout);
        return buttons;

    }

    /**
     * Class for image export running in thread
     * 
     * @author matej.bludsky
     *
     */
    public class ImageThread implements Runnable {

        @Override
        public void run() {
            List<String> messages = new ArrayList<>();
            List<String> savedFiles = new ArrayList<>();
            for (int row = 0; row < DataCache.getInstance().getTable().getModel().getRowCount(); row++) {
                if ((boolean) DataCache.getInstance().getTable().getModel().getValueAt(row, 0)) {
                    String message = DataCache.getInstance().getCache().get(DataCache.getInstance().getTable().getModel().getValueAt(row, 7));
                    messages.add(message);
                }
            }
            int increment = 100;
            if (messages.size() != 0) {
                increment = 100 / messages.size();
            }

            for (String message : messages) {
                savedFiles.addAll(ImageUtil.saveImagesToFile(message));
                updateProgress(increment, true);
            }
            updateProgress(100, false);
            if (!savedFiles.isEmpty()) {
                SystemUtil.openImagesLocation();
            } else {
                JOptionPane.showMessageDialog(ExportJFrame.getInstance(), "No images found in the selected messages", "Warning", JOptionPane.WARNING_MESSAGE);
                DragAndDropPanel.logToTextArea("No images found for export", true);
            }
        }
    }

    /**
     * Methods updates progress bar , if increment true, the value will be incremented
     * 
     * @param value
     * @param increment
     */
    public void updateProgress(int value, boolean increment) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (increment) {
                    progressBar.setValue(progressBar.getValue() + value);
                } else {
                    progressBar.setValue(value);
                }
            }
        });
    }

    /**
     * Methods returns list of messages that should be converted to SOAPUI these are selected from the table as check
     * box included in column 0 is enabled
     * 
     * @return
     */
    private List<String> prepareCacheForSoapUIProcessor() {
        List<String> tmpCache = new ArrayList<String>();
        List<String> messagesCache = new ArrayList<String>();

        for (int row = 0; row < DataCache.getInstance().getTable().getModel().getRowCount(); row++) {
            if ((boolean) DataCache.getInstance().getTable().getModel().getValueAt(row, 0)) {
                tmpCache.add(DataCache.getInstance().getTable().getModel().getValueAt(row, 7).toString() + "&" + DataCache.getInstance().getCache().get(DataCache.getInstance().getTable().getModel().getValueAt(row, 7).toString()));
            }
        }

        // entry in key&message
        // sort by date
        Comparator<String> comp = (String a, String b) -> {

            try {
                String A = a.substring(0, a.indexOf("&"));
                String B = b.substring(0, b.indexOf("&"));

                Date dateA = TraceStringUtils.getDateFormatDate(A);
                Date dateB = TraceStringUtils.getDateFormatDate(B);

                if (dateB.before(dateA)) {
                    return 1;
                } else {
                    return -1;
                }
            } catch (Exception e) {
                LOG.error("Cannot compare date", e);
            }

            return 0;
        };

        Collections.sort(tmpCache, comp);

        // remove key from the list

        for (Iterator<String> iterator = tmpCache.iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            string = string.substring(string.indexOf("&") + 1, string.length());
            messagesCache.add(string);
        }

        return messagesCache;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void display() {
        repaint();
        setVisible(true);
    }

}
