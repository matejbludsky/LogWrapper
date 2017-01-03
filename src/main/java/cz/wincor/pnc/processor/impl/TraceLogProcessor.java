package cz.wincor.pnc.processor.impl;

import java.io.File;
import java.util.Scanner;

import org.apache.log4j.Logger;

import cz.wincor.pnc.GUI.DragAndDropPanel;
import cz.wincor.pnc.error.CommTraceProcessException;
import cz.wincor.pnc.error.ProcessorException;
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

    public static final String KEY_TAG = "[TRACE]";

    public TraceLogProcessor(File originalLogFile) {
        super(originalLogFile);
        LOG.info("Starting TraceLogProcessor");
    }

    @Override
    public void process() throws ProcessorException {
        if (originalLogFile == null) {
            // TODO replace me with TraceLogException
            throw new CommTraceProcessException("Log File cannot be null");
        }
        try {
            LOG.info("Processing file : " + originalLogFile.getAbsolutePath());
            DragAndDropPanel.logToTextArea("Extracting WSCC Messages from file : " + originalLogFile.getName(), true);
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
     */
    private int extractWSCCMesagesIntoFile() {

        Scanner scan = null;
        String currentLine = null;
        String WSCCMessage = "";
        int messageCount = 0;
        boolean activeMessage = false;
        String key = "";

        try {
            scan = new Scanner(originalLogFile);
            while (scan.hasNextLine()) {
                currentLine = scan.nextLine();

                if (currentLine.startsWith(KEY_TAG)) {
                    key = currentLine.substring(KEY_TAG.length(), 33).trim();
                }

                if (currentLine.startsWith(WSCC_START_TAG) || activeMessage) {
                    activeMessage = true;
                    WSCCMessage += currentLine;
                    // WSCC entry
                    // look for soap footer
                    if (currentLine.startsWith(WSCC_END_TAG)) {
                        if (isCompliant(WSCCMessage)) {
                            writeToTmpFile(key + AbstractProcessor.SEPARATOR + TraceStringUtils.appendSOAPEnvelope(WSCCMessage), getExtractedTmpFile());
                            messageCount++;
                        }
                        WSCCMessage = "";
                        activeMessage = false;
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("Cannot import file", e);
        } finally {
            try {
                if (scan != null) {
                    scan.close();
                }
            } catch (Exception e2) {
                LOG.error("Cannot close resource", e2);
            }
        }

        return messageCount;

    }

}
