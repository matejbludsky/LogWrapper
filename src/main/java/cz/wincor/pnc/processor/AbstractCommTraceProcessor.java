package cz.wincor.pnc.processor;


import java.io.File;
import java.io.IOException;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;

import cz.wincor.pnc.types.HostTrxMessageType;

public abstract class AbstractCommTraceProcessor extends ParseTrace{

    

   
    
    protected volatile HostTrxMessageType hostTrxMessageType = null;
    
    public AbstractCommTraceProcessor(HostTrxMessageType hostTrxMessageType) {
        this.hostTrxMessageType = hostTrxMessageType;
    }

    public abstract int saveToDb(DB db) throws DBException, IOException;

    public HostTrxMessageType getHostTrxMessageType() {
        return hostTrxMessageType;
    }

    public void setHostTrxMessageType(HostTrxMessageType hostTrxMessageType) {
        this.hostTrxMessageType = hostTrxMessageType;
    }
    


    
   
}
