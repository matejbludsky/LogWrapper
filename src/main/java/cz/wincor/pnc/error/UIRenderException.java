package cz.wincor.pnc.error;

/**
 * @author matej.bludsky
 * 
 *         Exception for UI rendering issues
 */

public class UIRenderException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final int ID = 100;

    private String message;
    private Exception originalException;

    public UIRenderException(String message) {
        super();
        this.message = message;
    }

    public UIRenderException(String message, Exception originalException) {
        super();
        this.message = message;
        this.originalException = originalException;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getOriginalException() {
        return originalException;
    }

    public void setOriginalException(Exception originalException) {
        this.originalException = originalException;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getID() {
        return ID;
    }

    @Override
    public String toString() {
        return "ERROR" + ID + " : " + message;
    }

}
