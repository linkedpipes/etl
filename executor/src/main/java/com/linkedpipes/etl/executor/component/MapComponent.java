package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.event.EventManager;
import com.linkedpipes.etl.executor.execution.ExecutionModel;
import com.linkedpipes.etl.executor.execution.ExecutionModel.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute component with "MAP" execution type.
 *
 * @author Petr Škoda
 */
class MapComponent implements ComponentExecutor {

    private static final Logger LOG
            = LoggerFactory.getLogger(MapComponent.class);

    private final EventManager events;

    private final DataUnitManager dataunits;

    private final ExecutionModel.Component componentExecution;

    MapComponent(EventManager events, DataUnitManager dataunits,
            Component componentExecution) {
        this.events = events;
        this.dataunits = dataunits;
        this.componentExecution = componentExecution;
    }

    @Override
    public void execute() {
        LOG.info("Mapping starts for: {}", this.componentExecution.getIri());
        // Get data units belonging to this component that are also used
        // by other components.
        try {
            dataunits.onComponentStart(componentExecution);
        } catch (DataUnitManager.DataUnitException ex) {
        }
        dataunits.onComponentEnd(componentExecution);
        LOG.info("Mapping ends for: {}", this.componentExecution.getIri());
    }

    @Override
    public boolean unexpectedTermination() {
        return false;
    }

}
