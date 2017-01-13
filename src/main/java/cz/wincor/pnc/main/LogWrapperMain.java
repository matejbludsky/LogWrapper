package cz.wincor.pnc.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import cz.wincor.pnc.error.FileImportException;
import cz.wincor.pnc.error.UIRenderException;
import cz.wincor.pnc.gui.component.DragAndDropPanel;
import cz.wincor.pnc.gui.jframe.LogWrapperUIJFrame;
import cz.wincor.pnc.importer.FileImporter;
import cz.wincor.pnc.settings.LogWrapperSettings;
import cz.wincor.pnc.util.FileUtil;
import cz.wincor.pnc.util.SystemMonitoringThread;

public class LogWrapperMain implements Runnable {

    private static final Logger LOG = Logger.getLogger(LogWrapperMain.class);

    public static void main(String[] args) {

        LOG.info("Starting LogWrapper");
        try {
            cleanENV();
            Thread mainThread = new Thread(new LogWrapperMain());
            mainThread.start();
            Thread.sleep(2000);
            LOG.info("Processing arguments");
            processArguments(args);
        } catch (Exception e1) {
            LOG.error(e1);
        }

    }

    @Override
    public void run() {
        LOG.info("::Starting LogWapper::");
        LOG.info("::Displaing main JFrame::");
        LogWrapperSettings.loadSettings();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LogWrapperUIJFrame mainFrame = LogWrapperUIJFrame.getInstance();
                try {
                    mainFrame.renderUI();
                    SystemMonitoringThread monitoring = new SystemMonitoringThread();
                    monitoring.start();
                } catch (UIRenderException e) {
                    LOG.error(e.toString());
                }
            }
        });

    }

    private static void cleanENV() throws IOException {

        FileUtil.clearDirectory(LogWrapperSettings.TMP_LOCATION);

        if (LogWrapperSettings.SOAP_CLEAR_ON_START) {
            FileUtil.clearDirectory(LogWrapperSettings.SOAPUI_FINAL_LOCATION);
        }

        if (LogWrapperSettings.IMAGES_CLEAR_ON_START) {
            FileUtil.clearDirectory(LogWrapperSettings.IMAGES_LOCATION);
        }

        Files.createDirectories(Paths.get(LogWrapperSettings.LMDB_LOCATION));
        Files.createDirectories(Paths.get(LogWrapperSettings.IMAGES_LOCATION));
        Files.createDirectories(Paths.get(LogWrapperSettings.SOAPUI_FINAL_LOCATION));

    }

    /**
     * Iterates over arguments given and import files
     * 
     * @param args
     * @throws FileImportException
     */
    private static void processArguments(String[] args) throws FileImportException {
        if (args != null) {

            List<File> files = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                File f = createFile(args[i]);
                if (f != null) {
                    LOG.debug("Argument file found : " + f.getAbsolutePath());
                    files.add(f);
                }
            }
            if (!files.isEmpty()) {
                LOG.debug("Processing arguments");
                FileImporter processor = new FileImporter();
                processor.importFiles(files);
            }
        }
    }

    private static File createFile(String path) {
        File f = new File(path);

        if (f.exists()) {
            LOG.debug("File loaded : " + f.getAbsolutePath());
            return f;
        }

        LOG.warn("File : " + path + " doesnt exist");
        DragAndDropPanel.getInstance().logToTextArea("File : " + path + " doesnt exist, cannot import", true);
        return null;
    }

}
