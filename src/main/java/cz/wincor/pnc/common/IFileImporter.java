package cz.wincor.pnc.common;

import java.io.File;
import java.util.List;

import cz.wincor.pnc.error.FileImportException;

/**
 * 
 * @author matej.bludsky
 *
 *         Interface for drag and drop imported component
 */
public interface IFileImporter {

    /**
     * Method will begin action of importing the files and will call proper Log Processor to trasform log into SOAPUI
     * test suite definition
     * 
     * @param files
     * @throws FileImportException
     */
    void importFiles(List<File> files) throws FileImportException;

}
