package cz.wincor.pnc.processor.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;

import cz.wincor.pnc.cache.LevelDBCache;
import cz.wincor.pnc.cache.LogWrapperCacheItem;
import cz.wincor.pnc.error.ProcessorException;
import cz.wincor.pnc.gui.component.DragAndDropPanel;
import cz.wincor.pnc.processor.AbstractCommTraceProcessor;
import cz.wincor.pnc.processor.AbstractProcessor;
import cz.wincor.pnc.processor.CommTraceProcessorFactory;
import cz.wincor.pnc.types.HostTrxMessageEnum;
import cz.wincor.pnc.types.HostTrxMessageType;
import cz.wincor.pnc.util.TraceStringUtils;

/**
 * Class takes CommTrace log and process it for the application view
 * 
 * @author matej.bludsky
 *
 */
public class CommTraceProcessor extends AbstractProcessor {

    private static final Logger LOG = Logger.getLogger(CommTraceProcessor.class);

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
        int numberOfReadMessages = writeMessageIntoCache();
        //readWSCCRequestsIntoCache();
        //int numberOfReadMessages = readRSSRequestsIntoCache();
        DragAndDropPanel.getInstance().logToTextArea(numberOfReadMessages + " WSCC Messages found", true);

    }
    
    
    
    
    
    private int writeMessageIntoCache() throws ProcessorException {
        StringBuffer message = new StringBuffer();
        boolean activeMessage = false;
        LineIterator it = null;
        DB db = null;
        int messageCount = 0;
        int increment = 0;
        HostTrxMessageEnum hostTrxMessageEnum;
        try {
            db = LevelDBCache.getInstance().openDatabase();
            it = FileUtils.lineIterator(originalLogFile, "UTF-8");
            while (it.hasNext()) {
                String currentLine = it.nextLine();
                hostTrxMessageEnum = Arrays.stream(HostTrxMessageEnum.values())
                        .filter(v -> currentLine.startsWith(v.getPattern()))
                        .findFirst().orElse(HostTrxMessageEnum.NOT_SUPPORTED);
                if (hostTrxMessageEnum != HostTrxMessageEnum.NOT_SUPPORTED || activeMessage) { 
                    message.append(currentLine);
                    activeMessage = true;
                    if (isSOAPFooter(message.toString())) {
                        HostTrxMessageType hostTrxMessageType = new HostTrxMessageType();
                        hostTrxMessageType.setHostTrxMessageEnum(hostTrxMessageEnum);
                        hostTrxMessageType.setMessage(message.toString());
                        AbstractCommTraceProcessor abstractCommTraceProcessor =  CommTraceProcessorFactory.getInstance().getProcessor(hostTrxMessageType);
                        if(abstractCommTraceProcessor!=null){
                            abstractCommTraceProcessor.saveToDb(db);
                            messageCount++;
                            increment++;
                            if (increment > 50) {
                                DragAndDropPanel.getInstance().logToTextArea(".", false);
                                increment = 0;
                            }
                        }
                        message.setLength(0);
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
    
    
}