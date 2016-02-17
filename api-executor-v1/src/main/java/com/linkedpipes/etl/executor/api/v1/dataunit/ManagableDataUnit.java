package com.linkedpipes.etl.executor.api.v1.dataunit;

import java.io.File;
import java.util.Map;

/**
 * Interface for ManagableDataUnit.
 *
 * @author Škoda Petr
 */
public interface ManagableDataUnit extends DataUnit {

    public static class DataUnitException extends Exception {

        public DataUnitException(String message) {
            super(message);
        }

        public DataUnitException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * Called before data unit is used. Only one initializer method is called! Content of this data unit
     * should be loaded from given directory.
     *
     * @param directory
     * @throws com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit.DataUnitException
     */
    public void initialize(File directory) throws DataUnitException;

    /**
     * Called before data unit is used. Only one initializer method is called! Should prepare content
     * of data unit from given data units.
     *
     * @param dataUnits
     * @throws com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit.DataUnitException
     */
    public void initialize(Map<String, ManagableDataUnit> dataUnits) throws DataUnitException;

    /**
     * Save content of data unit into a directory so it can be later loaded by the {@link #initialize(java.io.File)}.
     *
     * @param directory
     * @throws com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit.DataUnitException
     */
    public void save(File directory) throws DataUnitException;

    /**
     * Called just once before close. This method should store any data or information. Output of this call
     * should store the content in format ready for user browse interaction.
     *
     * The reason for separation of this functionality aside of {@link #close()} is to provide better
     * exception handling and reporting.
     *
     * @param directory Directory where to store debug dump.
     * @throws com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit.DataUnitException
     */
    public void dumpContent(File directory) throws DataUnitException;

    /**
     * Close given data unit. After this call no other method is called.
     *
     * @throws com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit.DataUnitException
     */
    public void close() throws DataUnitException;

}
