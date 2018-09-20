package com.linkedpipes.etl.executor.execution.model;

import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.executor.pipeline.model.*;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represent a pipeline execution model.
 */
public class ExecutionModel {

    /**
     * Use original component IRI from pipeline.
     */
    private final Map<String, ExecutionComponent> components = new HashMap<>();

    /**
     * Use original data unit IRI from pipeline.
     */
    private final Map<String, DataUnit> dataUnits = new HashMap<>();

    private final String iri;

    private ResourceManager resourceManager;

    public ExecutionModel(ResourceManager resourceManager, String iri) {
        this.resourceManager = resourceManager;
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }

    public ExecutionComponent getComponent(PipelineComponent component) {
        return this.getComponent(component.getIri());
    }

    public ExecutionComponent getComponent(String iri) {
        return this.components.get(iri);
    }

    public List<DataUnit> getDataUnitsForInitialization() {
        List<DataUnit> usedDataUnits = new LinkedList<>();
        for (ExecutionComponent component : components.values()) {
            ExecutionType executionType = component.getExecutionType();
            if (executionType == ExecutionType.SKIP) {
                continue;
            }
            usedDataUnits.addAll(component.getDataUnits());
        }
        return usedDataUnits;
    }

    public void initialize(PipelineModel pipeline) {
        for (PipelineComponent pplComponent : pipeline.getComponents()) {
            ExecutionComponent execComponent = createComponent(pplComponent);
            components.put(pplComponent.getIri(), execComponent);
        }
    }

    private ExecutionComponent createComponent(PipelineComponent pplComponent) {
        ExecutionComponent execComponent = new ExecutionComponent(pplComponent);
        initializeComponent(execComponent);
        return execComponent;
    }

    private void initializeComponent(ExecutionComponent execComponent) {
        for (Port pplDataUnit : execComponent.getPorts()) {
            DataUnit execDataUnit = createDataUnit(pplDataUnit);
            execComponent.getDataUnits().add(execDataUnit);
            dataUnits.put(pplDataUnit.getIri(), execDataUnit);
        }
    }

    private DataUnit createDataUnit(Port pplPort) {
        File loadPath;
        DataSource source = pplPort.getDataSource();
        if (source == null) {
            loadPath = null;
        } else {
            loadPath = resourceManager.resolveExecutionPath(
                    source.getExecution(),
                    source.getDataPath()
            );
        }
        if (pplPort.isSaveDebugData()) {
            String debugVirtualPathSuffix =
                    String.format("%03d", dataUnits.size());
            File saveDirectory = resourceManager.getWorkingDirectory(
                    "dataunit-" + debugVirtualPathSuffix);
            String relativeDataPath = resourceManager.relative(saveDirectory);
            return new DataUnit(pplPort, debugVirtualPathSuffix,
                    saveDirectory, loadPath, relativeDataPath);
        } else {
            return new DataUnit(pplPort, loadPath);
        }
    }

}
