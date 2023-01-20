package org.acme.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    public static void deleteFileAndDir(Path filePath) {
        try {
            boolean fileDeleted = filePath.toFile().delete();
            if (fileDeleted) {
                boolean dirDeleted = filePath.getParent().toFile().delete();
                if (!dirDeleted) {
                    LOGGER.info("Error while deleting the directory path %s", filePath.getParent().getFileName().toString());
                }
            }
        } catch (Exception e) {
            LOGGER.info("Error while deleting the file path %s : %s", filePath.getFileName().toString(), e);
        }
    }

    public static Path getTempFileLocation(String dirName, String fileSuffix) throws IOException {
        Path tempDir = Files.createTempDirectory(dirName);
        Path tempFile = Files.createTempFile(tempDir, "", fileSuffix);
        return tempFile;
    }

    /**
     * Writes the modification time to a timestamp file
     * @param file the file
     * @param modificationTime the time of the last update
     */
    public static void writeTimeStampFile(File file, Long modificationTime)
    {
        FileWriter writer = null;
        try {
            writer= new FileWriter(file);
            writer.write(Long.toString(modificationTime));
        }
        catch (IOException ex) {
            LOGGER.error("An error occurred writing time stamp file", ex);
        }
        finally {
            close(writer);
        }
    }

    /**
     * Closes a closable object.
     * @param object the object to close
     */
    public static void close(Closeable object) {
        if (object != null) {
            try {
                object.close();
            } catch (IOException e) {
                LOGGER.warn("Error closing stream", e);
            }
        }
    }
}
