package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Interface for ManagableDataUnit.
 *
 * @author Škoda Petr
 */
public interface ManagableDataUnit extends DataUnit {

    /**
     * Called before data unit is used. Only one initializer method is called!
     * Content of this data unit should be loaded from given directory.
     *
     * @param directory
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     */
    public void initialize(File directory) throws LpException;

    /**
     * Called before data unit is used. Only one initializer method is called!
     * Should prepare content of data unit from given data units.
     *
     * @param dataUnits
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     */
    public void initialize(Map<String, ManagableDataUnit> dataUnits)
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
     * If given directory contains the data that should be visible
     * as a content of data unit than it should also be returned.
     *
     * @param directory
     * @return Optionally additional directories that contains data.
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     */
    public List<File> save(File directory) throws LpException;

    /**
     * Close given data unit. After this call no other method is called.
     *
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     */
    public void close() throws LpException;

}
