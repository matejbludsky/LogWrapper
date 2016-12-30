package cz.wincor.pnc.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import cz.wincor.pnc.error.CommTraceLoadException;
import cz.wincor.pnc.error.CommTraceProcessException;
import cz.wincor.pnc.error.ProcessorException;
import cz.wincor.pnc.settings.LogWrapperSettings;
import cz.wincor.pnc.util.TraceStringUtils;
import cz.wincor.pnc.util.FileUtil;

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

    public static final String[] SOAP_HEADERS = new String[] { "<soap:Envelope", "<soapenv:Envelope" };
    public static final String[] SOAP_FOOTER = new String[] { "</soapenv:Envelope>", "</soap:Envelope" };

    public static final String SEPARATOR = "&";

    public AbstractProcessor(File originalLogFile) {
        this.originalLogFile = originalLogFile;
        extractedTmpFile = new File(LogWrapperSettings.TMP_LOCATION + "/" + UUID.randomUUID() + "_WSCC_REQUEST.tmp");
    }

    /**
     * used by processors to execute action
     * 
     * @throws CommTraceProcessException
     */
    public abstract void process() throws ProcessorException;

    /**
     * Loads all entries that has been extracted by extractContent method from tmp file into cache Map<String,String>
     * 
     * Key is a date from the CommTrace file Value is the message itself
     * 
     * Example entry :
     * 
     * Key : 2016-11-15 14:13:48.14
     * 
     * Value : <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:core=
     * "http://wincornixdorf.com/pce/server/wsclientconnector/v02/core"> <soapenv:Header /> <soapenv:Body>
     * <ns2:EventRequest xmlns:ns2="http://wincornixdorf.com/pce/server/wsclientconnector/v02/core"> <Mixins>
     * <ClassName>cz.wincor.pnc.bobjs.PNCRequestMixin</ClassName> <Properties> <Name>AtmSequenceNumber</Name>
     * <String>----</String> </Properties> </Mixins> <ClientId>66000</ClientId>
     * <ClientRequestNumber>1479240827965</ClientRequestNumber>
     * <ClientRequestRepeatCounter>0</ClientRequestRepeatCounter> <ClientRequestTime>1469486247968</ClientRequestTime>
     * <EventReason>DEVICESTATE</EventReason> <WkstState>STARTUP</WkstState> </ns2:EventRequest> </soapenv:Body>
     * </soapenv:Envelope>
     * 
     * 
     * extractContent method as to be called before calling loadAll
     * 
     * 
     * @return
     * @throws CommTraceLoadException
     */
    public static Map<String, String> loadExtractedData() throws IOException {

        // load final merged file
        File logWrapperFile = findFinalFile();

        if (logWrapperFile == null) {
            return null;
        }

        Map<String, String> cache = new LinkedHashMap<>();

        /** LOAD TMP FILE LINE BY LINE **/
        BufferedReader br = null;
        FileReader fr = null;

        try {

            fr = new FileReader(logWrapperFile);
            br = new BufferedReader(fr);

            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                /** EACH LINE REPRESENTS ONE MESSAGE **/
                String[] keyAndValue = extractEntry(currentLine);
                cache.put(keyAndValue[0], keyAndValue[1]);
                LOG.debug("Entry : Key: " + keyAndValue[0] + " Value: " + keyAndValue[1]);
            }

            LOG.info("Chache readed from file : " + logWrapperFile.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Cannot import file", e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (Exception e2) {
                LOG.error("Cannot close resource", e2);
            }

        }

        return cache;

    }

    private static File findFinalFile() {
        File path = new File(LogWrapperSettings.TMP_LOCATION);

        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && files[i].getName().endsWith(".logwrapper")) {
                return files[i];
            }
        }

        return null;

    }

    /**
     * Writes String content to a tmp file
     * 
     * @param cache
     * @throws IOException
     */
    public void writeToTmpFile(String cache, File f) throws IOException {
        String line = cache.replace(System.lineSeparator(), " ");
        line.trim();
        line += System.lineSeparator();
        FileUtil.appendLineToFile(f, line);
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

    /**
     * Finds given string
     * 
     * @param subString
     * @param mainString
     * @return
     */
    protected boolean findSubstring(String subString, String mainString) {
        boolean foundme = false;
        int max = mainString.length() - 1;

        checkrecusion: for (int i = 0; i <= max; i++) {
            int n = subString.length();

            int j = i;
            int k = 0;

            while (n-- != 0) {
                if (mainString.charAt(j++) != subString.charAt(k++)) {
                    continue checkrecusion;
                }
            }
            foundme = true;
            break checkrecusion;
        }
        return foundme;
    }

    /**
     * Extracts key from given tmp file line content
     * 
     * @param line
     * @return
     */
    private static String[] extractEntry(String line) {

        String[] keyAndValue = line.split(SEPARATOR, 40);
        if (keyAndValue[0] != null && keyAndValue[1] != null) {
            return keyAndValue;
        }

        return null;
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
