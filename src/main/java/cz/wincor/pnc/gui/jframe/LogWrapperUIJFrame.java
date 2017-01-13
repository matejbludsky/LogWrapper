package cz.wincor.pnc.gui.jframe;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import cz.wincor.pnc.common.ILogWrapperUIRenderer;
import cz.wincor.pnc.error.FileImportException;
import cz.wincor.pnc.error.UIRenderException;
import cz.wincor.pnc.gui.component.DragAndDropPanel;
import cz.wincor.pnc.gui.component.SettingsPanel;
import cz.wincor.pnc.importer.FileImporter;
import cz.wincor.pnc.settings.LogWrapperSettings;

/**
 * 
 * @author matej.bludsky
 *
 *         Singleton class for main JFrame
 *
 */
public class LogWrapperUIJFrame extends JFrame implements ILogWrapperUIRenderer, WindowListener {

    public static LogWrapperUIJFrame instance = null;

    private static final Logger LOG = Logger.getLogger(LogWrapperUIJFrame.class);
    public static int WIDTH = 500;
    public static int HEIGHT = 676;

    private JLabel systemDetails = new JLabel();

    private JPanel mainPanel;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * returns instance of LogWrapperUIJFrame
     * 
     * @return
     */
    public static LogWrapperUIJFrame getInstance() {
        if (instance == null) {
            instance = new LogWrapperUIJFrame();
        }
        return instance;

    }

    private LogWrapperUIJFrame() {
        LOG.info("Starting LogWrapperUIJFrame");

    }

    /**
     * Renders the JFrame UI
     */
    @Override
    public void renderUI(Object... parameters) throws UIRenderException {

        try {
            setResizable(false);
            setTitle("Log Wrapper");
            setDefaultLookAndFeelDecorated(true);
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            addWindowListener(this);

            JMenuBar menu = new JMenuBar();
            JMenu file = new JMenu("File");
            JMenuItem open = new JMenuItem("Open");

            open.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setMultiSelectionEnabled(true);
                    Component frame = null;
                    chooser.showOpenDialog(frame);
                    File[] files = chooser.getSelectedFiles();

                    try {
                        FileImporter importProcessor = new FileImporter();
                        importProcessor.importFiles(Arrays.asList(files));
                    } catch (FileImportException e1) {
                        LOG.error("Cannot import file/s : " + files.toString());
                        DragAndDropPanel.getInstance().logToTextArea("Cannot import file/s : " + Arrays.toString(files), true);
                    }

                }
            });

            JMenuItem about = new JMenuItem("About");
            about.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    AboutMeJFrame about = new AboutMeJFrame();
                    try {
                        about.renderUI();
                    } catch (UIRenderException e1) {
                        LOG.error("Cannot render AboutMeFrame ");
                        DragAndDropPanel.getInstance().logToTextArea("Cannot display About ", true);
                    }

                }
            });

            JMenuItem settings = new JMenuItem("Settings");
            settings.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        AppSettingsJFrame.getInstance().renderUI();
                    } catch (UIRenderException e1) {
                        LOG.error("Cannot render AppSettingsJFrame");
                    }
                }
            });

            file.add(open);
            file.add(about);
            file.add(settings);
            menu.add(file);

            setJMenuBar(menu);

            renderMainPanel();
            getContentPane().add(mainPanel);
            pack();

            setVisible(true);
        } catch (Exception e) {
            LOG.error("Cannot render LogWrapperUIJFrame", e);
            throw new UIRenderException("Cannot render LogWrapperUIJFrame", e);
        }
    }

    /**
     * Renders main panel
     * 
     * @throws UIRenderException
     */
    private void renderMainPanel() throws UIRenderException {
        mainPanel = new JPanel();
        SettingsPanel settingsPanel = new SettingsPanel();
        DragAndDropPanel dragAndDropPanel = DragAndDropPanel.getInstance();

        BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS);

        settingsPanel.renderUI();
        dragAndDropPanel.renderUI();

        JPanel systemPanel = new JPanel();
        systemPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Monitoring"), BorderFactory.createEmptyBorder(0, 10, 0, 10)));

        systemDetails.setForeground(Color.RED);

        systemPanel.setPreferredSize(new Dimension(WIDTH, 50));
        systemPanel.add(systemDetails);

        mainPanel.add(settingsPanel);
        mainPanel.add(systemPanel);
        mainPanel.add(dragAndDropPanel);

    }

    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * Clear files on closing based on settings
     */
    @Override
    public void windowClosing(WindowEvent e) {
        try {
            FileUtils.deleteDirectory(new File(LogWrapperSettings.TMP_LOCATION));
            LOG.info(" Deleted directory : " + LogWrapperSettings.TMP_LOCATION);
        } catch (IOException e1) {
            LOG.error("Cannot delete tmp directory", e1);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // TODO Auto-generated method stub

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

    @Override
    public void display() {
        repaint();
        setVisible(true);
    }

    public synchronized void refreshSystemDetails(String details) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                systemDetails.setText(details);
            }
        });
    }
}
