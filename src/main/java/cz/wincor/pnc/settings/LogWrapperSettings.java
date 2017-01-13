package cz.wincor.pnc.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 
 * @author matej.bludsky
 *
 *         Singleton Class holds settings
 */
public class LogWrapperSettings {

    public static LogWrapperSettings instance;

    private static final Logger LOG = Logger.getLogger(LogWrapperSettings.class);
    public static String currentDir = System.getProperty("user.dir");

    public static SettingValue HEARTBEAT = new SettingValue("HeartBeatRequest", false);
    public static SettingValue FREEJOURNAL = new SettingValue("FreeJournalRequest", false);
    public static SettingValue WKSTCHANGEEVENTS = new SettingValue("EventRequest", false);
    public static SettingValue CASSCOUNTER = new SettingValue("CounterChanged", false);
    public static SettingValue FORCEATMTAG = new SettingValue("", true);
    public static SettingValue ATM_ID = new SettingValue("ClientId", "");

    public static String TMP_LOCATION = currentDir + "/tmp";
    public static String LMDB_LOCATION = currentDir + "/tmp/db";

    public static int LEVEL_DB_CACHE_ALLOCATION = 100;

    public static String COMMTRACE_NAME_REGEXP = "/\bCommTrace\b/";
    public static String TRACE_NAME_REGEXP = "/\bTraceLog\b/";
    public static String IMAGES_LOCATION = currentDir + "/tmp/images";
    public static boolean IMAGES_CLEAR_ON_START = true;
    public static boolean ENDPOINT_JBOSS = true;
    public static boolean ENDPOINT_WAS = false;
    public static String SOAPUI_FINAL_LOCATION = currentDir + "/soapui";
    public static String ENDPOINT = "JBOSS";
    public static boolean SOAP_CLEAR_ON_START = false;
    public static boolean SOAP_CLEAR_BEFORE = true;
    public static String ENDPOINT_URL = "http://localhost:8080";

    private LogWrapperSettings() {
        LOG.info("using current dir : " + currentDir);
    }

    public synchronized static LogWrapperSettings getInstance() {
        if (instance == null) {
            instance = new LogWrapperSettings();
        }

        return instance;
    }

    /**
     * Save in memory to file
     * 
     * @throws IOException
     */
    public static void saveSettings() {

        Properties prop = new Properties();

        prop.put("commtrace.names", COMMTRACE_NAME_REGEXP);
        prop.put("tracelog.names", TRACE_NAME_REGEXP);
        prop.put("images.location", IMAGES_LOCATION);
        prop.put("images.clear", Boolean.toString(IMAGES_CLEAR_ON_START));
        prop.put("soapui.location", SOAPUI_FINAL_LOCATION);
        prop.put("soapui.clear", Boolean.toString(SOAP_CLEAR_ON_START));
        prop.put("soapui.clear.before", Boolean.toString(SOAP_CLEAR_BEFORE));
        prop.put("cache.size", Integer.toString(LEVEL_DB_CACHE_ALLOCATION));
        OutputStream output = null;

        try {
            File file = new File(currentDir + "/conf/LogWrapper.properties");
            output = new FileOutputStream(file);
            prop.store(output, "Properties");
            LOG.info("LogWrapper.properties saved");
        } catch (Exception e) {
            LOG.error("Cannot load LogPatterns.properties");
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                LOG.error("cannot close stream", e);
            }
        }
    }

    /**
     * Save in memory to file
     * 
     * @throws IOException
     */
    public static void loadSettings() {

        InputStream in = null;
        try {
            Properties prop = new Properties();
            in = new FileInputStream(currentDir + "/conf/LogWrapper.properties");
            prop.load(in);
            COMMTRACE_NAME_REGEXP = prop.getProperty("commtrace.names");
            TRACE_NAME_REGEXP = prop.getProperty("tracelog.names");
            IMAGES_LOCATION = prop.getProperty("images.location");
            IMAGES_CLEAR_ON_START = Boolean.parseBoolean(prop.getProperty("images.clear"));
            SOAPUI_FINAL_LOCATION = prop.getProperty("soapui.location");
            SOAP_CLEAR_ON_START = Boolean.parseBoolean(prop.getProperty("soapui.clear"));
            SOAP_CLEAR_BEFORE = Boolean.parseBoolean(prop.getProperty("soapui.clear.before"));
            LEVEL_DB_CACHE_ALLOCATION = Integer.parseInt(prop.getProperty("cache.size"));
        } catch (IOException e) {
            LOG.error("cannot read properties");
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LOG.error("cannot close stream", e);
            }
        }

    }

    public static String normalizeDir(String dir) {
        String location = dir;

        if (dir.startsWith("./")) {
            String baseDir = Paths.get("").toAbsolutePath().toString();
            location = baseDir + location.substring(1, location.length());
        }
        location = location.replace("\\", "/");

        return location;
    }

    public static boolean isHEARTBEAT() {
        if (HEARTBEAT.getValue() != null) {
            return (boolean) HEARTBEAT.getValue();
        }

        return false;
    }

    public static boolean isFREEJOURNAL() {
        if (FREEJOURNAL.getValue() != null) {
            return (boolean) FREEJOURNAL.getValue();
        }

        return false;
    }

    public static boolean isWKSTCHANGEEVENTS() {
        if (WKSTCHANGEEVENTS.getValue() != null) {
            return (boolean) WKSTCHANGEEVENTS.getValue();
        }

        return false;
    }

    public static String getATM_ID() {

        if (ATM_ID.getValue() != null) {
            return (String) ATM_ID.getValue();
        }

        return null;

    }

}
