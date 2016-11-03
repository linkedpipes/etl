package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.api.v1.component.SequentialComponent;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.event.EventFactory;
import com.linkedpipes.etl.executor.event.EventManager;
import com.linkedpipes.etl.executor.execution.ExecutionModel;
import com.linkedpipes.etl.executor.logging.LoggerFacade;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

/**
 * Execute component with "EXECUTE" execution type.
 */
class ExecuteComponent implements ComponentExecutor, Runnable {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecuteComponent.class);

    /**
     * Instance of component to execute.
     */
    private final SequentialComponent componentInstance;

    /**
     * Definition of component to execute.
     */
    private final ExecutionModel.Component componentExecution;

    private final PipelineModel.Component componentDefinition;

    private final DataUnitManager dataUnitManager;

    private final EventManager eventManager;

    /**
     * When execution finished is set to true if executor thread
     * was ended in unexpected way and pipeline should failed.
     */
    private boolean unexpectedTermination = true;

    ExecuteComponent(SequentialComponent componentInstance,
            ExecutionModel.Component componentExecution,
            PipelineModel.Component componentDefinition,
            DataUnitManager dataUnitManager,
            EventManager eventManager) {
        this.componentInstance = componentInstance;
        this.componentExecution = componentExecution;
        this.componentDefinition = componentDefinition;
        this.dataUnitManager = dataUnitManager;
        this.eventManager = eventManager;
    }

    /**
     * @return False in case of a normal thread termination.
     */
    @Override
    public boolean unexpectedTermination() {
        return unexpectedTermination;
    }

    @Override
    public void execute() {
        LOG.info("Execution starts for: {}", this.componentDefinition.getIri());
        // We execute component in an other thread, so if thread in killed
        // it won't kill the whole pipeline execution.
        final Thread thread = new Thread(this,
                componentDefinition.getDefaultLabel());
        thread.start();
        while (thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                // Ignore exception.
                LOG.debug("Ignored interrupt.", ex);
            }
        }
        if (unexpectedTermination) {
            eventManager.publish(EventFactory.executionFailed(
                    "Unexpected termination of component execution thread."));
        }
        LOG.info("Execution ends for: {}", this.componentDefinition.getIri());
    }

    @Override
    public void run() {
        // We will consider preparation of the data part of the component
        // execution.
        eventManager.publish(EventFactory.componentBegin(componentDefinition));
        // Prepare data units - we  use this first in order to initialize
        // data units that are used by this component. As even if
        // mapped we need to store the debug data. It would be nice
        // if we could skip this step and initialize only required
        // data units.
        final Map<String, ManageableDataUnit> dataUnits;
        try {
            dataUnits = dataUnitManager.onComponentStart(componentExecution);
        } catch (DataUnitManager.DataUnitException ex) {
            eventManager.publish(EventFactory.executionFailed(
                    "Can't initialize data units.", ex));
            unexpectedTermination = false;
            return;
        }
        // Prepare component.
        try {
            componentInstance.initialize((Map) dataUnits);
        } catch (Throwable t) {
            eventManager.publish(EventFactory.executionFailed(
                    "Can't initialize component.", t));
            unexpectedTermination = false;
            return;
        }
        //
        MDC.put(LoggerFacade.COMPONENT_MDC, null);
        try {
            componentInstance.execute();
            eventManager.publish(EventFactory.componentFinished(
                    componentDefinition));
        } catch (Throwable t) {
            eventManager.publish(EventFactory.componentFailed(
                    componentDefinition, t));
            eventManager.publish(EventFactory.executionFailed(
                    "Component execution failed."));
        }
        // Clean up.
        dataUnitManager.onComponentEnd(componentExecution);
        MDC.remove(LoggerFacade.COMPONENT_MDC);
        unexpectedTermination = false;
    }

}
