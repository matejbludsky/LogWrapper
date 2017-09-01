package cz.wincor.pnc.processor;

import org.apache.log4j.Logger;

import cz.wincor.pnc.processor.impl.CommTraceProcessor;
import cz.wincor.pnc.processor.impl.RssCommTraceProcessor;
import cz.wincor.pnc.processor.impl.TraceLogProcessor;
import cz.wincor.pnc.processor.impl.WsccCommTraceProcessor;
import cz.wincor.pnc.types.HostTrxMessageEnum;
import cz.wincor.pnc.types.HostTrxMessageType;
import cz.wincor.pnc.types.PCEServerLog;

public class CommTraceProcessorFactory {
    private static final Logger LOG = Logger.getLogger(ProcessorFactory.class);

    private static CommTraceProcessorFactory instance;

    private CommTraceProcessorFactory() {
    }

    /**
     * Returns instance of ProcessorFactory
     * 
     * @return
     */
    public static CommTraceProcessorFactory getInstance() {
        if (instance == null) {
            return new CommTraceProcessorFactory();
        }

        return instance;
    }

    /**
     * Returns right processor class
     * 
     * @param file
     * @return
     */
    public AbstractCommTraceProcessor getProcessor(HostTrxMessageType hostTrx) {

        switch (hostTrx.getHostTrxMessageEnum()) {
        case RSS:
            LOG.debug("Instantiating CommTrace processor");
            return new RssCommTraceProcessor(hostTrx);
        case WSCC:
            LOG.debug("Instantiating CommTrace processor");
            return new WsccCommTraceProcessor(hostTrx);
        default:
            LOG.error("Unsupported processor type");
            break;
        }

        return null;

    }

}
