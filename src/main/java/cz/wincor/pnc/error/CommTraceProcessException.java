package cz.wincor.pnc.error;

/**
 * 
 * @author matej.bludsky
 *
 *         Exception for errors while processing CommTrace files
 */
public class CommTraceProcessException extends ProcessorException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String message = null;

    public CommTraceProcessException(String message) {
        super();
        this.message = message;
    }

}
