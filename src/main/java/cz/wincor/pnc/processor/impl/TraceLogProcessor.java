package cz.wincor.pnc.processor.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import cz.wincor.pnc.GUI.DragAndDropPanel;
import cz.wincor.pnc.error.ProcessorException;
import cz.wincor.pnc.error.TraceLoadingException;
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
            throw new TraceLoadingException("Log File cannot be null");
        }
        try {
            LOG.info("Processing file : " + originalLogFile.getAbsolutePath());
            DragAndDropPanel.logToTextArea("Extracting file : " + originalLogFile.getName(), true);
            int numberOfReadMessages = extractWSCCMesagesIntoFile();
            if (numberOfReadMessages == 0) {
                DragAndDropPanel.logToTextArea("No WSCC Messages found in file " + originalLogFile.getName(), true);
                return;
            }
        } catch (Exception e) {
            LOG.error("Cannot extract comm trace " + originalLogFile.getAbsolutePath(), e);
        }

    }

    /**
     * s Method is going to filter WSCC messages from the merged log file
     * 
     * @throws IOException
     */
    private int extractWSCCMesagesIntoFile() throws IOException {

        String WSCCMessage = "";
        int messageCount = 0;
        boolean activeMessage = false;
        String serverDate = "";
        StringBuilder messages = new StringBuilder();
        LineIterator it;
        it = FileUtils.lineIterator(originalLogFile, "UTF-8");
        try {
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
                            messages.append(key + AbstractProcessor.SEPARATOR + serverDate + AbstractProcessor.SEPARATOR + TraceStringUtils.appendSOAPEnvelope(WSCCMessage));
                            messageCount++;
                        }
                        WSCCMessage = "";
                        activeMessage = false;
                    }
                }
            }

            writeToTmpFile(messages, getExtractedTmpFile());

        } finally {
            LineIterator.closeQuietly(it);
            LOG.info("Cache Prepared");
        }

        return messageCount;
    }
}
