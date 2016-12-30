package cz.wincor.pnc.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import cz.wincor.pnc.GUI.DragAndDropPanel;
import cz.wincor.pnc.settings.LogWrapperSettings;

/**
 * @author matej.bludsky
 * 
 *         Utils class for system operations
 */

public class SystemUtil {

    private static final Logger LOG = Logger.getLogger(SystemUtil.class);

    /**
     * Opens image location via cmd in windows
     */
    public static void openImagesLocation() {
        try {
            String location = LogWrapperSettings.normalizeDir(LogWrapperSettings.IMAGES_LOCATION);
            Process builder = Runtime.getRuntime().exec("cmd /c start " + location);
            LOG.error("Location" + location + " opened in Windows Explorer");
        } catch (IOException e1) {
            DragAndDropPanel.logToTextArea("Cannot open images folder", true);
            LOG.error("Cannot open location for images", e1);
        }
    }

    /**
     * Opens soapui location via cmd in windows
     */
    public static void openSoapUIFinalLocation() {
        try {
            String location = LogWrapperSettings.normalizeDir(LogWrapperSettings.SOAPUI_FINAL_LOCATION);
            Process builder = Runtime.getRuntime().exec("cmd /c start " + location);
            LOG.error("Location" + location + " opened in Windows Explorer");
        } catch (IOException e1) {
            DragAndDropPanel.logToTextArea("Cannot open images folder", true);
            LOG.error("Cannot open location for images", e1);
        }
    }

    /**
     * Copy String into system clipboard
     * 
     * @param content
     */
    public static void copyToClipboard(String content) {
        StringSelection stringSelection = new StringSelection(content);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
        LOG.debug("Copied to clipboard");
    }

    /**
     * Formats given String into pretty print XML
     * 
     * @param input
     * @return
     */
    public static String formatXML(String input) {
        try {
            Document doc = DocumentHelper.parseText(input);
            StringWriter sw = new StringWriter();
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setNewLineAfterDeclaration(false);
            format.setSuppressDeclaration(true);
            format.setOmitEncoding(true);
            format.setIndent(true);
            format.setTrimText(true);
            format.setIndentSize(3);
            XMLWriter xw = new XMLWriter(sw, format);

            xw.write(doc);

            return sw.toString();
        } catch (Exception e) {
            LOG.error("Cannot pretty print message : " + input);
            return "";
        }
    }

    /**
     * Created folder for images
     * 
     * @param path
     */
    public static void createImagesDirectory(String path) {
        File dir = new File(path);
        dir.mkdirs();
    }

    /**
     * Saves image into the directory for images
     * 
     * @param image
     * @param name
     * @return
     * @throws IOException
     */
    public static String saveImage(String image, String name) throws IOException {

        String path = LogWrapperSettings.IMAGES_LOCATION;
        if (path == null || path.isEmpty()) {
            path = Paths.get("").toAbsolutePath().toString();
            path += "\\tmp\\images\\" + name + ".PNG";
        } else {
            String lastChar = path.substring(path.length() - 1, path.length());
            if (lastChar != "\\" && lastChar != "/") {
                path += "/";
            }
        }
        FileOutputStream fos = null;
        File f = null;
        try {
            createImagesDirectory(path);

            path += name + ".PNG";
            path = path.replace("\\", "/");

            byte[] decoded = Base64.decodeBase64(image);
            f = new File(path);
            f.createNewFile();

            fos = new FileOutputStream(f);
            fos.write(decoded);
        } catch (IOException e) {
            LOG.error("Cannot save image : " + path);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        return path;
    }

    /*
     * FUTURE public static void addToExplorerMenu() throws IOException { Reg reg = new
     * Reg("HKEY_CLASSES_ROOT\\*\\shell"); Reg.Key cp = reg.get("Open with LogWrapper");
     * 
     * if(cp==null){
     * 
     * }
     * 
     * Reg.Key sound = cp.getChild("Sound"); String beep = sound.get("Beep"); }
     * 
     * private static writeRegistryKey(){ Reg reg = new Reg(); Reg.Key key =
     * reg.add("HKEY_CLASSES_ROOT\\*\\shell\\Open with LogWrapper\\command");
     * 
     * key.put("default", "log"); reg.write(); }
     */
}
