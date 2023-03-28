package org.hyades.vulnmirror.datasource;

import java.util.concurrent.Future;

public interface DatasourceMirror {

    /**
     * Determine whether a given {@link Datasource} is supported by this mirror.
     *
     * @param datasource The {@link Datasource} to check
     * @return {@code true} when supported, otherwise {@code false}
     */
    boolean supportsDatasource(final Datasource datasource);

    /**
     * <em>Asynchronously</em> execute a mirroring operating.
     *
     * @return A {@link Future} for tracking completion of the operation
     */
    Future<?> doMirror();

}