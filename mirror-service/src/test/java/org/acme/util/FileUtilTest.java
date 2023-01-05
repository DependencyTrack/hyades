package org.acme.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.acme.util.FileUtil.deleteFileAndDir;
import static org.acme.util.FileUtil.getTempFileLocation;

public class FileUtilTest {

    @Test
    void testGetAndDeleteFile() throws IOException {
        Path tempPath = getTempFileLocation("test", ".test");
        deleteFileAndDir(tempPath);
        Assertions.assertThat(!tempPath.toFile().exists());
    }
}
