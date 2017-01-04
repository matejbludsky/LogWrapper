package cz.wincor.pnc.error;

/**
 * @author matej.bludsky
 * 
 *         Exception for CommTrace loading
 */

public class CommTraceLoadException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String message;

    public CommTraceLoadException(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
