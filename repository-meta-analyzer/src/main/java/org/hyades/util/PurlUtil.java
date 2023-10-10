package org.hyades.util;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

public final class PurlUtil {

    private PurlUtil() {
    }

    /**
     * @param purl the purl string to parse
     * @return a PackageURL object
     * @since 3.1.0
     */
    public static PackageURL parsePurlCoordinates(final String purl) {
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            throw new IllegalStateException("""
                    The provided PURL is invalid, even though it should have been
                    validated in a previous processing step
                    """, e);
        }
    }
}
