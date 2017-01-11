package cz.wincor.pnc.cache;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import cz.wincor.pnc.error.CommTraceLoadException;
import cz.wincor.pnc.processor.AbstractProcessor;
import cz.wincor.pnc.settings.LogWrapperSettings;
import cz.wincor.pnc.util.TraceStringUtils;

/**
 * @author matej.bludsky
 * 
 *         Cache class implementation
 */

public class DataCache {

    private static final Logger LOG = Logger.getLogger(DataCache.class);
    public static DataCache instance = null;
    private Map<String, LogWrapperCacheItem> cache = new HashMap<>();
    private File finalCacheFile;

    private DataCache() {
        LOG.debug("DataCache started");
    }

    /**
     * returns instance of MessagesReviewJFrame
     * 
     * @return
     */
    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;

    }

    public void clearAll() {
        this.cache.clear();
        this.finalCacheFile = null;
    }

    /**
     * Methods loads keys from the final cache file into List<String> keysCache
     * 
     * @throws IOException
     */
    public void initializeCache() throws CommTraceLoadException, IOException {
        LineIterator it = null;
        try {
            clearAll();
            findFinalFile();

            if (finalCacheFile == null) {
                LOG.error("Cannot load cache, File is null");
                throw new CommTraceLoadException("File cannot be null");
            }

            LOG.info("Preparing cache for : " + finalCacheFile.getName());

            it = FileUtils.lineIterator(finalCacheFile, "UTF-8");
            while (it.hasNext()) {
                String line = it.nextLine();
                String[] entry = line.split(AbstractProcessor.SEPARATOR);
                if (entry == null || entry.length != 3) {
                    LOG.error("Cannot read entry : " + Arrays.toString(entry));
                    continue;
                }

                String key = entry[0];
                Date serverDate = TraceStringUtils.getDateFromString(entry[1]);
                String message = entry[2];
                cache.put(key, new LogWrapperCacheItem(message, serverDate));
            }
        } finally {
            LineIterator.closeQuietly(it);
            LOG.info("Cache Prepared");
        }
    }

   /*
    public void synchronizeCacheIntoFile() throws IOException {

        StringBuilder content = new StringBuilder();

        for (Map.Entry<String, LogWrapperCacheItem> entry : cache.entrySet()) {
            String key = entry.getKey();
            String serverDate = new SimpleDateFormat(TraceStringUtils.importDateFormats[0]).format(entry.getValue().getServerDate());
            String message = entry.getValue().getMessage();
            content.append(key + AbstractProcessor.SEPARATOR + serverDate + AbstractProcessor.SEPARATOR + message);
        }

        FileUtils.writeStringToFile(finalCacheFile, content.toString());
        LOG.debug("Cache refreshed");
    }
*/
    /**
     * find the final cache file created by log processors
     */
    private void findFinalFile() {
        File path = new File(LogWrapperSettings.TMP_LOCATION);

        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && files[i].getName().endsWith(".logwrapper")) {
                this.finalCacheFile = files[i];
                LOG.info("Using final cache : " + files[i].getName());
                return;
            }
        }
        LOG.error("No final cache found");
    }

    public Map<String, LogWrapperCacheItem> getCache() {
        return cache;
    }

    /**
     * Item representation of the TMP loaded file
     * 
     * @author matej.bludsky
     *
     */
    public class LogWrapperCacheItem {

        private String message;
        private Date serverDate;

        public LogWrapperCacheItem(String message, Date serverDate) {
            super();
            this.message = message;
            this.serverDate = serverDate;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Date getServerDate() {
            return serverDate;
        }

        public void setServerDate(Date serverDate) {
            this.serverDate = serverDate;
        }

    }

}
