package cz.wincor.pnc.error;

/**
 * @author matej.bludsky
 * 
 *         Exception for soap ui transformation issues
 */

public class SOAPUITransformationException extends ProcessorException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String message = null;

    public SOAPUITransformationException(String message) {
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
