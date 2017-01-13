package cz.wincor.pnc.gui.jframe;

import java.awt.Dimension;
import java.awt.Label;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import cz.wincor.pnc.common.ILogWrapperUIRenderer;
import cz.wincor.pnc.error.UIRenderException;
import cz.wincor.pnc.settings.LogWrapperSettings;

/**
 * @author matej.bludsky
 * 
 * 
 */

public class AppSettingsJFrame extends JFrame implements ILogWrapperUIRenderer {

    /**
     * 
     */

    private static AppSettingsJFrame instance = null;

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(AppSettingsJFrame.class);

    public static AppSettingsJFrame getInstance() {
        if (instance == null) {
            instance = new AppSettingsJFrame();
        }

        return instance;
    }

    private AppSettingsJFrame() {
        LOG.debug("Starting AppSettingsJFrame");
    }

    @Override
    public void renderUI(Object... parameters) throws UIRenderException {
        setTitle("Application settings");
        setPreferredSize(new Dimension(350, 480));
        setDefaultLookAndFeelDecorated(true);
        setResizable(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(new Point((int) screenSize.getWidth() / 2 - 175, (int) screenSize.getHeight() / 2 - 240));

        getContentPane().add(getMainPanel());
        pack();
        setVisible(true);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Settings"), BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);

        Label levelDB = new Label("LevelDB Cache Size in MB");
        levelDB.setPreferredSize(new Dimension(300, 20));

        JTextField levelDBCache = new JTextField();
        levelDBCache.setText(Integer.toString(LogWrapperSettings.LEVEL_DB_CACHE_ALLOCATION));
        levelDBCache.setPreferredSize(new Dimension(300, 20));
        levelDBCache.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField source = (JTextField) e.getSource();
                int cache = Integer.parseInt(source.getText());
                LogWrapperSettings.LEVEL_DB_CACHE_ALLOCATION = cache;
                LOG.debug("LMDB_ALLOCATION changed to : " + cache);
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }
        });

        levelDBCache.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
                    getToolkit().beep();
                    e.consume();
                }
            }
        });

        Label commTraceNamesLabel = new Label("CommTrace file names filter (regexp)");
        commTraceNamesLabel.setPreferredSize(new Dimension(300, 20));

        JTextField commTraceNames = new JTextField(LogWrapperSettings.COMMTRACE_NAME_REGEXP);
        commTraceNames.setPreferredSize(new Dimension(300, 20));
        commTraceNames.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField source = (JTextField) e.getSource();
                LogWrapperSettings.COMMTRACE_NAME_REGEXP = source.getText();
                LOG.debug("COMMTRACE_NAMES changed to : " + source.getText());
            }
        });

        Label traceNamesLabel = new Label("TraceLog file names filter (regexp)");
        traceNamesLabel.setPreferredSize(new Dimension(300, 20));

        JTextField traceNames = new JTextField(LogWrapperSettings.TRACE_NAME_REGEXP);
        traceNames.setPreferredSize(new Dimension(300, 20));
        traceNames.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField source = (JTextField) e.getSource();
                LogWrapperSettings.TRACE_NAME_REGEXP = source.getText();
                LOG.debug("TRACE_NAME_REGEXP changed to : " + source.getText());

            }
        });

        Label imagesLocationLabel = new Label("Image output location");
        imagesLocationLabel.setPreferredSize(new Dimension(300, 20));

        JTextField imagesLocation = new JTextField(LogWrapperSettings.IMAGES_LOCATION);
        imagesLocation.setPreferredSize(new Dimension(300, 20));
        imagesLocation.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                JTextField source = (JTextField) evt.getSource();
                LogWrapperSettings.IMAGES_LOCATION = source.getText();
            }
        });

        JRadioButton imagesClear = new JRadioButton("Clear images location on startup");
        imagesClear.setPreferredSize(new Dimension(300, 20));
        imagesClear.setSelected(LogWrapperSettings.IMAGES_CLEAR_ON_START);
        imagesClear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JRadioButton source = (JRadioButton) e.getSource();
                LogWrapperSettings.IMAGES_CLEAR_ON_START = source.isSelected();
            }
        });

        Label soapuiProjectLocationLabel = new Label("Soapui project output location");
        soapuiProjectLocationLabel.setPreferredSize(new Dimension(300, 20));

        JTextField soapuiProjectLocation = new JTextField(LogWrapperSettings.SOAPUI_FINAL_LOCATION);
        soapuiProjectLocation.setPreferredSize(new Dimension(300, 20));

        soapuiProjectLocation.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField source = (JTextField) e.getSource();
                LogWrapperSettings.SOAPUI_FINAL_LOCATION = source.getText();

            }
        });

        JRadioButton soapuiOverwrite = new JRadioButton("Delete soap ui directory before transformation");
        soapuiOverwrite.setPreferredSize(new Dimension(300, 20));
        soapuiOverwrite.setSelected(LogWrapperSettings.SOAP_CLEAR_BEFORE);
        soapuiOverwrite.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JRadioButton source = (JRadioButton) e.getSource();
                LogWrapperSettings.SOAP_CLEAR_ON_START = source.isSelected();
            }
        });

        JRadioButton soapuiClear = new JRadioButton("Clear soap ui location on startup");
        soapuiClear.setPreferredSize(new Dimension(300, 20));
        soapuiClear.setSelected(LogWrapperSettings.SOAP_CLEAR_ON_START);
        soapuiClear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JRadioButton source = (JRadioButton) e.getSource();
                LogWrapperSettings.SOAP_CLEAR_ON_START = source.isSelected();
            }
        });

        JButton save = new JButton("Save");
        save.setPreferredSize(new Dimension(180, 30));
        save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                LogWrapperSettings.saveSettings();
                AppSettingsJFrame.getInstance().setVisible(false);
                LogWrapperUIJFrame.getInstance().display();
            }
        });

        mainPanel.add(levelDB, layout);
        mainPanel.add(levelDBCache, layout);
        mainPanel.add(commTraceNamesLabel, layout);
        mainPanel.add(commTraceNames, layout);
        mainPanel.add(traceNamesLabel, layout);
        mainPanel.add(traceNames, layout);
        mainPanel.add(imagesLocationLabel, layout);
        mainPanel.add(imagesLocation, layout);
        mainPanel.add(imagesClear, layout);
        mainPanel.add(soapuiProjectLocationLabel, layout);
        mainPanel.add(soapuiProjectLocation, layout);
        mainPanel.add(soapuiOverwrite, layout);
        mainPanel.add(soapuiClear, layout);
        mainPanel.add(Box.createRigidArea(new Dimension(250, 20)));
        mainPanel.add(save, layout);

        return mainPanel;
    }

    @Override
    public void display() {
        repaint();
        setVisible(true);
    }

}
