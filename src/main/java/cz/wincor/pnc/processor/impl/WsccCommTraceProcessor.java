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

public class WsccCommTraceProcessor extends AbstractCommTraceProcessor{

    private static final Logger LOG = Logger.getLogger(WsccCommTraceProcessor.class);
    private HostTrxMessageType hostTrxMessageType;
    
    //public static final String WSCC_TAG = "|>WS|";
    
    
    public WsccCommTraceProcessor(HostTrxMessageType hostTrxMessageType) {
        super(hostTrxMessageType);
        this.hostTrxMessageType = hostTrxMessageType;
    }

    @Override
    public int saveToDb(DB db) throws DBException, IOException {
        // TODO Auto-generated method stub
        
        LOG.info("-------------------------------------------");
        LOG.info(hostTrxMessageType.getMessage());
     
        String key = UUID.randomUUID().toString();
        String message = filterSOAPMessage(hostTrxMessageType.getMessage());
        
        String serverDate = "" ;
        
        serverDate = hostTrxMessageType.getMessage().substring(hostTrxMessageType.getMessage().indexOf(HostTrxMessageEnum.WSCC.getPattern()) + HostTrxMessageEnum.WSCC.getPattern().length(), hostTrxMessageType.getMessage().indexOf(HostTrxMessageEnum.WSCC.getPattern()) + HostTrxMessageEnum.WSCC.getPattern().length() + 23);
        
        LogWrapperCacheItem item = parseMessage(key, message);
        item.setServerDate(TraceStringUtils.getDateFromString(serverDate));
        item.setHostTrxMessageType(hostTrxMessageType.getHostTrxMessageEnum());
        db.put(key.getBytes(), LevelDBCache.getInstance().convertToBytes(item));
        return 0;

    }

    @Override
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
    

}
