package cz.wincor.pnc.processor.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;

import cz.wincor.pnc.cache.LevelDBCache;
import cz.wincor.pnc.cache.LogWrapperCacheItem;
import cz.wincor.pnc.error.ProcessorException;
import cz.wincor.pnc.gui.component.DragAndDropPanel;
import cz.wincor.pnc.processor.AbstractProcessor;
import cz.wincor.pnc.util.TraceStringUtils;

/**
 * @author matej.bludsky
 * 
 *         Processor class for trace files
 */

public class TraceLogProcessor extends AbstractProcessor {

    private static final Logger LOG = Logger.getLogger(TraceLogProcessor.class);
    public static final String WSCC_START_TAG = "<ns2:";
    public static final String WSCC_END_TAG = "</ns2:";

    public static final String SERVER_DATE_TAG = "[TRACE]";

    public TraceLogProcessor(File originalLogFile) {
        super(originalLogFile);
        LOG.info("Starting TraceLogProcessor");
    }

    @Override
    public void process() throws ProcessorException {
        if (originalLogFile == null) {
            // TODO replace me with TraceLogException
            throw new ProcessorException("Log File cannot be null");
        }
        try {
            LOG.info("Processing file : " + originalLogFile.getAbsolutePath());
            DragAndDropPanel.getInstance().logToTextArea("Extracting file : " + originalLogFile.getName(), true);
            DragAndDropPanel.getInstance().logToTextArea("Loading ", true);
            int numberOfReadMessages = extractWSCCMesagesIntoFile();
            DragAndDropPanel.getInstance().logToTextArea(numberOfReadMessages + " WSCC Messages found", true);
        } catch (Exception e) {
            LOG.error("Cannot extract comm trace " + originalLogFile.getAbsolutePath(), e);
        }

    }

    /**
     * s Method is going to filter WSCC messages from the merged log file
     * 
     * @throws IOException
     */
    private int extractWSCCMesagesIntoFile() throws ProcessorException {

        String WSCCMessage = "";
        int messageCount = 0;
        int increment = 0;
        boolean activeMessage = false;
        String serverDate = "";
        LineIterator it = null;
        DB db = null;
        try {
            db = LevelDBCache.getInstance().openDatabase();
            it = FileUtils.lineIterator(originalLogFile, "UTF-8");
            while (it.hasNext()) {
                String currentLine = it.nextLine();
                if (currentLine.startsWith(SERVER_DATE_TAG)) {
                    serverDate = currentLine.substring(SERVER_DATE_TAG.length(), 33).trim();
                }

                if (currentLine.startsWith(WSCC_START_TAG) || activeMessage) {
                    activeMessage = true;
                    WSCCMessage += currentLine;
                    // WSCC entry
                    // look for soap footer
                    if (currentLine.startsWith(WSCC_END_TAG)) {
                        if (isCompliant(WSCCMessage)) {
                            String key = UUID.randomUUID().toString();
                            LogWrapperCacheItem item = parseMessage(key, WSCCMessage);
                            item.setServerDate(TraceStringUtils.getDateFromString(serverDate));
                            db.put(key.getBytes(), LevelDBCache.getInstance().convertToBytes(item));
                            messageCount++;
                            increment++;
                            if (increment > 50) {
                                DragAndDropPanel.getInstance().logToTextArea(".", false);
                                increment = 0;
                            }
                        }
                        WSCCMessage = "";
                        activeMessage = false;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot read trace", e);
            throw new ProcessorException("Cannot process file : " + originalLogFile.getName() + " Error : " + e.getMessage());
        } finally {
            try {
                db.close();
                LineIterator.closeQuietly(it);
                LOG.info("Cache Prepared");
            } catch (IOException e) {
                LOG.error("Cannot close resource", e);
            }
        }

        return messageCount;
    }
}
