package com.linkedpipes.etl.executor.dataunit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import com.linkedpipes.etl.executor.event.EventFactory;
import com.linkedpipes.etl.executor.event.EventManager;
import com.linkedpipes.etl.executor.execution.ExecutionModel;
import com.linkedpipes.etl.executor.module.ModuleFacade;
import com.linkedpipes.etl.executor.module.ModuleFacade.ModuleException;
import com.linkedpipes.etl.executor.pipeline.PipelineDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manage life cycle of all data units.
 */
public class DataUnitManager {

    public static class DataUnitException extends ExecutorException {

        public DataUnitException(String messages, Object... args) {
            super(messages, args);
        }

    }

    /**
     * Name of subdirectory for data in the data unit directory.
     */
    private static final String DATA_DIRECTORY = "data";

    private static final Logger LOG
            = LoggerFactory.getLogger(DataUnitManager.class);

    private final PipelineDefinition pipelineSparql;

    private final ExecutionModel execution;

    private final EventManager events;

    private final Map<String, DataUnitContainer> dataUnits = new HashMap<>();

    /**
     * Store direct access to data unit instances.
     */
    private final Map<String, ManageableDataUnit> instances = new HashMap<>();

    public DataUnitManager(PipelineDefinition pipelineSparql,
            ExecutionModel execution, EventManager events) {
        this.pipelineSparql = pipelineSparql;
        this.execution = execution;
        this.events = events;
    }

    public void onExecutionStart(ModuleFacade moduleFacade)
            throws DataUnitException {
        // Eager mode. Create instances of data units.
        // Instances should be an empty classes till the initialization
        // so it should be ok to do this.
        for (ExecutionModel.Component comp : this.execution.getComponents()) {
            for (ExecutionModel.DataUnit dataUnit : comp.getDataUnits()) {
                createDataUnit(moduleFacade, dataUnit);
            }
        }
    }

    public void onExecutionEnd() {
        // Close all data units.
        boolean failure = false;
        for (DataUnitContainer container : dataUnits.values()) {
            try {
                close(container);
            } catch (DataUnitException ex) {
                failure = true;
                LOG.error("Can't close data unit.", ex);
            }
        }
        if (failure) {
            events.publish(EventFactory.executionFailed(
                    "Failed to save and close data units."));
        }
    }

    public Map<String, ManageableDataUnit> onComponentStart(
            ExecutionModel.Component component) throws DataUnitException {
        // Get all data units used by the given component, initialize
        // them and return them.
        final Map<String, ManageableDataUnit> usedDataUnits = new HashMap<>();
        for (ExecutionModel.DataUnit dataUnit : component.getDataUnits()) {
            if (!dataUnit.isUsedForExecution()) {
                // Skip those that are not used for execution.
                continue;
            }
            final DataUnitContainer container =
                    dataUnits.get(dataUnit.getIri());
            initialize(container);
            usedDataUnits.put(dataUnit.getIri(), container.getInstance());
            // If the data unit is input, we want to save the
            // data here. So the used can see input of a running
            // component.
            if (dataUnit.isInput()) {
                save(container);
            }
        }
        return usedDataUnits;
    }

    public void onComponentEnd(ExecutionModel.Component component) {
        // Save used components.
        for (ExecutionModel.DataUnit dataUnit : component.getDataUnits()) {
            if (!dataUnit.isUsedForExecution()) {
                // Skip those that are not used for execution.
                continue;
            }
            //
            final DataUnitContainer container =
                    dataUnits.get(dataUnit.getIri());
            save(container);
        }
    }

    /**
     * If given data unit is used then create and add a container with instance
     * for it.
     *
     * @param moduleFacade
     * @param dataUnit
     */
    private void createDataUnit(ModuleFacade moduleFacade,
            ExecutionModel.DataUnit dataUnit) throws DataUnitException {
        if (!dataUnit.isUsedForExecution()) {
            // Skip data units that are not used in the execution.
            return;
        }
        // Create an instance and add to list of data units.
        try {
            final ManageableDataUnit instance = moduleFacade.getDataUnit(
                    pipelineSparql, dataUnit.getIri());
            instances.put(dataUnit.getIri(), instance);
            dataUnits.put(dataUnit.getIri(),
                    new DataUnitContainer(instance, dataUnit));
        } catch (ModuleException ex) {
            throw new DataUnitException("Can't get data unit instance.", ex);
        }
    }

    private void initialize(DataUnitContainer container)
            throws DataUnitException {
        // Check for status.
        switch (container.getStatus()) {
            case OPEN:
                // Already initialized.
                return;
            case CLOSED:
                throw new DataUnitException("Can't reopen data unit: {}",
                        container.getMetadata().getIri());
        }
        //
        final ExecutionModel.DataUnit dataUnit = container.getMetadata();
        if (dataUnit.isMapped()) {
            LOG.info("Loading existing data into data unit {} : {} ... ",
                    dataUnit.getBinding(), dataUnit.getIri());
            final File dataPath = new File(dataUnit.getLoadPath(),
                    DATA_DIRECTORY);
            try {
                container.getInstance().initialize(dataPath);
            } catch (LpException ex) {
                throw new DataUnitException("Can't load data unit: {}",
                        dataUnit.getIri(), ex);
            }
            LOG.info("Loading existing data into data unit {} : {} ... done",
                    dataUnit.getBinding(), dataUnit.getIri());
        } else {
            // We trust ManageableDataUnit and provide it with all
            // data units.
            LOG.info("Initializing data unit: {} : {} ...",
                    dataUnit.getBinding(), dataUnit.getIri());
            try {
                container.getInstance().initialize(instances);
            } catch (LpException ex) {
                throw new DataUnitException("Can't initialize unit: {}",
                        dataUnit.getIri(), ex);
            }
            LOG.info("Initializing data unit: {} : {} ... done",
                    dataUnit.getBinding(), dataUnit.getIri());
        }
        // Update container status.
        container.onInitialized();
    }

    private void save(DataUnitContainer container) {
        switch (container.getStatus()) {
            case SAVED:
                // Already saved.
                return;
            case CLOSED:
                LOG.warn("Can't save closed data unit: {}",
                        container.getMetadata().getIri());
                return;
        }
        //
        final File dataFile = container.getMetadata().getDataPath();
        final ExecutionModel.DataUnit dataUnit = container.getMetadata();
        LOG.info("Saving data unit: {} : {} ... ",
                dataUnit.getBinding(), dataUnit.getIri());
        if (dataFile != null) {
            List<File> debugPaths = Collections.EMPTY_LIST;
            try {
                final File dataDirectory = new File(dataFile,
                        DATA_DIRECTORY);
                dataDirectory.mkdirs();
                debugPaths = container.getInstance().save(dataDirectory);
            } catch (Throwable ex) {
                LOG.error("Can't save data unit : {}",
                        container.getMetadata().getIri(), ex);
            }
            // Save debug paths relative to file we store data it.
            final ObjectMapper mapper = new ObjectMapper();
            final File debugFile = new File(dataFile, "/debug.json");
            final List<String> relativeDebugPaths
                    = new ArrayList<>(debugPaths.size());
            for (File file : debugPaths) {
                relativeDebugPaths.add(dataFile.toPath().relativize(
                        file.toPath()).toString());
            }
            try {
                mapper.writeValue(debugFile, relativeDebugPaths);
            } catch (IOException ex) {
                LOG.error("Can't save data debug paths.", ex);
            }
        }
        LOG.info("Saving data unit: {} : {} ... done",
                dataUnit.getBinding(), dataUnit.getIri());
        // Update container status.
        container.onSave();
    }

    private void close(DataUnitContainer container) throws DataUnitException {
        switch (container.getStatus()) {
            case CLOSED:
                // Already closed.
                return;
        }
        final ExecutionModel.DataUnit dataUnit = container.getMetadata();
        LOG.info("Closing data unit: {} : {} ... ",
                dataUnit.getBinding(), dataUnit.getIri());
        try {
            container.getInstance().close();
        } catch (LpException ex) {
            throw new DataUnitException(("Can't close data unit : {}"),
                    container.getMetadata().getIri(), ex);
        }
        LOG.info("Closing data unit: {} : {} ... done",
                dataUnit.getBinding(), dataUnit.getIri());
        container.onClose();
    }

}
