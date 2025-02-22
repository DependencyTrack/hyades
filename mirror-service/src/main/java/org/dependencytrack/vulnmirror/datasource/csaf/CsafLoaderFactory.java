package org.dependencytrack.vulnmirror.datasource.csaf;

import io.github.csaf.sbom.retrieval.CsafLoader;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
class CsafLoaderFactory {

    CsafLoader create() {
        return CsafLoader.Companion.getLazyLoader();
    }

}