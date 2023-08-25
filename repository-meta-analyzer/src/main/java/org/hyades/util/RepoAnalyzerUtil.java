package org.hyades.util;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import io.quarkus.narayana.jta.QuarkusTransaction;
import org.hyades.persistence.model.Repository;
import org.hyades.persistence.model.RepositoryType;
import org.hyades.persistence.repository.RepoEntityRepository;

import java.util.List;

public final class RepoAnalyzerUtil {

    public static PackageURL parsePurl(final String purl) {
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            throw new IllegalStateException("""
                    The provided PURL is invalid, even though it should have been
                    validated in a previous processing step
                    """, e);
        }
    }

    public static List<Repository> getApplicableRepositories(final RepoEntityRepository repoEntityRepository,
                                                       final RepositoryType repositoryType) {
        // Hibernate requires an active transaction to perform any sort of interaction
        // with the database. Because processors can't be CDI beans, we cannot use
        // @Transactional and the like. Falling back to manual transaction management.
        //
        // NOTE: The result of this query can potentially be cached for at least a few minutes.
        // Executing it for every single component feels excessive.
        // Quarkus has Hibernate L2 cache enabled by default, we just need to opt in to using
        // it for this query: https://quarkus.io/guides/hibernate-orm#caching-of-queries
        // Should be tested whether throughput can be improved this way.
        //changed this to joinexisting because with new transaction, it is not fetching values that were inserted from
        // existing previous transaction and returning empty result
        return QuarkusTransaction.joiningExisting()
                .call(() -> repoEntityRepository.findEnabledRepositoriesByType(repositoryType));
    }
}
