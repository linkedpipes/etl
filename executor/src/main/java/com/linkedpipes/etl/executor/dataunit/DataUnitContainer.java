package com.linkedpipes.etl.executor.dataunit;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.execution.model.DataUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * Wrap data unit. Add information required for ExecutionModel.
 */
class DataUnitContainer {

    public enum Status {
        NEW,
        INITIALIZED,
        // Respective component was already executed.
        AFTER_EXECUTION,
        SAVED,
        CLOSED,
        MAPPED
    }

    private static final Logger LOG =
            LoggerFactory.getLogger(DataUnitContainer.class);

    /**
     * Wrapped instance.
     */
    private final ManageableDataUnit instance;

    /**
     * ExecutionObserver metadata.
     */
    private final DataUnit metadata;

    /**
     * Internal status of the container.
     */
    private Status status;

    public DataUnitContainer(
            ManageableDataUnit instance,
            DataUnit metadata) {
        this.instance = instance;
        this.metadata = metadata;
        this.status = Status.NEW;
    }

    /**
     * @return Wrapped instance.
     */
    public ManageableDataUnit getInstance() {
        return this.instance;
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
                    this.metadata.getIri(), this.status);
        }
        LOG.debug("load from sources: {}", metadata.getIri());
        try {
            this.instance.initialize(instances);
        } catch (LpException ex) {
            throw new ExecutorException("Can't bindToPipeline data unit: {}",
                    this.metadata.getIri(), ex);
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
                    this.metadata.getIri(), this.status);
        }
        LOG.debug("load from file: {}", this.metadata.getIri());
        try {
            this.instance.initialize(directory);
        } catch (LpException ex) {
            throw new ExecutorException("Can't bindToPipeline data unit: {}",
                    this.metadata.getIri(), ex);
        }
        this.status = Status.INITIALIZED;
    }

    public void onComponentDidExecute() throws ExecutorException {
        if (this.status != Status.INITIALIZED) {
            throw new ExecutorException("Invalid status change from: {} to {}",
                    this.status, Status.AFTER_EXECUTION);
        }
        this.status = Status.AFTER_EXECUTION;
    }

    /**
     * Save content of the data unit instance.
     */
    public void save() throws ExecutorException {
        if (this.status == Status.NEW) {
            // This can happen for unused data units (if pipeline fail).
            return;
        }
        if (this.status == Status.CLOSED) {
            throw new ExecutorException("Can't save closed data unit: {}",
                    this.metadata.getIri());
        }
        File saveDirectory = this.metadata.getSaveDirectory();
        if (saveDirectory == null) {
            return;
        }
        saveDirectory.mkdirs();
        try {
            this.instance.save(saveDirectory);
        } catch (LpException ex) {
            throw new ExecutorException("Can't save data unit: {}",
                    this.metadata.getIri(), ex);
        }
        this.status = Status.SAVED;
    }

    /**
     * Close wrapped instance.
     */
    public void close() {
        if (this.status == Status.NEW) {
            // Can happen if pipeline fail.
            return;
        }
        if (this.status == Status.CLOSED) {
            LOG.warn("Data unit already closed: {}",
                    this.metadata.getIri());
            return;
        }
        //
        try {
            this.instance.close();
        } catch (LpException ex) {
            LOG.info("Can't close data unit: {}", this.metadata.getIri(), ex);
        }
        this.status = Status.CLOSED;
    }

    /**
     * Do not load the content, just load reference. This make it possible
     * to save the debug data.
     */
    public void mapByReference(File source)
            throws ExecutorException {
        File saveDirectory = this.metadata.getSaveDirectory();
        if (saveDirectory == null) {
            return;
        }
        try {
            LOG.debug("map by reference: {} from {}",
                    this.metadata.getIri(), source);
            this.instance.referenceContent(source, saveDirectory);
        } catch (LpException ex) {
            throw new ExecutorException("Can't reference content.", ex);
        }
        this.status = Status.MAPPED;
    }

    public boolean openWithData() {
        return this.status == Status.AFTER_EXECUTION ||
                this.status == Status.SAVED ||
                this.status == Status.MAPPED;
    }

    // TODO REMOVE
    public Status getStatus() {
        return this.status;
    }
}
