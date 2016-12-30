package cz.wincor.pnc.error;

import java.io.File;

/**
 * 
 * @author matej.bludsky
 *
 *         Exception for importing files
 */
public class FileImportException extends ProcessorException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final String MESSAGE = "Cannot import file";
    private File fileToImport;

    public String getErrorMessage() {
        if (fileToImport == null) {
            return MESSAGE;
        }

        return MESSAGE + fileToImport.getAbsolutePath();

    }

}
