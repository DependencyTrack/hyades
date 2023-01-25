package org.hyades.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.hyades.util.FileUtil.deleteFileAndDir;
import static org.hyades.util.FileUtil.getTempFileLocation;

public class FileUtilTest {

    @Test
    void testGetAndDeleteFile() throws IOException {
        Path tempPath = getTempFileLocation("test", ".test");
        deleteFileAndDir(tempPath);
        Assertions.assertThat(!tempPath.toFile().exists());
    }
}
