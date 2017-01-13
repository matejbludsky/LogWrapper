package cz.wincor.pnc.error;

/**
 * @author matej.bludsky
 * 
 *         Generic exception for processing exceptions
 * 
 */

public class ProcessorException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    String message;

    public ProcessorException(String message) {
        super();
        this.message = message;
    }

    public ProcessorException() {
        super();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
