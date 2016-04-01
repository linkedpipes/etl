package com.linkedpipes.etl.executor.dataunit;

import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit.DataUnitException;
import com.linkedpipes.etl.executor.event.EventFactory;
import com.linkedpipes.etl.executor.event.EventManager;
import com.linkedpipes.etl.executor.execution.ExecutionModel;
import com.linkedpipes.etl.executor.module.ModuleFacade;
import com.linkedpipes.etl.executor.module.ModuleFacade.ModuleException;
import com.linkedpipes.etl.executor.pipeline.PipelineDefinition;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage life cycle of all data units.
 *
 * @author Petr Škoda
 */
public class DataUnitManager {

    public static class CantInitializeDataUnit extends Exception {

        public CantInitializeDataUnit(String message) {
            super(message);
        }

        public CantInitializeDataUnit(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private static final Logger LOG
            = LoggerFactory.getLogger(DataUnitManager.class);

    /**
     * Store all used data units.
     */
    private final Map<String, ManagableDataUnit> dataUnits = new HashMap<>();

    private final PipelineDefinition pipeline;

    private final ExecutionModel execution;

    private final ModuleFacade moduleFacade;

    public DataUnitManager(PipelineDefinition pipeline, ExecutionModel execution,
            ModuleFacade moduleFacade) {
        this.pipeline = pipeline;
        this.execution = execution;
        this.moduleFacade = moduleFacade;
    }

    public void prepareDataUnits() throws CantInitializeDataUnit {
        for (ExecutionModel.Component comp : this.execution.getComponents()) {
            for (ExecutionModel.DataUnit dataUnit : comp.getDataUnits()) {
                if (!dataUnit.isUsedForExecution()) {
                    continue;
                }
                final ManagableDataUnit instance;
                try {
                    instance = moduleFacade.getDataUnit(pipeline,
                            dataUnit.getIri());
                } catch (ModuleException ex) {
                    throw new CantInitializeDataUnit(
                            "Can't get data unit instace.", ex);
                }
                dataUnits.put(dataUnit.getIri(), instance);
            }
        }
    }

    public ManagableDataUnit getDateUnit(String iri) {
        return dataUnits.get(iri);
    }

    /**
     * Does not change the state of returned data units. Return instances
     * of used data units by given component.
     *
     * @param component
     * @return Data unit instances.
     */
    public Map<String, ManagableDataUnit> getDataUnits(
            ExecutionModel.Component component) {
        final Map<String, ManagableDataUnit> result = new HashMap<>();
        for (ExecutionModel.DataUnit dataUnit : component.getDataUnits()) {
            if (dataUnit.isUsedForExecution()) {
                result.put(dataUnit.getIri(), dataUnits.get(dataUnit.getIri()));
            }
        }
        return result;
    }

    public void initialize(String iri, ManagableDataUnit instance)
            throws CantInitializeDataUnit {
        final ExecutionModel.DataUnit dataUnit = execution.getDataUnit(iri);
        if (dataUnit == null) {
            throw new CantInitializeDataUnit("Unknown IRI.");
        }
        if (dataUnit.isMapped()) {
            LOG.info("Loading data unit: {} : {}",
                    dataUnit.getBinding(), dataUnit.getIri());
            try {
                instance.initialize(dataUnit.getLoadPath());
            } catch (DataUnitException ex) {
                throw new CantInitializeDataUnit("Can't load: "
                        + dataUnit.getIri(), ex);
            }
        } else {
            // We trust ManagableDataUnit and provide it with all
            // data units.
            LOG.info("Initializing data unit: {} : {}",
                    dataUnit.getBinding(), dataUnit.getIri());
            try {
                instance.initialize(dataUnits);
            } catch (DataUnitException ex) {
                throw new CantInitializeDataUnit("Can't initialize: "
                        + dataUnit.getIri(), ex);
            }
        }
    }

    /**
     * Close and save all opened data units.
     * @param events
     */
    public void close(EventManager events) {
        boolean result = true;
        for (ExecutionModel.Component component : execution.getComponents()) {
            for (ExecutionModel.DataUnit dataUnit : component.getDataUnits()) {
                result &= close(dataUnit);
            }
        }
        if (!result) {
            events.publish(EventFactory.executionFailed(
                    "Failed to save and close data units."));
        }
        dataUnits.clear();
    }

    /**
     * Close data unit. Save the data and debug data, store
     * debug directories back into the data unit model.
     *
     * @param execution
     * @param iri
     * @return False in case of an error.
     */
    private boolean close(ExecutionModel.DataUnit dataUnit) {
        final ManagableDataUnit instance = dataUnits.get(dataUnit.getIri());
        if (instance == null) {
            return true;
        }
        boolean result = true;
        try {
            instance.save(dataUnit.getSavePath());
        } catch (Throwable ex) {
            result = false;
            LOG.error("Can't save data unit.", ex);
        }
        try {
            final List<File> debugPaths = new ArrayList<>(2);
            debugPaths.add(dataUnit.getDebugPath());
            debugPaths.addAll(instance.dumpContent(dataUnit.getDebugPath()));
            dataUnit.setDebugPaths(debugPaths);
        } catch (Throwable ex) {
            result = false;
            LOG.error("Can't save data unit.", ex);
        }
        try {
            instance.close();
        } catch (DataUnitException ex) {
            result = false;
            LOG.error("Can't close data unit.", ex);
        }
        return result;
    }

}
