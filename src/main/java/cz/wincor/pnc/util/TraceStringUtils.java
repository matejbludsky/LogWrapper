package cz.wincor.pnc.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author matej.bludsky
 * 
 *         Util class for comm trace operations
 * 
 */

public class TraceStringUtils {

    private static final Logger LOG = Logger.getLogger(TraceStringUtils.class);

    public volatile static SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS  dd.MM.yyyy");

    public static String[] importDateFormats = { "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd, HH:mm:ss.SSS" };

    private static boolean isNull(String message) {
        if (message == null) {
            return true;
        }
        return false;
    }

    public static Date getDateFormatDate(String date) {
        Date parsedDate = null;
        for (int i = 0; i < importDateFormats.length; i++) {
            try {
                parsedDate = new SimpleDateFormat(importDateFormats[i]).parse(date);
            } catch (ParseException e) {
                LOG.debug("Cannot parse date from : " + date + " with pattern : " + importDateFormats[i]);
            }
        }

        return parsedDate;
    }

    /**
     * Determines if the message is SOAP request
     * 
     * @param message
     * @return
     */
    public static boolean isWSCCMessage(String message) {

        if (message.contains("<ns2:")) {
            return true;
        }

        return false;
    }

    /**
     * Determines if the message is SOAP request
     * 
     * @param message
     * @return
     */
    public static boolean isRequestMessage(String message) {

        if (message.contains("Request")) {
            return true;
        }

        return false;
    }

    /**
     * Extracts ATM date from the XML tag
     * 
     * Example XML entry
     * 
     * <ClientRequestTime>1469486247968</ClientRequestTime>
     * 
     * @param message
     */
    public static Date extractDateDate(String key, String message) {
        if (isNull(message)) {
            return null;
        }
        Date time = null;
        try {
            int startTag = message.indexOf("<ClientRequestTime>");
            int endTag = message.indexOf("</ClientRequestTime>");

            time = new Date(Long.parseLong(message.substring(startTag + 19, endTag)));
            LOG.debug("Row id : " + key + " Extracted ATM date " + time);
        } catch (Exception e) {
            LOG.error("Cannot extract ATM Date, message : " + key);
        }
        return time;
    }

    /**
     * Extracts ATM date from the XML tag
     * 
     * Example XML entry
     * 
     * <ClientRequestTime>1469486247968</ClientRequestTime>
     * 
     * @param message
     */
    public static String extractDateString(String key, String message) {
        if (isNull(message)) {
            return null;
        }
        String time;
        try {
            int startTag = message.indexOf("<ClientRequestTime>");
            int endTag = message.indexOf("</ClientRequestTime>");

            time = dateFormatter.format(Long.parseLong(message.substring(startTag + 19, endTag)));
            LOG.debug("Row id : " + key + " Extracted ATM date " + time);
        } catch (Exception e) {
            LOG.error("Cannot extract ATM Date, message : " + key);
            time = "-";
        }
        return time;
    }

    /**
     * Extracts ATM ID from the XML tag
     * 
     * Example XML entry
     * 
     * <ClientId>66000</ClientId>
     * 
     * @param message
     */
    public static String extractATMID(String message) {
        if (isNull(message)) {
            return null;
        }
        String atmId = "-";
        try {
            int startTag = message.indexOf("<ClientId>");
            int endTag = message.indexOf("</ClientId>");

            atmId = message.substring(startTag + 10, endTag);
            LOG.debug("Extracted ATM ID " + atmId);
        } catch (Exception e) {
            LOG.error("Cannot extract ATM ID");
        }
        return atmId;
    }

    /**
     * Extracts ATM ID from the XML tag
     * 
     * Example XML entry
     * 
     * <ClientId>66000</ClientId>
     * 
     * @param message
     */
    public static String extractClientRequestNumber(String message) {
        if (isNull(message)) {
            return null;
        }
        String reqNumber = null;
        try {
            int startTag = message.indexOf("<ClientRequestNumber>");
            int endTag = message.indexOf("</ClientRequestNumber>");

            reqNumber = message.substring(startTag + 22, endTag);
            LOG.debug("Extracted client sequence ID " + reqNumber);
        } catch (Exception e) {
            LOG.error("Cannot extract ATM ID");
        }
        return reqNumber;
    }

    /**
     * Extracts ATM ID from the XML tag
     * 
     * Example XML entry
     * 
     * <ClientId>66000</ClientId>
     * 
     * @param message
     */
    public static List<String> extractImage(String message) {
        if (isNull(message)) {
            return null;
        }
        List<String> base64Images = new ArrayList<>();

        int imageStart = 0;
        int imageEnd = 0;

        String content = message;

        do {

            imageStart = content.indexOf("<Image>");
            imageEnd = content.indexOf("</Image>");

            if (imageStart != -1 && imageEnd != -1) {
                // got image
                base64Images.add(new String(content.substring(imageStart + 7, imageEnd)));
                content = content.substring(imageEnd + 8, content.length());
            }

        } while (imageStart != -1 && imageEnd != -1);

        return base64Images;
    }

    /**
     * Extracts message type
     * 
     * Example XML entry
     * 
     * <ns2:EventRequest xmlns:ns2="http://wincornixdorf.com/pce/server/wsclientconnector/v02/core"> <Mixins>
     * <ClassName>cz.wincor.pnc.bobjs.PNCRequestMixin</ClassName> <Properties> <Name>AtmSequenceNumber</Name>
     * <String>----</String> </Properties> </Mixins> <ClientId>66000</ClientId>
     * <ClientRequestNumber>1479240827965</ClientRequestNumber>
     * <ClientRequestRepeatCounter>0</ClientRequestRepeatCounter> <ClientRequestTime>1469486247968</ClientRequestTime>
     * <EventReason>DEVICESTATE</EventReason> <WkstState>STARTUP</WkstState> </ns2:EventRequest>
     * 
     * Message type : EventRequest
     * 
     * @param message
     */
    public static String extractMessageType(String key, String message) {
        if (isNull(message)) {
            return null;
        }

        String messageType = "-";
        try {
            int startTag = message.indexOf("<ns2:");
            int endTag = message.indexOf("xmlns:ns2");

            messageType = message.substring(startTag + 5, endTag);
            LOG.debug("Row id : " + key + " Extracted Message Type " + messageType);

        } catch (Exception e) {
            LOG.error("Cannot extract Message Type");
        }
        return messageType;
    }

    /**
     * Extracts ATM ID from the XML tag
     * 
     * Example XML entry for BRASS machine
     * 
     * <Properties> <Name>AtmSequenceNumber</Name> <String>1145</String> </Properties>
     * 
     * SEQ number can be ---- this needs to be translated to -1
     * 
     * MVS machines are storing SEQ numbers into ClientRequestNumber
     * 
     * Example >
     * 
     * <ClientRequestNumber>1145</ClientRequestNumber>
     * 
     * Method checks if there is AtmSequenceNumber present, if not it takes the SEQ number out of ClientRequestNumber
     * Tag
     * 
     * @param message
     */
    public static String extractSEQNumber(String key, String message) {
        if (isNull(message)) {
            return null;
        }
        String SEQNumber = "-";

        try {
            int startTag = -1;
            int endTag = -1;
            if (message.indexOf("AtmSequenceNumber") != -1) {
                // BRASS
                startTag = message.indexOf("<Properties>");
                endTag = message.indexOf("</Properties>");

                String mixinString = message.substring(startTag, endTag);

                startTag = mixinString.indexOf("<String>");
                endTag = mixinString.indexOf("</String>");

                SEQNumber = mixinString.substring(startTag + 8, endTag);

            } else {
                // MVS
                startTag = message.indexOf("<ClientRequestNumber>");
                endTag = message.indexOf("</ClientRequestNumber>");

                if (startTag == -1 || endTag != -1) {
                    SEQNumber = message.substring(startTag, endTag);
                }
            }

            if (SEQNumber.equalsIgnoreCase("----")) {
                SEQNumber = "-1";
            }

            try {
                int test = Integer.parseInt(SEQNumber);
            } catch (Exception e) {
                SEQNumber = "-";
            }

            LOG.debug("Row id : " + key + " Extracted SEQ NUMBER " + SEQNumber);
        } catch (Exception e) {
            LOG.error("Cannot extract SEQ Number");
        }
        return SEQNumber;
    }

    /**
     * Determines if this transaction is InfoTransaction
     * 
     * Example XML entry
     * 
     * <ClientId>66000</ClientId>
     * 
     * @param message
     */
    public static boolean isInfoTransaction(String key, String message) {
        if (isNull(message)) {
            return false;
        }

        boolean infoTransaction = false;

        if (message.toLowerCase().contains("response")) {
            return true;
        }

        LOG.debug("Row id : " + key + " InfoTransaction :  " + infoTransaction);
        return infoTransaction;
    }

}
