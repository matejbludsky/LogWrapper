package cz.wincor.pnc.importer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import cz.wincor.pnc.GUI.DragAndDropPanel;
import cz.wincor.pnc.GUI.MessagesReviewJFrame;
import cz.wincor.pnc.cache.DataCache;
import cz.wincor.pnc.common.IFileImporter;
import cz.wincor.pnc.error.FileImportException;
import cz.wincor.pnc.error.TraceLoadingException;
import cz.wincor.pnc.error.UIRenderException;
import cz.wincor.pnc.processor.AbstractProcessor;
import cz.wincor.pnc.processor.PCELogType;
import cz.wincor.pnc.processor.PCEServerLog;
import cz.wincor.pnc.processor.ProcessorFactory;
import cz.wincor.pnc.settings.LogWrapperSettings;
import cz.wincor.pnc.util.FileUtil;

/**
 * @author matej.bludsky
 * 
 *         Processor for importing files into application File extensions supported : ctr,txt,rtf
 */

public class FileImporter implements IFileImporter {

    public static Set<String> supportedExtensions;

    private static final Logger LOG = Logger.getLogger(DragAndDropPanel.class);

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

            List<File> supported = new ArrayList<File>();
            List<File> unsupported = new ArrayList<File>(importFiles);
            try {
                FileUtil.clearDirectory(LogWrapperSettings.TMP_LOCATION);

                for (File droppedFile : importFiles) {
                    DragAndDropPanel.clearTextArea();
                    LOG.info("Importing dropped file : " + droppedFile.getAbsolutePath());

                    Files.walk(droppedFile.toPath()).forEach(path -> isSupported(path.toFile(), supported));
                    /**
                     * Process Files
                     **/
                    LOG.debug("Processing files : " + supported.toString());

                }
                DragAndDropPanel.renderTextAreaLogFiles(supported, true);
                DragAndDropPanel.resetProgressBar();
                setProgress(15);
                int updateValue = 0;
                // create log items
                for (File log : supported) {
                    PCEServerLog serverLog = new PCEServerLog(log, PCELogType.fromFileName(log.getName()));
                    AbstractProcessor processor = ProcessorFactory.getInstance().getProcessor(serverLog);
                    processor.process();
                    updateValue = updateValue + (85 / supported.size());
                    setProgress(updateValue);
                    serverLog = null;
                    processor = null;
                }

                unsupported.removeAll(supported);
                // load MessageReviewFrame
                if (!supported.isEmpty()) {
                    FileUtil.mergeExtractedTmpFiles();
                    DataCache.getInstance().initializeCache();
                    MessagesReviewJFrame preview = new MessagesReviewJFrame();
                    preview.renderUI();
                } else {
                    DragAndDropPanel.renderTextAreaLogFiles(unsupported, false);
                }
                setProgress(100);
                System.gc();

            } catch (IOException e) {
                LOG.error("Cannot import files ", e);
            } catch (TraceLoadingException e) {
                LOG.error("Cannot load trace files ", e);
            } catch (UIRenderException e) {
                LOG.error("Cannot render MessagesReviewJFrame ", e);
            }

            return true;
        }
    }

}
