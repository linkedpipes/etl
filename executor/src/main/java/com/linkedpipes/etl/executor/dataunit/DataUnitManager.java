package com.linkedpipes.etl.executor.dataunit;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.execution.model.DataUnit;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import com.linkedpipes.etl.executor.pipeline.model.PipelineModel;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for handling dataunit management.
 */
public class DataUnitManager {

    private final Map<DataUnit, DataUnitContainer> dataUnits
            = new HashMap<>();

    /**
     * Map of instances. Given to data units for initialization.
     */
    private final Map<String, ManageableDataUnit> instances = new HashMap<>();

    private final PipelineQuery pipelineQuery;

    private final DataUnitCloser portCloser;

    public DataUnitManager(PipelineModel pipeline) {
        this.pipelineQuery = new PipelineQuery(pipeline);
        this.portCloser = new DataUnitCloser(this.dataUnits, this.pipelineQuery);
    }

    public void initialize(
            DataUnitInstanceSource dataUnitInstanceSource,
            Collection<DataUnit> dataUnits)
            throws ExecutorException {
        for (DataUnit dataUnit : dataUnits) {
            createDataUnitContainer(dataUnitInstanceSource, dataUnit);
        }
    }

    private void createDataUnitContainer(
            DataUnitInstanceSource dataUnitInstanceSource,
            DataUnit dataUnit) throws ExecutorException {
        String iri = dataUnit.getIri();
        ManageableDataUnit instance;
        try {
            instance = dataUnitInstanceSource.getDataUnit(iri);
        } catch (ExecutorException ex) {
            throw new ExecutorException(
                    "Can't instantiate data unit: {}", iri, ex);
        }
        this.dataUnits.put(dataUnit, new DataUnitContainer(instance, dataUnit));
        this.instances.put(iri, instance);
    }

    public void close() {
        for (DataUnitContainer container : this.dataUnits.values()) {
            if (container.openWithData()) {
                container.close();
            }
        }
    }

    public Map<String, com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit>
    onComponentWillExecute(ExecutionComponent component)
            throws ExecutorException {
        Map<String, com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit>
                result = new HashMap<>();
        //
        for (DataUnit dataUnit : component.getDataUnits()) {
            DataUnitContainer container = getContainer(dataUnit);
            //
            File dataDirectory = dataUnit.getLoadDirectory();
            if (dataDirectory == null) {
                container.initialize(this.instances);
            } else {
                container.initialize(dataDirectory);
            }
            result.put(dataUnit.getIri(), container.getInstance());
        }
        this.portCloser.addComponentDataUnits(component);
        return result;
    }

    private DataUnitContainer getContainer(DataUnit dataUnit)
            throws ExecutorException {
        DataUnitContainer container = this.dataUnits.get(dataUnit);
        if (container == null) {
            throw new ExecutorException(
                    "Missing data unit: {}", dataUnit.getIri());
        }
        return container;
    }

    public void onComponentDidExecute(ExecutionComponent component)
            throws ExecutorException {
        for (DataUnit dataUnit : component.getDataUnits()) {
            DataUnitContainer container = getContainer(dataUnit);
            container.onComponentDidExecute();
            if (dataUnit.getPort().isSaveDebugData()) {
                container.save();
            }
        }
        this.portCloser.onComponentExecuted(component);
        this.portCloser.closeUnusedDataUnits();
    }

    public void onComponentMapByReference(ExecutionComponent component)
            throws ExecutorException {
        for (DataUnit dataUnit : component.getDataUnits()) {
            DataUnitContainer container = this.dataUnits.get(dataUnit);
            if (container == null) {
                throw new ExecutorException("Missing data unit: {} for {}",
                        dataUnit.getIri(), component.getIri());
            }
            File sourceFile = dataUnit.getLoadDirectory();
            if (pipelineQuery.isDataUnitUsed(component, dataUnit)) {
                container.initialize(sourceFile);
                container.onComponentDidExecute();
                if (dataUnit.getPort().isSaveDebugData()) {
                    container.save();
                }
            } else {
                container.mapByReference(sourceFile);
            }
        }
        // After this there are no new data-units that can be closed.
        this.portCloser.addComponentDataUnits(component);
    }

}
