package cz.wincor.pnc.processor;

import java.io.File;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.wincor.pnc.cache.LogWrapperCacheItem;
import cz.wincor.pnc.error.ProcessorException;
import cz.wincor.pnc.settings.LogWrapperSettings;
import cz.wincor.pnc.util.TraceStringUtils;

/**
 * 
 * Abstract class for possible Log processors
 * 
 * Implemented only CommTrace log processor, future implementation for TraceLog, PCELog etc
 * 
 * @author matej.bludsky
 *
 */
public abstract class AbstractProcessor {

    protected volatile File originalLogFile = null;

    public static final String[] SOAP_HEADERS = new String[] { "<soap:Envelope", "<soapenv:Envelope", "<S:Envelope" };
    public static final String[] SOAP_FOOTER = new String[] { "</soapenv:Envelope>", "</soap:Envelope", "</S:Envelope>" };

    public static final String SEPARATOR = "&";

    public AbstractProcessor(File originalLogFile) {
        this.originalLogFile = originalLogFile;
    }

    /**
     * used by processors to execute action
     * 
     * @throws TraceLoadingException
     */
    public abstract void process() throws ProcessorException;

    /**
     * provides LogWrapperCacheItem filled with preparsed items that are shared between processors
     * 
     * @param key
     * @param message
     * @return
     */
    protected LogWrapperCacheItem parseMessage(String key, String message) {

        LogWrapperCacheItem item = new LogWrapperCacheItem();

        String SEQNumber = TraceStringUtils.extractSEQNumber(key, message);
        Date clientDate = TraceStringUtils.extractClientDateToDate(key, message);
        String ATMId = TraceStringUtils.extractATMID(message);
        String messageType = TraceStringUtils.extractMessageType(key, message);
        boolean infoTransaction = TraceStringUtils.isInfoTransaction(key, message, messageType);

        item.setSEQNumber(SEQNumber);
        item.setClientDate(clientDate);
        item.setATMId(ATMId);
        item.setMessageType(messageType);
        item.setInfoTransaction(infoTransaction);
        item.setMessage(message);

        return item;

    }

    /**
     * Determines if the message is comlient with the rules
     * 
     * @param key
     * @param value
     * @return
     */
    protected boolean isCompliant(String value) {
        boolean complient = true;

        complient = applyATMIDRule(value);

        // HEARTBEAT
        if (value.contains(LogWrapperSettings.HEARTBEAT.getRepresentation())) {
            if (!(boolean) LogWrapperSettings.HEARTBEAT.getValue()) {
                complient = false;
            }
        }
        // FREEJOURNAL
        if (value.contains(LogWrapperSettings.FREEJOURNAL.getRepresentation())) {
            if (!(boolean) LogWrapperSettings.FREEJOURNAL.getValue()) {
                complient = false;
            }
        }
        // WKSTCHANGEEVENTS
        if (value.contains(LogWrapperSettings.WKSTCHANGEEVENTS.getRepresentation())) {
            if (!(boolean) LogWrapperSettings.WKSTCHANGEEVENTS.getValue()) {
                complient = false;
            }
        }

        // COUNTER CHANGE
        if (value.contains(LogWrapperSettings.CASSCOUNTER.getRepresentation())) {
            if (!(boolean) LogWrapperSettings.CASSCOUNTER.getValue()) {
                complient = false;
            }
        }

        return complient;
    }

    private static boolean applyATMIDRule(String value) {
        if (LogWrapperSettings.ATM_ID.getValue().toString().isEmpty()) {
            return true;
        }
        String ATMID = TraceStringUtils.extractATMID(value);

        Pattern p = Pattern.compile(LogWrapperSettings.ATM_ID.getValue().toString());
        Matcher m = p.matcher(ATMID);

        if (m.matches()) {
            return true;
        }

        return false;
    }

    /**
     * returns first index of given string
     * 
     * @param substring
     * @param message
     * @return
     */
    protected int findIdexOf(String substring, String message) {
        return message.indexOf(substring);
    }

    protected boolean isSOAPFooter(String message) {

        for (int i = 0; i < SOAP_FOOTER.length; i++) {
            if (message.contains(SOAP_FOOTER[i])) {
                return true;
            }
        }
        return false;
    }

    public File getOriginalLogFile() {
        return originalLogFile;
    }

    public void setOriginalLogFile(File originalLogFile) {
        this.originalLogFile = originalLogFile;
    }

}
