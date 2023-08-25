package org.hyades.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RepoAnalyzerUtilTest {

    @Test
    void testParsePurlWithValidPurl() {
        var parsedPurl = RepoAnalyzerUtil.parsePurl("pkg:maven/foo/bar@1.2.3");
        assertThat(parsedPurl.getType()).isEqualTo("maven");
        assertThat(parsedPurl.getNamespace()).isEqualTo("foo");
        assertThat(parsedPurl.getName()).isEqualTo("bar");
        assertThat(parsedPurl.getVersion()).isEqualTo("1.2.3");
    }

    @Test
    void testParsePurlWithInvalidPurl() {
        var thrown = assertThrows(IllegalStateException.class, () ->
                RepoAnalyzerUtil.parsePurl("invalid"));
        assertEquals("The provided PURL is invalid, even though it should have been\n" +
                "validated in a previous processing step\n", thrown.getMessage());
    }
}
