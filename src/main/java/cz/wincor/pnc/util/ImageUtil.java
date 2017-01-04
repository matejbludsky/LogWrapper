package cz.wincor.pnc.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import cz.wincor.pnc.GUI.DragAndDropPanel;
import cz.wincor.pnc.cache.DataCache;
import cz.wincor.pnc.settings.LogWrapperSettings;

/**
 * @author matej.bludsky
 * 
 *         Utils class for operations with images
 */

public class ImageUtil {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ImageUtil.class);

    /**
     * Saves images found in the given string to the folder based on settings
     * 
     * @param content
     * @return
     */
    public static List<String> saveImagesToFile(String content) {

        String fileNameAppendix = TraceStringUtils.extractClientRequestNumber(content);

        if (fileNameAppendix == null) {
            fileNameAppendix = UUID.randomUUID().toString();
        }
        List<String> imagePaths = new ArrayList<>();

        List<String> images = TraceStringUtils.extractImage(content);
        int iteration = 0;
        for (Iterator<String> iterator = images.iterator(); iterator.hasNext();) {
            // base64 image data
            String image = (String) iterator.next();
            try {
                String path = SystemUtil.saveImage(image, fileNameAppendix + "_" + iteration);
                imagePaths.add(path);
                iteration++;
            } catch (IOException e) {
                LOG.error("Cannot save image");
            }
        }

        return imagePaths;

    }

    /**
     * finds <Image> tag and converts base64 data into image as html
     */
    public static void saveImages(String keyID) {
        if (LogWrapperSettings.IMAGES_SAVE) {
            LOG.debug("Analysing preview area");
            // transform check base64 image to file
            List<String> imageLocations = saveImagesToFile(DataCache.getInstance().getCache().get(keyID));

            for (Iterator<String> iterator = imageLocations.iterator(); iterator.hasNext();) {
                String string = (String) iterator.next();
                DragAndDropPanel.logToTextArea("Image saved : " + string, true);
            }
        }
    }

}
