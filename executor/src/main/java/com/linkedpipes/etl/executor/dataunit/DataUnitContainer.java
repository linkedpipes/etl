package com.linkedpipes.etl.executor.dataunit;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.execution.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * Wrap data unit. Add information required for execution.
 */
class DataUnitContainer {

    public enum Status {
        NEW,
        INITIALIZED,
        SAVED,
        CLOSED
    }

    private static final Logger LOG =
            LoggerFactory.getLogger(DataUnitContainer.class);

    /**
     * Wrapped instance.
     */
    private final ManageableDataUnit instance;

    /**
     * Execution metadata.
     */
    private final Execution.DataUnit metadata;

    /**
     * Internal status of the container.
     */
    private Status status;

    public DataUnitContainer(
            ManageableDataUnit instance,
            Execution.DataUnit metadata) {
        this.instance = instance;
        this.metadata = metadata;
        this.status = Status.NEW;
    }

    /**
     * @return Wrapped instance.
     */
    public ManageableDataUnit getInstance() {
        return instance;
    }

    /**
     * Initialize data unit from given data units.
     *
     * @param instances
     */
    public void initialize(Map<String, ManageableDataUnit> instances)
            throws ExecutorException {
        if (this.status != Status.NEW) {
            throw new ExecutorException("Invalid stat of data unit ({}) : {}",
                    metadata.getDataUnitIri(), this.status);
        }
        LOG.info("bindToPipeline from sources: {}", metadata.getDataUnitIri());
        //
        try {
            instance.initialize(instances);
        } catch (LpException ex) {
            throw new ExecutorException("Can't bindToPipeline data unit: {}",
                    metadata.getDataUnitIri(), ex);
        }
        this.status = Status.INITIALIZED;
    }

    /**
     * Initialize data unit from given directory. The given directory
     * must contains previously saved data by the data unit.
     *
     * @param directory
     */
    public void initialize(File directory)
            throws ExecutorException {
        if (this.status != Status.NEW) {
            throw new ExecutorException("Invalid stat of data unit ({}) : {}",
                    metadata.getDataUnitIri(), this.status);
        }
        LOG.info("bindToPipeline from file: {} {}",
                metadata.getDataUnitIri(), directory);
        //
        try {
            instance.initialize(directory);
        } catch (LpException ex) {
            throw new ExecutorException("Can't bindToPipeline data unit: {}",
                    metadata.getDataUnitIri(), ex);
        }
        this.status = Status.INITIALIZED;
    }

    /**
     * Save content of the data unit instance.
     */
    public void save() throws ExecutorException {
        if (status == Status.NEW) {
            // This may happen for unused data units (if pipeline fail).
            return;
        }
        if (this.status == Status.CLOSED) {
            throw new ExecutorException("Can't save closed data unit: {}",
                    metadata.getDataUnitIri());
        }
        final File saveDirectory = metadata.getSaveDirectory();
        if (saveDirectory == null){
            return;
        }
        saveDirectory.mkdirs();
        try {
            instance.save(saveDirectory);
        } catch (LpException ex) {
            throw new ExecutorException("Can't save data unit: {}",
                    metadata.getDataUnitIri(), ex);
        }
        this.status = Status.SAVED;
    }

    /**
     * Close wrapped instance.
     */
    public void close() throws ExecutorException {
        if (status == Status.NEW) {
            LOG.warn("Data unit not initialized: {}",
                    metadata.getDataUnitIri());
            return;
        }
        if (status == Status.CLOSED) {
            LOG.warn("Data unit already closed: {}",
                    metadata.getDataUnitIri());
            return;
        }
        //
        try {
            instance.close();
        } catch (LpException ex) {
            throw new ExecutorException("Can't close data unit: {}",
                    metadata.getDataUnitIri(), ex);
        }
        this.status = Status.CLOSED;
    }

}
