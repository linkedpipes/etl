package com.linkedpipes.etl.executor.dataunit;

import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import com.linkedpipes.etl.executor.execution.ExecutionModel;
import com.linkedpipes.etl.executor.rdf.EntityLoader;
import org.openrdf.model.Value;

/**
 * Data object used to carry information about data unit and its status.
 *
 * @author Petr Škoda
 */
final class DataUnitContainer implements EntityLoader.Loadable {

    static enum Status {
        NEW,
        /**
         * Initialized.
         */
        OPEN,
        /**
         * Open but with saved content.
         */
        SAVED,
        /**
         * Close, should not be further used.
         */
        CLOSED
    }

    private final ManagableDataUnit instance;

    private final ExecutionModel.DataUnit metadata;

    private Status status = Status.NEW;

    DataUnitContainer(ManagableDataUnit instance,
            ExecutionModel.DataUnit metadata) {
        this.instance = instance;
        this.metadata = metadata;
    }

    public void onInitialized() {
        status = Status.OPEN;
    }

    public void onSave() {
        status = Status.SAVED;
    }

    public void onClose() {
        status = Status.CLOSED;
    }

    public ManagableDataUnit getInstance() {
        return instance;
    }

    public ExecutionModel.DataUnit getMetadata() {
        return metadata;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public EntityLoader.Loadable load(String predicate, Value object)
            throws LpException {
        return null;
    }

}
