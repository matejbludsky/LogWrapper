package cz.wincor.pnc.processor;

import org.apache.log4j.Logger;

import cz.wincor.pnc.processor.impl.CommTraceProcessor;
import cz.wincor.pnc.processor.impl.TraceLogProcessor;

/**
 * 
 * @author matej.bludsky
 *
 *         Class is providing new log processor instance based on selected Type
 *
 */
public class ProcessorFactory {
    private static final Logger LOG = Logger.getLogger(ProcessorFactory.class);

    private static ProcessorFactory instance;

    private ProcessorFactory() {
    }

    /**
     * Returns instance of ProcessorFactory
     * 
     * @return
     */
    public static ProcessorFactory getInstance() {
        if (instance == null) {
            return new ProcessorFactory();
        }

        return instance;
    }

    /**
     * Returns right processor class
     * 
     * @param file
     * @return
     */
    public AbstractProcessor getProcessor(PCEServerLog log) {

        switch (log.getLogType()) {
        case COMMTRACE:
            LOG.debug("Instantiating CommTrace processor");
            return new CommTraceProcessor(log.getLogFile());

        case TRACE:
            LOG.debug("Instantiating TraceLog processor");
            return new TraceLogProcessor(log.getLogFile());

        default:
            LOG.error("Unsupported processor type");
            break;
        }

        return null;

    }

}
