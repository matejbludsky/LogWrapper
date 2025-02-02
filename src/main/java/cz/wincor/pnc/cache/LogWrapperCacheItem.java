package cz.wincor.pnc.cache;

import java.io.Serializable;
import java.util.Date;

import cz.wincor.pnc.types.HostTrxMessageEnum;

/**
 * Item representation of the TMP loaded file
 * 
 * @author matej.bludsky
 *
 */
public class LogWrapperCacheItem implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1239084122340923L;
    private String message;
    private Date serverDate;
    private String SEQNumber;
    private Date clientDate;
    private String ATMId;
    private String messageType;
    private boolean infoTransaction = false;
    private HostTrxMessageEnum hostTrxMessageType;

    public LogWrapperCacheItem(String message, Date serverDate, String sEQNumber, Date clientDate, String aTMId, HostTrxMessageEnum hostTrxMessageType ,String messageType, boolean infoTransaction) {
        super();
        this.message = message;
        this.serverDate = serverDate;
        SEQNumber = sEQNumber;
        this.clientDate = clientDate;
        ATMId = aTMId;
        this.messageType = messageType;
        this.infoTransaction = infoTransaction;
        this.hostTrxMessageType = hostTrxMessageType;
       
    }

    public LogWrapperCacheItem() {
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

    public String getSEQNumber() {
        return SEQNumber;
    }

    public void setSEQNumber(String sEQNumber) {
        SEQNumber = sEQNumber;
    }

    public Date getClientDate() {
        return clientDate;
    }

    public void setClientDate(Date clientDate) {
        this.clientDate = clientDate;
    }

    public String getATMId() {
        return ATMId;
    }

    public void setATMId(String aTMId) {
        ATMId = aTMId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isInfoTransaction() {
        return infoTransaction;
    }

    public void setInfoTransaction(boolean infoTransaction) {
        this.infoTransaction = infoTransaction;
    }

    public HostTrxMessageEnum getHostTrxMessageType() {
        return hostTrxMessageType;
    }

    public void setHostTrxMessageType(HostTrxMessageEnum hostTrxMessageType) {
        this.hostTrxMessageType = hostTrxMessageType;
    }

}