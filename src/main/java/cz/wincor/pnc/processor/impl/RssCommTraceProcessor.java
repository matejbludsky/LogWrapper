package cz.wincor.pnc.processor.impl;



import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;

import cz.wincor.pnc.cache.LevelDBCache;
import cz.wincor.pnc.cache.LogWrapperCacheItem;
import cz.wincor.pnc.processor.AbstractCommTraceProcessor;
import cz.wincor.pnc.types.HostTrxMessageEnum;
import cz.wincor.pnc.types.HostTrxMessageType;
import cz.wincor.pnc.util.TraceStringUtils;

public class RssCommTraceProcessor extends AbstractCommTraceProcessor{
    
    private HostTrxMessageType hostTrxMessageType;
    private static final Logger LOG = Logger.getLogger(RssCommTraceProcessor.class);
    
    
    public RssCommTraceProcessor(HostTrxMessageType hostTrxMessageType) {
        super(hostTrxMessageType);
        this.hostTrxMessageType = hostTrxMessageType;
        // TODO Auto-generated constructor stub
    }
    
    
    @Override
    protected LogWrapperCacheItem parseMessage(String key, String message) {

        LogWrapperCacheItem item = new LogWrapperCacheItem();

        String SEQNumber = TraceStringUtils.extractSEQNumber(key, message);
        // Date clientDate = 0l;
        // if(TraceStringUtils.extractClientDateToDate(key, message)!=null)
        Date clientDate = TraceStringUtils.extractClientDateToDate(key, message);

        String ATMId = TraceStringUtils.extractATMID(message);
        String messageType = TraceStringUtils.extractMessageTypeRSS(key, message);
        boolean infoTransaction = TraceStringUtils.isInfoTransaction(key, message, messageType);

        item.setSEQNumber(SEQNumber);
        item.setClientDate(clientDate);
        item.setATMId(ATMId);
        item.setMessageType(messageType);
        item.setInfoTransaction(infoTransaction);
        item.setMessage(message);

        return item;

    }
    
    @Override
    public int saveToDb(DB db) throws DBException, IOException {
        String key = UUID.randomUUID().toString();
        String message = filterSOAPMessage(hostTrxMessageType.getMessage());
        String serverDate = hostTrxMessageType.getMessage().substring(hostTrxMessageType.getMessage().indexOf(HostTrxMessageEnum.RSS.getPattern()) + HostTrxMessageEnum.RSS.getPattern().length(), hostTrxMessageType.getMessage().indexOf(HostTrxMessageEnum.RSS.getPattern()) + HostTrxMessageEnum.RSS.getPattern().length() + 23);
        LogWrapperCacheItem item = parseMessage(key, message);
        item.setServerDate(TraceStringUtils.getDateFromString(serverDate));
        item.setHostTrxMessageType(hostTrxMessageType.getHostTrxMessageEnum());
        db.put(key.getBytes(), LevelDBCache.getInstance().convertToBytes(item));
        return 0;
    }
    
}
