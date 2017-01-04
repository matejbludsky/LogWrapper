package cz.wincor.pnc.error;

/**
 * 
 * @author matej.bludsky
 *
 *         Exception for errors while processing CommTrace files
 */
public class TraceLoadingException extends ProcessorException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String message = null;

    public TraceLoadingException(String message) {
        super();
        this.message = message;
    }

}
