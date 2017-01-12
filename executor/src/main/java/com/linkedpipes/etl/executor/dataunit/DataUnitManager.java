package com.linkedpipes.etl.executor.dataunit;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.execution.Execution;
import com.linkedpipes.etl.executor.module.ModuleFacade;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * This class is responsible for handling dataunit management.
 */
public class DataUnitManager {

    private static final Logger LOG =
            LoggerFactory.getLogger(DataUnitManager.class);

    private final Pipeline pipeline;

    private final Execution execution;

    private final ModuleFacade moduleFacade;

    public DataUnitManager(Pipeline pipeline, Execution execution,
            ModuleFacade moduleFacade) {
        this.pipeline = pipeline;
        this.execution = execution;
        this.moduleFacade = moduleFacade;
    }

    /**
     * Take care about proper handling of data units for mapped component.
     *
     * @param component
     */
    public void onMappedComponent(PipelineModel.Component component)
            throws ExecutorException {
        LOG.info("onMappedComponent {}", component.getIri());
    }

    /**
     * Prepare and return inputs for given component.
     *
     * @param component
     * @return
     */
    public Map<String, ManageableDataUnit> onExecuteComponent(
            PipelineModel.Component component) throws ExecutorException {
        LOG.info("onExecuteComponent {}", component.getIri());
        return Collections.EMPTY_MAP;
    }

    /**
     * Called when component has been executed.
     *
     * @param component
     */
    public void onComponentExecuted(PipelineModel.Component component)
            throws ExecutorException {
        LOG.info("onComponentExecuted {}", component.getIri());
    }

    public void close() {
        LOG.info("close");
    }

}
