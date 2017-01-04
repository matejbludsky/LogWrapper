package cz.wincor.pnc.processor;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import cz.wincor.pnc.error.TraceLoadingException;
import cz.wincor.pnc.error.ProcessorException;
import cz.wincor.pnc.settings.LogWrapperSettings;
import cz.wincor.pnc.util.FileUtil;
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

    private static final Logger LOG = Logger.getLogger(AbstractProcessor.class);

    protected volatile File originalLogFile = null;
    protected volatile File extractedTmpFile = null;

    public static final String[] SOAP_HEADERS = new String[] { "<soap:Envelope", "<soapenv:Envelope", "<S:Envelope" };
    public static final String[] SOAP_FOOTER = new String[] { "</soapenv:Envelope>", "</soap:Envelope", "</S:Envelope>" };

    public static final String SEPARATOR = "&";

    public AbstractProcessor(File originalLogFile) {
        this.originalLogFile = originalLogFile;
        extractedTmpFile = new File(LogWrapperSettings.TMP_LOCATION + "/" + UUID.randomUUID() + "_WSCC_REQUEST.tmp");
    }

    /**
     * used by processors to execute action
     * 
     * @throws TraceLoadingException
     */
    public abstract void process() throws ProcessorException;

    /**
     * Writes String content to a tmp file
     * 
     * @param cache
     * @throws IOException
     */
    public void writeToTmpFile(StringBuilder cache, File f) throws IOException {
        FileUtil.writeToFile(f, cache.toString());
    }

    /**
     * deletes file
     */
    protected void deleteFile() {

        if (extractedTmpFile != null) {
            String name = extractedTmpFile.getAbsolutePath();
            if (extractedTmpFile.delete()) {
                LOG.info("File deteled :" + name);
            }
        }
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

    public File getExtractedTmpFile() {
        return extractedTmpFile;
    }

    public void setExtractedTmpFile(File extractedTmpFile) {
        this.extractedTmpFile = extractedTmpFile;
    }

}
