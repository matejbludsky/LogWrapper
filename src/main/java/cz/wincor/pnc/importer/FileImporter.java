package cz.wincor.pnc.importer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import cz.wincor.pnc.cache.LevelDBCache;
import cz.wincor.pnc.common.IFileImporter;
import cz.wincor.pnc.error.FileImportException;
import cz.wincor.pnc.error.ProcessorException;
import cz.wincor.pnc.gui.component.DragAndDropPanel;
import cz.wincor.pnc.gui.jframe.MessagesReviewJFrame;
import cz.wincor.pnc.processor.AbstractProcessor;
import cz.wincor.pnc.processor.ProcessorFactory;
import cz.wincor.pnc.settings.LogWrapperSettings;
import cz.wincor.pnc.types.PCELogType;
import cz.wincor.pnc.types.PCEServerLog;
import cz.wincor.pnc.util.FileUtil;

/**
 * @author matej.bludsky
 * 
 *         Processor for importing files into application File extensions supported : ctr,txt,rtf
 */

public class FileImporter implements IFileImporter {

    public static Set<String> supportedExtensions;

    private static final Logger LOG = Logger.getLogger(DragAndDropPanel.class);

    private MessagesReviewJFrame activePreview = null;

    /**
     * add supported formats
     */
    static {
        supportedExtensions = new LinkedHashSet<String>();
        supportedExtensions.add("ctr");
        supportedExtensions.add("txt");
        supportedExtensions.add("rtf");
        supportedExtensions.add("prn");
    }

    /**
     * Imports files into application
     */
    @Override
    public void importFiles(List<File> files) throws FileImportException {

        ImportFileWorker worker = new ImportFileWorker(files);
        worker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                switch (evt.getPropertyName()) {
                case "progress":
                    int progress = (Integer) evt.getNewValue();
                    DragAndDropPanel.updateProgress(progress, false);
                    break;
                }
            }
        });

        worker.execute();
    }

    /**
     * Determines if file provided is supported by the application Folders are not considered files
     * 
     * @param f
     * @param unsupported
     */
    private void isSupported(File f, List<File> supported) {
        String fileName = f.getName();
        String fileExtension = f.getName().toString().substring(fileName.lastIndexOf(".") + 1, fileName.length());

        if (f.isDirectory()) {
            return;
        }

        if (supportedExtensions.contains(fileExtension)) {
            LOG.debug("SUPPORTED file " + f.getAbsolutePath());

            PCELogType type = PCELogType.fromFileName(fileName);
            switch (type) {
            case UNSUPPORTED:
                LOG.debug("Unsupported file : " + fileName);
                break;

            default:
                supported.add(f);
                break;
            }

        } else {
            LOG.debug("NOT supported file " + f.getAbsolutePath());
        }

    }

    public class ImportFileWorker extends SwingWorker<Boolean, String> {

        private List<File> importFiles = new ArrayList<File>();

        public ImportFileWorker(List<File> importFiles) {
            super();
            this.importFiles = importFiles;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            System.gc();
            // initialize cache
            if (activePreview != null) {
                activePreview.dispose();
            }

            LevelDBCache.getInstance().initialize();
            List<File> supported = new ArrayList<File>();
            try {
                FileUtil.clearDirectory(LogWrapperSettings.TMP_LOCATION);

                for (File droppedFile : importFiles) {
                    DragAndDropPanel.getInstance().clearTextArea();
                    LOG.info("Importing dropped file : " + droppedFile.getAbsolutePath());

                    Files.walk(droppedFile.toPath()).forEach(path -> isSupported(path.toFile(), supported));
                }

                DragAndDropPanel.getInstance().renderTextAreaLogFiles(supported);
                DragAndDropPanel.resetProgressBar();
                setProgress(15);
                int updateValue = 0;
                // create log items
                for (File log : supported) {
                    /**
                     * Process Files
                     **/
                    LOG.debug("Processing file : " + log.getName());
                    PCEServerLog serverLog = new PCEServerLog(log, PCELogType.fromFileName(log.getName()));
                    AbstractProcessor processor = ProcessorFactory.getInstance().getProcessor(serverLog);
                    processor.process();
                    updateValue = updateValue + (85 / supported.size());
                    setProgress(updateValue);
                    serverLog = null;
                    processor = null;
                }

                // load MessageReviewFrame
                if (!supported.isEmpty()) {
                    activePreview = new MessagesReviewJFrame();
                    activePreview.renderUI();
                } else {
                    DragAndDropPanel.getInstance().logToTextArea("No files supported", true);
                }

                setProgress(100);
            } catch (ProcessorException e) {
                LOG.error("Cannot import files ", e);
                JOptionPane.showMessageDialog(DragAndDropPanel.getInstance(), e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            }

            return true;
        }
    }
}
