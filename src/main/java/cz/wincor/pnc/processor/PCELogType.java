package cz.wincor.pnc.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.wincor.pnc.settings.LogWrapperSettings;

/**
 * Processor types FUTURE use of other traces
 * 
 * @author matej.bludsky
 *
 */
public enum PCELogType {

    COMMTRACE, TRACE, UNSUPPORTED;

    public static PCELogType fromFileName(String name) {

        Pattern p1 = Pattern.compile(LogWrapperSettings.COMMTRACE_NAME_REGEXP);
        Matcher m1 = p1.matcher(name);

        if (m1.matches()) {
            return COMMTRACE;
        }

        Pattern p2 = Pattern.compile(LogWrapperSettings.TRACE_NAME_REGEXP);
        Matcher m2 = p2.matcher(name);

        if (m2.matches()) {
            return PCELogType.TRACE;
        }

        return PCELogType.UNSUPPORTED;

    }

}
