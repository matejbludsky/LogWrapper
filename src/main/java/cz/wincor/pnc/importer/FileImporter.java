package cz.wincor.pnc.importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.wincor.pnc.GUI.DragAndDropPanel;
import cz.wincor.pnc.GUI.MessagesReviewJFrame;
import cz.wincor.pnc.common.IFileImporter;
import cz.wincor.pnc.error.FileImportException;
import cz.wincor.pnc.error.ProcessorException;
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
        List<File> supported = new ArrayList<File>();
        List<File> unsupported = new ArrayList<File>(files);
        try {
            FileUtil.clearDirectory(LogWrapperSettings.TMP_LOCATION);

            for (File droppedFile : files) {
                DragAndDropPanel.logArea.setText("");
                LOG.info("Importing dropped file : " + droppedFile.getAbsolutePath());

                Files.walk(droppedFile.toPath()).forEach(path -> isSupported(path.toFile(), supported));
                /**
                 * Process Files
                 **/
                LOG.debug("Processing files : " + supported.toString());
                DragAndDropPanel.renderTextAreaLogFiles(supported, true);
            }

            // create log items
            for (File log : supported) {
                PCEServerLog serverLog = new PCEServerLog(log, PCELogType.fromFileName(log.getName()));
                AbstractProcessor processor = ProcessorFactory.getInstance().getProcessor(serverLog);
                processor.process();
            }

            unsupported.removeAll(supported);
            DragAndDropPanel.renderTextAreaLogFiles(unsupported, false);
            // load MessageReviewFrame

            if (!supported.isEmpty()) {
                FileUtil.mergeExtractedTmpFiles();

                MessagesReviewJFrame preview = new MessagesReviewJFrame();
                preview.renderUI(AbstractProcessor.loadExtractedData());
            }

        } catch (IOException e) {
            LOG.error("Cannot import files ", e);
        } catch (ProcessorException e) {
            LOG.error("Cannot process file ", e);
        } catch (UIRenderException e) {
            LOG.error("Cannot render MessagesReviewJFrame ", e);
        }
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
            supported.add(f);
        } else {
            LOG.debug("NOT supported file " + f.getAbsolutePath());
        }

    }

}
