package cz.wincor.pnc.processor;

import java.util.Date;

import cz.wincor.pnc.cache.LogWrapperCacheItem;
import cz.wincor.pnc.util.TraceStringUtils;

public abstract class  ParseTrace {
    
    public static final String[] SOAP_HEADERS = new String[] { "<soap:Envelope", "<soapenv:Envelope", "<S:Envelope","<SOAP-ENV:Envelope" };
    public static final String[] SOAP_FOOTER = new String[] { "</soapenv:Envelope>", "</soap:Envelope", "</S:Envelope>" ,"</SOAP-ENV:Envelope>"};
    
    /**
     * Filters only SOAP messages from the String line content
     * 
     * @param line
     * @return
     */
    protected  String filterSOAPMessage(String line) {

        int indexOfSOAPHeader = -1;
        int closingTagSOAPHeader = -1;

        int footerSubstringLength = 0;

        // HEADERS
        for (String header : SOAP_HEADERS) {
            int index = findIdexOf(header, line);
            if (index != -1) {
                indexOfSOAPHeader = index;

            }
        }

        // FOOTERS
        for (String footer : SOAP_FOOTER) {
            int index = findIdexOf(footer, line);
            if (index != -1) {
                closingTagSOAPHeader = index;
                footerSubstringLength = footer.length();

            }
        }

        if (indexOfSOAPHeader == -1 || closingTagSOAPHeader == -1) {
            return null;
        }

        String message = line.substring(indexOfSOAPHeader, closingTagSOAPHeader + footerSubstringLength);

        if (!message.endsWith(">")) {
            message += ">";
        }

        return message;

    }
    
    /**
     * returns first index of given string
     * 
     * @param substring
     * @param message
     * @return
     */
    protected  int findIdexOf(String substring, String message) {
        return message.indexOf(substring);
    }
    
    /**
     * provides LogWrapperCacheItem filled with preparsed items that are shared between processors
     * 
     * @param key
     * @param message
     * @return
     */
    protected abstract LogWrapperCacheItem parseMessage(String key, String message);    


}
