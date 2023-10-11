package org.hyades.util;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

public final class PurlUtil {

    private PurlUtil() {
    }

    /**
     * @param purl the purl string to parse
     * @return a PackageURL object
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

    /**
     * @param purl the purl string to parse
     * @return a PackageURL object
     */
    public static PackageURL parsePurlCoordinatesWithoutVersion(final String purl) {
        try {
            final var parsedPurl = new PackageURL(purl);
            return new PackageURL(parsedPurl.getType(), parsedPurl.getNamespace(),
                    parsedPurl.getName(), null, null, null);
        } catch (MalformedPackageURLException e) {
            throw new IllegalStateException("""
                    The provided PURL is invalid, even though it should have been
                    validated in a previous processing step
                    """, e);
        }
    }
}
