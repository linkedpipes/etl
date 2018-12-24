package com.linkedpipes.etl.executor.dataunit;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.execution.model.DataUnit;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


class DataUnitCloser {

    private static final Logger LOG =
            LoggerFactory.getLogger(DataUnitCloser.class);

    private final Map<DataUnit, DataUnitContainer> dataUnits;

    private final PipelineQuery pipelineQuery;

    private final Set<String> executedComponents = new HashSet<>();

    /**
     * We have owners only for initialized data units.
     */
    private final Map<DataUnit, ExecutionComponent> owners = new HashMap<>();

    public DataUnitCloser(
            Map<DataUnit, DataUnitContainer> dataUnits,
            PipelineQuery pipelineQuery) {
        this.dataUnits = dataUnits;
        this.pipelineQuery = pipelineQuery;
    }

    public void addComponentDataUnits(ExecutionComponent component) {
        for (DataUnit dataUnit : component.getDataUnits()) {
            this.owners.put(dataUnit, component);
        }
    }

    public void onComponentExecuted(ExecutionComponent component) {
        LOG.info("onComponentExecuted", getComponentId(component.getIri()));
        this.executedComponents.add(component.getIri());
    }

    private String getComponentId(String iri) {
        int startIndex = iri.indexOf("/component") + "component".length();
        return iri.substring(startIndex);
    }

    public void closeUnusedDataUnits() throws ExecutorException {
        // Collect ports to close.
        LOG.info("Close data units:");
        List<DataUnit> toClose = new ArrayList<>();
        for (Map.Entry<DataUnit, DataUnitContainer> entry :
                this.dataUnits.entrySet()) {
            DataUnit dataUnit = entry.getKey();
            if (!entry.getValue().openWithData()) {
                continue;
            }
            if (this.isNoLongerUsed(dataUnit)) {
                toClose.add(dataUnit);
                LOG.info("  CLOSE {} {} : {}",
                        entry.getValue().getStatus(),
                        getComponentId(dataUnit.getIri()),
                        dataUnit.getPort().getBinding());
            } else {
                LOG.info("        {} {} : {}",
                        entry.getValue().getStatus(),
                        getComponentId(dataUnit.getIri()),
                        dataUnit.getPort().getBinding());
            }
        }
        //
        for (DataUnit dataUnit : toClose) {
            DataUnitContainer container = this.dataUnits.get(dataUnit);
            container.close();
        }
    }

    private boolean isNoLongerUsed(DataUnit dataUnit) throws ExecutorException {
        ExecutionComponent owner = this.owners.get(dataUnit);
        // 'owner' would be null for only non-initialized components,
        // so that should never happen.
        return this.pipelineQuery.isNoLongerUsed(
                this.executedComponents, owner, dataUnit);
    }

}
