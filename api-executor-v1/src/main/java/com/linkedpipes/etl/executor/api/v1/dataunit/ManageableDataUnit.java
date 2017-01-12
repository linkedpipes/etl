package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.LpException;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Extension of DataUnit provides ways and means for management.
 */
public interface ManageableDataUnit extends DataUnit {

    /**
     * Called before data unit is used. Only one initializer method is called!
     * Content of this data unit should be loaded from given directory.
     *
     * DataUnit initialized by this method is read-only.
     *
     * @param directory
     */
    void initialize(File directory) throws LpException;

    /**
     * Called before data unit is used. Only one initializer method is called!
     * Should prepare content of data unit from given data units.
     *
     * @param dataUnits
     */
    void initialize(Map<String, ManageableDataUnit> dataUnits)
            throws LpException;

    /**
     * Save content of data unit into a directory so it can be later loaded
     * by the {@link #initialize(java.io.File)}.
     *
     * This method can be called multiple times.
     *
     * The method must return list of directories that contains data that
     * can be shown to user as a content of this data unit. The returned
     * paths must be in scope of the execution.
     *
     * @param directory
     * @return Optionally additional directories that contains data.
     */
    List<File> save(File directory) throws LpException;

    /**
     * Close given data unit. After this call no other method is called.
     */
    void close() throws LpException;

}
