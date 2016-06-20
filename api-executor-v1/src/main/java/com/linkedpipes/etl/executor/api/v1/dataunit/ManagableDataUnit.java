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
     * @param directory
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     */
    public void save(File directory) throws LpException;

    /**
     * Called just once before close. This method should store any data or
     * information. Output of this call should store the content in format
     * ready for user browse interaction.
     *
     * The reason for separation of this functionality aside of
     * {@link #close()} is to provide better exception handling and reporting.
     *
     * It's consider save to just link working directories of data units as
     * they are not deleted if debug is used.
     *
     * The optionally returned additional directories must be working
     * directories assigned by the core. Utilisation of any other directory
     * is forbidden.
     *
     * @param directory Directory where to store debug dump.
     * @return Optionally additional directories that contains debug data.
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     */
    public List<File> dumpContent(File directory) throws LpException;

    /**
     * Close given data unit. After this call no other method is called.
     *
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     */
    public void close() throws LpException;

}
