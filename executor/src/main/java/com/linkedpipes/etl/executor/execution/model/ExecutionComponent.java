package com.linkedpipes.etl.executor.execution.model;

import com.linkedpipes.etl.executor.pipeline.model.ExecutionType;
import com.linkedpipes.etl.executor.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.executor.pipeline.model.Port;

import java.util.ArrayList;
import java.util.List;

public class ExecutionComponent {

    private final PipelineComponent pipelineComponent;

    private final List<DataUnit> dataUnits = new ArrayList<>(4);

    public ExecutionComponent(PipelineComponent component) {
        this.pipelineComponent = component;
    }

    public String getIri() {
        return this.pipelineComponent.getIri();
    }

    public ExecutionType getExecutionType() {
        return this.pipelineComponent.getExecutionType();
    }

    public List<Port> getPorts() {
        return this.pipelineComponent.getPorts();
    }

    public List<DataUnit> getDataUnits() {
        return this.dataUnits;
    }

}
