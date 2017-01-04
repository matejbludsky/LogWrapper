package cz.wincor.pnc.cache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import cz.wincor.pnc.error.CommTraceLoadException;
import cz.wincor.pnc.processor.AbstractProcessor;
import cz.wincor.pnc.settings.LogWrapperSettings;

/**
 * @author matej.bludsky
 * 
 * 
 */

public class DataCache {

    private static final Logger LOG = Logger.getLogger(DataCache.class);
    public static DataCache instance = null;
    private Map<String, String> cache = new HashMap<>();
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

        clearAll();
        findFinalFile();

        if (finalCacheFile == null) {
            LOG.error("Cannot load cache, File is null");
            throw new CommTraceLoadException("File cannot be null");
        }

        LOG.info("Preparing cache for : " + finalCacheFile.getName());
        LineIterator it;
        it = FileUtils.lineIterator(finalCacheFile, "UTF-8");
        try {
            while (it.hasNext()) {
                String line = it.nextLine();
                String[] entry = line.split(AbstractProcessor.SEPARATOR);
                if (entry == null || entry.length != 2) {
                    LOG.error("Cannot read entry : " + entry);
                    continue;
                }

                String key = entry[0];
                String message = entry[1];
                cache.put(key, message);
            }
        } finally {
            LineIterator.closeQuietly(it);
            LOG.info("Cache Prepared");
        }
    }

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

    public Map<String, String> getCache() {
        return cache;
    }

}
