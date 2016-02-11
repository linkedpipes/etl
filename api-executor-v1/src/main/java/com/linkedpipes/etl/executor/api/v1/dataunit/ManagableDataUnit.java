package com.linkedpipes.etl.executor.api.v1.dataunit;

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
     * Prepare to use.
     *
     * @param dataUnits
     * @throws com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit.DataUnitException
     */
    public void initialize(Map<String, ManagableDataUnit> dataUnits) throws DataUnitException;

    /**
     * Called just once before close. This method should store any data or information.
     *
     * The reason for separation of this functionality aside of {@link #close()} is to provide better
     * exception handling and reporting.
     *
     * @throws com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit.DataUnitException
     */
    public void dumpContent() throws DataUnitException;

    /**
     * Close given data unit. After this call no other method is called.
     *
     * @throws com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit.DataUnitException
     */
    public void close() throws DataUnitException;

}
