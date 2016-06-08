package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager.CantInitializeDataUnit;
import com.linkedpipes.etl.executor.event.EventFactory;
import com.linkedpipes.etl.executor.event.EventManager;
import com.linkedpipes.etl.executor.execution.ExecutionModel;
import com.linkedpipes.etl.executor.logging.LoggerFacade;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.linkedpipes.etl.executor.api.v1.component.SimpleComponent;

/**
 * Execute component with "EXECUTE" execution type.
 *
 * @author Petr Škoda
 */
class ExecuteComponent implements ComponentExecutor, Runnable {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecuteComponent.class);

    /**
     * Instance of component to execute.
     */
    private final SimpleComponent componentInstance;

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

    /**
     * Execution context for the component.
     */
    private final ComponentContext executionContext;

    ExecuteComponent(SimpleComponent componentInstance,
            ExecutionModel.Component componentExecution,
            PipelineModel.Component componentDefinition,
            DataUnitManager dataUnitManager,
            EventManager eventManager) {
        this.componentInstance = componentInstance;
        this.componentExecution = componentExecution;
        this.componentDefinition = componentDefinition;
        this.dataUnitManager = dataUnitManager;
        this.eventManager = eventManager;
        this.executionContext = new ComponentContext(eventManager);
    }

    /**
     *
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
                LOG.debug("Ignored interrup.", ex);
            }
        }
        if (unexpectedTermination) {
            eventManager.publish(EventFactory.executionFailed(
                    "Unexpected termination of component execution thread."));
        }
        LOG.info("Execution ends for: {}", this.componentDefinition.getIri());
    }

    @Override
    public void cancel() {
        executionContext.cancell();
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
        final Map<String, ManagableDataUnit> dataUnits
                = dataUnitManager.getDataUnits(componentExecution);
        for (Entry<String, ManagableDataUnit> item : dataUnits.entrySet()) {
            try {
                dataUnitManager.initialize(item.getKey(), item.getValue());
            } catch (CantInitializeDataUnit ex) {
                eventManager.publish(EventFactory.executionFailed(
                        "Can't initialize data units.", ex));
                unexpectedTermination = false;
            }
        }
        // Prepare component.
        try {
            componentInstance.initialize((Map) dataUnits, executionContext);
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
        } finally {
            MDC.remove(LoggerFacade.COMPONENT_MDC);
            unexpectedTermination = false;
        }
    }

}
