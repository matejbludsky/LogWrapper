package cz.wincor.pnc.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import cz.wincor.pnc.cache.LogWrapperCacheItem;
import cz.wincor.pnc.error.ProcessorException;
import cz.wincor.pnc.error.SOAPUITransformationException;
import cz.wincor.pnc.gui.component.DragAndDropPanel;
import cz.wincor.pnc.settings.LogWrapperSettings;
import cz.wincor.pnc.util.BindingTranslator;
import cz.wincor.pnc.util.BindingTranslator.BindingType;
import cz.wincor.pnc.util.BindingTranslator.EndPointType;
import cz.wincor.pnc.util.FileUtil;
import cz.wincor.pnc.util.SystemUtil;
import cz.wincor.pnc.util.TraceStringUtils;

/**
 * @author matej.bludsky
 * 
 *         Class for SOAP UI transformation running as thread
 */

public class SOAPUIExporter extends SwingWorker<Boolean, String> {
    private static final Logger LOG = Logger.getLogger(SOAPUIExporter.class);

    public static String V02_WSDL_LOCATION = "$V02_WSDL_LOCATION$";
    public static String PROJECT_NAME = "$PROJECT_NAME$";
    public static String TEST_STEPS = "$TEST_STEPS$";
    public static String TEST_SUITE_NAME = "$TEST_SUITE_NAME$";
    public static String SOAP_REQUEST = "$SOAP_REQUEST$";

    public static String BINDING = "$BINDING$";
    public static String OPERATION = "$OPERATION$";
    public static String REQUEST_NAME = "$REQUEST_NAME$";
    public static String UUID_REQUEST = "$UUID$";
    public static String ENDPOINT = "$ENDPOINT$";
    public static String TEST_STEP_NAME = "$TEST_STEP_NAME$";
    public static String TRACK2 = "$TRACK2$";
    
    public static String CARD_TRACK2_TAG ="<PreDefName>CARD_TRACK_2</PreDefName>";
    private List<LogWrapperCacheItem> cache = new ArrayList<LogWrapperCacheItem>();

    private String finalFilePath = LogWrapperSettings.normalizeDir(LogWrapperSettings.SOAPUI_FINAL_LOCATION);

    public SOAPUIExporter(List<LogWrapperCacheItem> cache, String finalPath) {
        super();
        this.cache = cache;
        if (finalPath != null) {
            this.finalFilePath = finalPath;
        }
    }

    /**
     * Method takes populated cache and transform it into soap ui format
     * 
     */
    public void process() throws ProcessorException {
        try {
            doInBackground();
        } catch (Exception e) {
            LOG.error("Cannot finish soapui export", e);
            throw new SOAPUITransformationException("Cannot trasform to SOAP UI");
        }
    }

    /**
     * determines endpoint type based of in memory settings
     * 
     * @return
     */
    private String loadEndPointType() {
        if (LogWrapperSettings.ENDPOINT_JBOSS) {
            return "JBOSS";
        } else {
            return "WAS";
        }

    }

    /**
     * Takes WSDLS and copy it with project template to final location replaces the $V02_WSDL_LOCATION$ with the
     * location absolute path
     * 
     * @throws IOException
     */
    private void prepareSoapUITemplate() throws IOException {

        setProgress(0);
        String templateName = "LogWrapper_" + UUID.randomUUID().toString().substring(0, 5) + "_" + loadEndPointType();

        String template = loadProjectTemplate();
        template = replaceProjectName(template, templateName);
        template = replaceTestSuiteName(template, templateName);
        template = replaceTrack2(template);
        template = replaceWSDLPaths(template, finalFilePath + "/WSDL/V02");
        template = replaceUUID(template);
        setProgress(25);
        template = replaceTestSteps(template, prepareTestSteps());

        if (LogWrapperSettings.SOAP_CLEAR_BEFORE) {
            FileUtil.clearDirectory(finalFilePath);
        }

        Files.createDirectories(Paths.get(finalFilePath));
        writeTemplate(template, finalFilePath, templateName + ".xml");

        LOG.info("Transformed SOAPUI template written to : " + finalFilePath);
        setProgress(100);

    }

    /**
     * Prepares the test step template for each message
     * 
     * @return
     * @throws IOException
     */
    private String prepareTestSteps() throws IOException {

        int progress = 0;

        StringBuilder suites = new StringBuilder();
        // for each message create test suite
        for (Iterator<LogWrapperCacheItem> iterator = cache.iterator(); iterator.hasNext();) {

            LogWrapperCacheItem cacheItem = iterator.next();
            String request = cacheItem.getMessage();
            // ignore responses
            if (!TraceStringUtils.isRequestMessage(request)) {
                continue;
            }

            try {
                String template = loadTestSuiteTemplate();

                BindingType binding = BindingTranslator.fromWSCCRequest(request);
                template = replaceBinding(template, binding.getBinding());
                template = replaceOperationAndTestStepName(template, request, binding);
                template = replaceRequestName(template, cacheItem.getMessageType());
                template = replaceUUID(template);
                template = replaceEndpoint(template, binding);
                request = replaceCardTrackData2(request);
                template = insertRequest(template, request);

                suites.append(template);
                suites.append(System.lineSeparator());

                progress = progress + 75 / cache.size();

                setProgress(progress);
                LOG.debug("Test Suite created : " + template);
            } catch (SOAPUITransformationException e) {
                LOG.error(e.getMessage());
                continue;
            }
        }
        return suites.toString();
    }

    /**
     * Loads soap ui project template
     * 
     * @return
     * @throws IOException
     */
    private String loadProjectTemplate() throws IOException {
        File file = new File(LogWrapperSettings.currentDir + "/template/emptyProjectSOAPUI.xml");

        String template = FileUtils.readFileToString(file);
        LOG.debug("Template loaded : " + file.getAbsolutePath());
        return template;
    }

    /**
     * Loads soap ui test step template
     * 
     * @return
     * @throws IOException
     */
    private String loadTestSuiteTemplate() throws IOException {
        File file = new File(LogWrapperSettings.currentDir + "/template/TestStepTemplate.xml");

        String template = FileUtils.readFileToString(file);
        LOG.debug("Template loaded : " + file.getAbsolutePath());
        return template;
    }

    private String replaceUUID(String template) {
        return template = template.replace(UUID_REQUEST, UUID.randomUUID().toString());
    }

    private String replaceTestSteps(String template, String suites) {
        return template = template.replace(TEST_STEPS, suites);
    }

    private String replaceTestSuiteName(String template, String message) {
        return template = template.replace(TEST_SUITE_NAME, message);
    }
    
    private String replaceTrack2(String template){
        if (LogWrapperSettings.CARD_DATA) {
            template = template.replace(TRACK2, LogWrapperSettings.CARD_DATA_VALUE);
        }
               
        return template;
        
    }

    private String replaceRequestName(String template, String messageType) {
        return template = template.replace(REQUEST_NAME, messageType);
    }

    private String replaceBinding(String template, String binding) {
        return template = template.replace(BINDING, binding);
    }

    private String insertRequest(String template, String request) {
        return template = template.replace(SOAP_REQUEST, SystemUtil.formatXML(request));
    }

    private String replaceCardTrackData2(String request) {
        StringBuilder outputTemplate = new StringBuilder();
        outputTemplate.append(request.substring(0, request.indexOf(CARD_TRACK2_TAG)));
        String output = request.substring(request.indexOf(CARD_TRACK2_TAG));
        output = output.replaceFirst("<Value>(.*?)</Value>", "<Value>\\${\\#Project\\#track2data}</Value>");
        outputTemplate.append(output);
        request = SystemUtil.formatXML(outputTemplate.toString());
        return request; 
    }
    
    private String replaceEndpoint(String template, BindingType binding) {
        String host = LogWrapperSettings.ENDPOINT_URL;

        String suffix = binding.getEndpointSuffix().get(EndPointType.JBOSS);
        if (LogWrapperSettings.ENDPOINT_WAS) {
            suffix = binding.getEndpointSuffix().get(EndPointType.WAS);
        }

        return template = template.replace(ENDPOINT, host + suffix);
    }

    private String replaceOperationAndTestStepName(String template, String request, BindingType binding) throws SOAPUITransformationException {
        String operation = BindingTranslator.determineOperation(binding, request);
        if (operation == null) {
            throw new SOAPUITransformationException("Cannot translate operation");
        }
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.substring(0, 5);
        String stepName = operation + "_" + uuid;

        template = template.replace(TEST_STEP_NAME, stepName);
        return template = template.replace(OPERATION, operation);
    }

    private void writeTemplate(String template, String finalFilePath, String name) throws IOException {
        FileUtils.writeStringToFile(new File(finalFilePath + "/" + name), template);
    }

    private String replaceProjectName(String template, String name) {
        return template = template.replace(PROJECT_NAME, name);
    }

    private String replaceWSDLPaths(String template, String path) {
        return template = template.replace(V02_WSDL_LOCATION, path);
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        prepareSoapUITemplate();
        return true;
    }

}
