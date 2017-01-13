package cz.wincor.pnc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * @author matej.bludsky
 * 
 *         Util class for file operations
 */

public class FileUtil {

    private static final Logger LOG = Logger.getLogger(FileUtil.class);

    public static void clearDirectory(String path) throws IOException {
        FileUtil.delete(new File(path));
    }

    public static void delete(File file) throws IOException {

        if (file.isDirectory()) {
            // directory is empty, then delete it
            if (file.list().length == 0) {
                file.delete();
                LOG.debug("Directory is deleted : " + file.getAbsolutePath());

            } else {
                // list all the directory contents
                String files[] = file.list();
                for (String temp : files) {
                    // construct the file structure
                    File fileDelete = new File(file, temp);
                    // recursive delete
                    delete(fileDelete);
                }
                // check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    LOG.debug("Directory is deleted : " + file.getAbsolutePath());
                }
            }
        } else {
            // if file, then delete it
            file.delete();
            LOG.debug("File is deleted : " + file.getAbsolutePath());
        }
    }

    /**
     * Copies all content from folder to folder
     * 
     * @param src
     * @param dest
     * @throws IOException
     */
    public static void copyFolder(File src, File dest) throws IOException {

        if (src.isDirectory()) {
            // if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
                LOG.debug("Directory copied from " + src + "  to " + dest);
            }

            // list all the directory contents
            String files[] = src.list();
            for (String file : files) {
                // construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // recursive copy
                copyFolder(srcFile, destFile);
            }

        } else {
            // if file, then copy it
            // Use bytes stream to support all file types
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];

                int length;
                // copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } catch (Exception e) {
                LOG.error("Cannot copy file ");
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }

            LOG.debug("File copied from " + src + " to " + dest);
        }
    }

}
