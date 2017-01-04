package cz.wincor.pnc.util;

import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;

import com.sun.management.OperatingSystemMXBean;

import cz.wincor.pnc.GUI.LogWrapperUIJFrame;

/**
 * @author matej.bludsky
 * 
 * 
 */

@SuppressWarnings("restriction")
public class SystemMonitoringThread extends Thread {

    private static final Logger LOG = Logger.getLogger(SystemMonitoringThread.class);

    public static int REFRESH_INTERVAL = 1000;

    @Override
    public void run() {
        super.run();
        try {

            while (!isInterrupted()) {
                String statistics = getCPUUsage() + " " + getAppMemory();
                LogWrapperUIJFrame.getInstance().refreshSystemDetails(statistics);
                LOG.debug("APP STAT : " + statistics);
                sleep(REFRESH_INTERVAL);
            }
        } catch (Exception e) {
            LOG.error("Cannot sleep" + e);
        }

    }

    private String getCPUUsage() {
        OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        String CPU = "CPU Usage : App : " + 100 * Math.round(bean.getProcessCpuLoad() * 100.0) / 100.0 + "% System : " + 100 * Math.round(bean.getSystemCpuLoad() * 100.0) / 100.0 + "%";
        return CPU;
    }

    private String getAppMemory() {
        Runtime runtime = Runtime.getRuntime();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        return "Memory usage : " + bytesIntoHumanReadable(memory);
    }

    private String bytesIntoHumanReadable(long bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;
        long terabyte = gigabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return (bytes / gigabyte) + " GB";

        } else if (bytes >= terabyte) {
            return (bytes / terabyte) + " TB";

        } else {
            return bytes + " Bytes";
        }
    }
}
