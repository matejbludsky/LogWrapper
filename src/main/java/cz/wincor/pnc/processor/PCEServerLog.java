package cz.wincor.pnc.processor;

import java.io.File;

/**
 * @author matej.bludsky
 * 
 *         POJO for log items
 */

public class PCEServerLog {

    private File logFile;
    private PCELogType logType;

    public PCEServerLog(File mergedLogFile, PCELogType logType) {
        super();
        this.logFile = mergedLogFile;
        this.logType = logType;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    public PCELogType getLogType() {
        return logType;
    }

    public void setLogType(PCELogType logType) {
        this.logType = logType;
    }

}
