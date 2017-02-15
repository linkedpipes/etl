package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.LpException;

import java.io.File;
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
     * The method must also create a "debug.json" file in
     * given directory, that contains reference to directories
     * with debug data for the user.
     *
     * @param directory
     */
    void save(File directory) throws LpException;

    /**
     * Close given data unit. After this call no other method is called.
     */
    void close() throws LpException;

    /**
     * Used when content of given dataunit is mapped from another execution
     * but is not used in this execution.
     *
     * The content should be referenced, it. the original data must not be
     * modified. The result should be similar to calling
     * {@link #initialize(java.io.File)} and {@link #save(java.io.File)},
     * but the data must stay in the source directory.
     *
     * This created dependency of the content of this data unit on the
     * content of other data unit.
     *
     * This function must not change inner state of the instance.
     *
     * @param source
     * @param destination
     */
    void referenceContent(File source, File destination) throws LpException;

}
