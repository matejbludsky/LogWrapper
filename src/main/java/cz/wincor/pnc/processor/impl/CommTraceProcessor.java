package cz.wincor.pnc.processor.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import cz.wincor.pnc.GUI.DragAndDropPanel;
import cz.wincor.pnc.error.TraceLoadingException;
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
    public void process() throws TraceLoadingException {
        if (originalLogFile == null) {
            throw new TraceLoadingException("Log File cannot be null");
        }
        try {
            LOG.info("Processing file : " + originalLogFile.getAbsolutePath());
            DragAndDropPanel.logToTextArea("Extracting file : " + originalLogFile.getName(), true);
            int numberOfReadMessages = readWSCCRequestsIntoTmpFile();
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
    private int readWSCCRequestsIntoTmpFile() throws IOException {

        String WSCCMessage = null;
        int messageCount = 0;
        boolean activeMessage = false;

        StringBuilder messages = new StringBuilder();
        
        LineIterator it;
        it = FileUtils.lineIterator(originalLogFile, "UTF-8");
        try {
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
                            messages.append(key + AbstractProcessor.SEPARATOR + message+System.lineSeparator());
                            
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
