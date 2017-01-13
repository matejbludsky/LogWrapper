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
 * Class takes CommTrace log and process it for the application view
 * 
 * @author matej.bludsky
 *
 */
public class CommTraceProcessor extends AbstractProcessor {

    private static final Logger LOG = Logger.getLogger(CommTraceProcessor.class);

    public static final String WSCC_TAG = "|>WS|";

    public CommTraceProcessor(File logFile) {
        super(logFile);
        LOG.info("Starting CommTraceProcessor");
    }

    /**
     * CommTrace files already merged into logFile * Method takes merged file and extracts information that is needed in
     * order to complete SOAPUI Suite conversion. Method is searching for |>WS| String, this is a tag for
     * WebServiceClientConnector
     * 
     * Method is going to analyze each line and extract only requests SOAP messages
     */
    @Override
    public void process() throws ProcessorException {
        if (originalLogFile == null) {
            throw new ProcessorException("Log File cannot be null");
        }
        LOG.info("Processing file : " + originalLogFile.getAbsolutePath());
        DragAndDropPanel.getInstance().logToTextArea("Extracting file : " + originalLogFile.getName(), true);
        DragAndDropPanel.getInstance().logToTextArea("Loading ", true);
        int numberOfReadMessages = readWSCCRequestsIntoCache();
        DragAndDropPanel.getInstance().logToTextArea(numberOfReadMessages + " WSCC Messages found", true);

    }

    /**
     * s Method is going to filter WSCC messages from the merged log file
     * 
     * @throws IOException
     */
    private int readWSCCRequestsIntoCache() throws ProcessorException {

        String WSCCMessage = null;
        int messageCount = 0;
        int increment = 0;
        boolean activeMessage = false;
        LineIterator it = null;
        DB db = null;
        try {
            db = LevelDBCache.getInstance().openDatabase();
            it = FileUtils.lineIterator(originalLogFile, "UTF-8");

            while (it.hasNext()) {
                String currentLine = it.nextLine();
                if (currentLine.startsWith(WSCC_TAG) || activeMessage) {
                    activeMessage = true;
                    WSCCMessage += currentLine;
                    // WSCC entry
                    // look for soap footer
                    if (isSOAPFooter(WSCCMessage)) {
                        String key = UUID.randomUUID().toString();
                        String message = filterSOAPMessage(WSCCMessage);
                        if (TraceStringUtils.isWSCCMessage(message) && isCompliant(message)) {
                            String serverDate = WSCCMessage.substring(WSCCMessage.indexOf(WSCC_TAG) + WSCC_TAG.length(), WSCCMessage.indexOf(WSCC_TAG) + WSCC_TAG.length() + 23);
                            LogWrapperCacheItem item = parseMessage(key, message);
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
            LOG.error("Cannot read comm trace", e);
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

    /**
     * Filters only SOAP messages from the String line content
     * 
     * @param line
     * @return
     */
    private String filterSOAPMessage(String line) {

        int indexOfSOAPHeader = -1;
        int closingTagSOAPHeader = -1;

        int footerSubstringLength = 0;

        // HEADERS
        for (String header : SOAP_HEADERS) {
            int index = findIdexOf(header, line);
            if (index != -1) {
                indexOfSOAPHeader = index;

            }
        }

        // FOOTERS
        for (String footer : SOAP_FOOTER) {
            int index = findIdexOf(footer, line);
            if (index != -1) {
                closingTagSOAPHeader = index;
                footerSubstringLength = footer.length();

            }
        }

        if (indexOfSOAPHeader == -1 || closingTagSOAPHeader == -1) {
            return null;
        }

        String message = line.substring(indexOfSOAPHeader, closingTagSOAPHeader + footerSubstringLength);

        if (!message.endsWith(">")) {
            message += ">";
        }

        return message;

    }

}
