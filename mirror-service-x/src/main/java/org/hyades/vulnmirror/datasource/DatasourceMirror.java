package org.hyades.vulnmirror.datasource;

public interface DatasourceMirror {

    boolean supportsDatasource(final Datasource datasource);

    void doMirror();

}
