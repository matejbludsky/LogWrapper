package cz.wincor.pnc.util;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.media.jai.PlanarImage;

import org.apache.log4j.Logger;

import com.mortennobel.imagescaling.ResampleOp;
import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;

import cz.wincor.pnc.cache.LevelDBCache;
import cz.wincor.pnc.gui.component.DragAndDropPanel;

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

        String fileNameAppendix = UUID.randomUUID().toString();

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

    public static BufferedImage resizeImage(double zoom, BufferedImage image) {
        System.out.println(zoom);
        ResampleOp resampleOp = new ResampleOp((int) (image.getWidth() * zoom), (int) (image.getHeight() * zoom));
        BufferedImage resizedIcon = resampleOp.filter(image, null);
        return resizedIcon;
    }

    public static BufferedImage loadTIFFImage(byte[] data) throws Exception {
        BufferedImage image = null;
        SeekableStream stream = new ByteArraySeekableStream(data);
        String[] names = ImageCodec.getDecoderNames(stream);
        ImageDecoder dec = ImageCodec.createImageDecoder(names[0], stream, null);
        RenderedImage im = dec.decodeAsRenderedImage();
        image = PlanarImage.wrapRenderedImage(im).getAsBufferedImage();
        return image;
    }

    /**
     * finds <Image> tag and converts base64 data into image as html
     */
    public static List<String> saveImages(String keyID) {
        List<String> imageLocations = new ArrayList<>();
        LOG.debug("Analysing preview area");
        // transform check base64 image to file
        imageLocations = saveImagesToFile(LevelDBCache.getInstance().get(keyID).getMessage());
        for (Iterator<String> iterator = imageLocations.iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            DragAndDropPanel.getInstance().logToTextArea("Image saved : " + string, true);
        }

        return imageLocations;
    }

}
