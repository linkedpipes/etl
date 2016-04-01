package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager.CantInitializeDataUnit;
import com.linkedpipes.etl.executor.event.EventFactory;
import com.linkedpipes.etl.executor.event.EventManager;
import com.linkedpipes.etl.executor.execution.ExecutionModel;
import com.linkedpipes.etl.executor.execution.ExecutionModel.Component;
import java.util.Map;
import java.util.Map.Entry;
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

    private boolean cancel = false;

    MapComponent(EventManager events, DataUnitManager dataunits,
            Component componentExecution) {
        this.events = events;
        this.dataunits = dataunits;
        this.componentExecution = componentExecution;
    }

    @Override
    public void execute() {
        LOG.info("Mappping starts for: {}", this.componentExecution.getIri());
        // Get data units belonging to this comonent that are also used
        // by other components.
        final Map<String, ManagableDataUnit> dataUnits =
                dataunits.getDataUnits(componentExecution);
        for (Entry<String, ManagableDataUnit> item : dataUnits.entrySet()) {
            try {
                dataunits.initialize(item.getKey(), item.getValue());
            } catch (CantInitializeDataUnit ex) {
                events.publish(EventFactory.executionFailed(
                        "Can't initialize data units.", ex));
            }
            if (cancel) {
                break;
            }
        }
        LOG.info("Mapping ends for: {}", this.componentExecution.getIri());
    }

    @Override
    public void cancel() {
        cancel = true;
    }

    @Override
    public boolean unexpectedTermination() {
        return false;
    }

}
