package com.linkedpipes.etl.dataunit.system.files;

/**
 *
 * @author Škoda Petr
 */
public final class SystemFilesDataUnitFactory {

    public static ManagableFilesDataUnit create(FilesDataUnitConfiguration config) {
        return new FilesDataUnitImpl(config);
    }

}
