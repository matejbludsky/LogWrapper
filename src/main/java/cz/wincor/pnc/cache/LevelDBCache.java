package cz.wincor.pnc.cache;

import java.io.File;

/**
@author matej.bludsky


*/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.nustaq.serialization.FSTConfiguration;

import cz.wincor.pnc.settings.LogWrapperSettings;
import cz.wincor.pnc.util.FileUtil;

/**
 * @author matej.bludsky
 * 
 * 
 */

public class LevelDBCache {
    private static final Logger LOG = Logger.getLogger(LevelDBCache.class);
    public static LevelDBCache instance = null;
    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    private LevelDBCache() {
        LOG.info("LevelDBCache started");
    }

    public void initialize() throws IOException {
        FileUtil.clearDirectory(LogWrapperSettings.LMDB_LOCATION);
        Files.createDirectories(Paths.get(LogWrapperSettings.LMDB_LOCATION));
        LOG.info("LevelDBCache Initialized");
    }

    /**
     * Gets instance of LMDBCache
     * 
     * @return
     */
    public static LevelDBCache getInstance() {
        if (LevelDBCache.instance == null) {
            instance = new LevelDBCache();
        }

        return instance;
    }

    /**
     * Opens instance of database
     * 
     * @throws IOException
     */
    public synchronized DB openDatabase() throws IOException {
        DBFactory factory = JniDBFactory.factory;
        Options options = new Options();
        options.createIfMissing(true);
        options.cacheSize(LogWrapperSettings.LEVEL_DB_CACHE_ALLOCATION * 1048576); // 100MB cache
        //options.compressionType(CompressionType.NONE);
        return factory.open(new File(LogWrapperSettings.LMDB_LOCATION), options);
    }

    /**
     * Puts key and serialized value into the DB
     * 
     * @param key
     * @param item
     * @throws IOException
     */
    public synchronized void put(String key, LogWrapperCacheItem item) throws IOException {
        DB db = null;
        try {
            db = openDatabase();
            db.put(key.getBytes(), convertToBytes(item));
        } catch (Exception e) {
            LOG.error("Cannnot put entry", e);
        } finally {
            try {
                db.close();
            } catch (IOException e) {
                LOG.error("Cannot close DB", e);
            }
        }

    }

    /**
     * Puts key and serialized value into the DB
     * 
     * @param key
     * @param item
     * @throws IOException
     */
    public synchronized void putBatch(WriteBatch batch) throws IOException {
        DB db = null;
        try {
            db = openDatabase();
            db.write(batch);
        } catch (Exception e) {
            LOG.error("Cannnot put entry", e);
        } finally {
            try {
                db.close();
            } catch (IOException e) {
                LOG.error("Cannot close DB", e);
            }
        }
    }

    /**
     * returns LogWrapperCacheItem based on key
     * 
     * @param key
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public synchronized LogWrapperCacheItem get(String key) {

        DB db = null;
        try {
            db = openDatabase();
            byte[] value = db.get(key.getBytes());
            db.close();
            return convertFromBytes(value);
        } catch (Exception e) {
            LOG.error("Cannot get entry from key : " + key, e);
            try {
                db.close();
            } catch (IOException e1) {
                LOG.error("Cannot close DB", e1);
            }
            return null;
        }
    }

    public byte[] convertToBytes(Object object) throws IOException {
        byte barray[] = conf.asByteArray(object);
        return barray;

    }

    public LogWrapperCacheItem convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        return (LogWrapperCacheItem) conf.asObject(bytes);
    }
}
