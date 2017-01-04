package cz.wincor.pnc.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Label;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import org.apache.log4j.Logger;

import cz.wincor.pnc.common.ILogWrapperUIRenderer;
import cz.wincor.pnc.error.UIRenderException;
import cz.wincor.pnc.importer.FileImporter;

/**
 * 
 * @author matej.bludsky
 *
 *         Panel implementation with Drag and Drop feature for log files to be dropped inside the application itself
 * 
 * 
 */
public class DragAndDropPanel extends JPanel implements ILogWrapperUIRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(DragAndDropPanel.class);

    public static DragAndDropPanel instance;

    public static final String UNSUPPORTED = "Unsupported file/s : ";
    public static final String SUPPORTED = "Supported file/s : ";

    private static JTextArea logArea;
    private static JProgressBar progressBar;

    public static synchronized DragAndDropPanel getInstance() {
        if (instance == null) {
            instance = new DragAndDropPanel();
        }
        return instance;
    }

    private DragAndDropPanel() {
        LOG.info("Starting Drag and Drop Panel");
        logArea = new JTextArea();
    }

    /**
     * Drop target for files being dropped into application frame
     */
    private DropTarget dropTarget = new DropTarget() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Drop listener to import dropped files
         */
        @SuppressWarnings("unchecked")
        @Override
        public void drop(DropTargetDropEvent dtde) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            List<File> droppedFiles = null;
            try {
                droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                FileImporter importProcessor = new FileImporter();
                importProcessor.importFiles(droppedFiles);
            } catch (Exception e) {
                LOG.error("Cannot receive file drop", e);
            }
        }
    };

    /**
     * Repaint area for each message displayed
     */
    private PropertyChangeListener changeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JTextArea area = (JTextArea) evt.getSource();
            area.repaint();
        }
    };

    @Override
    public void renderUI(Object... parameters) throws UIRenderException {
        try {

            setPreferredSize(new Dimension(LogWrapperUIJFrame.WIDTH, 410));
            setBackground(new Color(190, 190, 190));

            JScrollPane scrollPane = new JScrollPane(logArea);
            scrollPane.setPreferredSize(new Dimension(470, 300));

            DefaultCaret caret = (DefaultCaret) logArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            logArea.setDropTarget(dropTarget);
            logArea.setBackground(new Color(190, 190, 190));

            logArea.setEditable(false);
            logArea.addPropertyChangeListener(changeListener);

            logArea.setMargin(new Insets(5, 5, 5, 5));

            Label importText = new Label("Drag and Drop CommTrace file/s", Label.CENTER);
            importText.setPreferredSize(new Dimension(400, 60));

            add(importText, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            prepareProgressBar();
            add(progressBar, BorderLayout.SOUTH);

            setVisible(true);

        } catch (Exception e) {
            LOG.error("Cannot render component" + e);
            throw new UIRenderException("Cannot render component", e);
        }
        LOG.debug("Drag and Drop panel rendered");
    }

    /**
     * Methods prepares progress bar component
     */
    private void prepareProgressBar() {
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(470, 30));
        progressBar.setMaximum(100);
        progressBar.setValue(0);

        progressBar.setBackground(new Color(190, 190, 190));
        progressBar.setForeground(new Color(102, 255, 102));

    }

    public static void resetProgressBar() {
        progressBar.setValue(0);
    }

    public static void clearTextArea() {
        logArea.setText("");
    }

    /**
     * updates the progress bar
     * 
     * @param value
     * @param increment
     */
    public static void updateProgress(int value, boolean increment) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (increment) {
                    progressBar.setValue(progressBar.getValue() + value);
                } else {
                    progressBar.setValue(value);
                }
            }
        }).start();

    }

    /**
     * Logs into Text Area
     * 
     * @param message
     * @param lineSeparated
     */
    public synchronized static void logToTextArea(String message, boolean lineSeparated) {
        if (logArea == null) {
            return;
        }

        if (lineSeparated) {
            logArea.setText(logArea.getText() + System.lineSeparator() + message);
        } else {
            logArea.setText(logArea.getText() + message);
        }

        logArea.update(logArea.getGraphics());
        LOG.debug("Log message : " + message + " displayed to DragAndDropTextArea");
    }

    /**
     * Renders TextArea that keeps log of actions performed with the application as well as details on supported vs not
     * supported files to import
     * 
     * @param files
     * @param supported
     */
    public static void renderTextAreaLogFiles(List<File> files, Boolean supported) {

        if (files.isEmpty()) {
            return;
        }
        StringBuffer buffer = new StringBuffer();

        if (supported) {
            buffer.append(SUPPORTED);
        } else {
            buffer.append(UNSUPPORTED);
        }

        buffer.append(System.lineSeparator());
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            buffer.append(file.getName());
            buffer.append(System.lineSeparator());
        }

        logArea.append(logArea.getText() + buffer.toString());
        logArea.update(logArea.getGraphics());
    }

    @Override
    public void display() {
        repaint();
        setVisible(true);
    }

}
