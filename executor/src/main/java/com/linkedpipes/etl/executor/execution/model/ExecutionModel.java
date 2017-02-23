package com.linkedpipes.etl.executor.execution.model;

import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;

import java.io.File;
import java.util.*;

/**
 * Represent a pipeline execution model.
 */
public class ExecutionModel {

    /**
     * Represent information about data unit in a execution.
     */
    public class DataUnit {

        private final PipelineModel.DataUnit dataUnit;

        private final String debugVirtualPathSuffix;

        private final File saveDirectory;

        private final File loadDirectory;

        private final String relativeDataPath;

        public DataUnit(PipelineModel.DataUnit dataUnit,
                String debugVirtualPathSuffix,
                File saveDirectory,
                File loadDirectory,
                String relativeDataPath) {
            this.dataUnit = dataUnit;
            this.debugVirtualPathSuffix = debugVirtualPathSuffix;
            this.saveDirectory = saveDirectory;
            this.loadDirectory = loadDirectory;
            this.relativeDataPath = relativeDataPath;
        }

        public String getDataUnitIri() {
            return dataUnit.getIri();
        }

        public String getVirtualDebugPath() {
            return debugVirtualPathSuffix;
        }

        public File getLoadDirectory() {
            return loadDirectory;
        }

        public File getSaveDirectory() {
            return saveDirectory;
        }

        public String getRelativeDataPath() {
            return relativeDataPath;
        }

    }

    public class Component {

        private final String iri;

        private final PipelineModel.Component component;

        private final List<DataUnit> dataUnits = new ArrayList<>(4);

        private ExecutionStatus status;

        public Component(String iri,
                PipelineModel.Component component) {
            this.iri = iri;
            this.component = component;
        }

        public String getComponentIri() {
            return component.getIri();
        }

        public List<DataUnit> getDataUnits() {
            return Collections.unmodifiableList(dataUnits);
        }

    }

    /**
     * Use original component IRI from pipeline.
     */
    private final Map<String, Component> components = new HashMap<>();

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

    public Component getComponent(PipelineModel.Component component) {
        return components.get(component.getIri());
    }

    public List<DataUnit> getDataUnitsForInitialization() {
        final List<DataUnit> usedDataUnits = new LinkedList<>();
        for (Component component : components.values()) {
            final PipelineModel.ExecutionType execType =
                    component.component.getExecutionType();
            if (execType == PipelineModel.ExecutionType.SKIP) {
                continue;
            }
            usedDataUnits.addAll(component.getDataUnits());
        }
        return usedDataUnits;
    }

    public void initialize(PipelineModel pipeline) {
        for (PipelineModel.Component pplComponent : pipeline.getComponents()) {
            final Component execComponent = createComponent(pplComponent);
            components.put(pplComponent.getIri(), execComponent);
            initializeComponent(execComponent);
        }
    }

    private Component createComponent(PipelineModel.Component pplComponent) {
        final String componentIri = iri + "/component/" + components.size();
        final Component execComponent =
                new Component(componentIri, pplComponent);
        return execComponent;
    }

    private void initializeComponent(Component execComponent) {
        for (PipelineModel.DataUnit pplDataUnit :
                execComponent.component.getDataUnits()) {
            final DataUnit execDataUnit = createDataUnit(pplDataUnit);
            execComponent.dataUnits.add(execDataUnit);
            dataUnits.put(pplDataUnit.getIri(), execDataUnit);
        }
    }

    private DataUnit createDataUnit(PipelineModel.DataUnit pplDataUnit) {
        final String debugVirtualPathSuffix =
                String.format("%03d", dataUnits.size());

        final File saveDirectory = resourceManager.getWorkingDirectory(
                        "dataunit-" + debugVirtualPathSuffix);

        final String relativeDataPath = resourceManager.relative(saveDirectory);

        final File loadPath;
        final PipelineModel.DataSource source = pplDataUnit.getDataSource();
        if (source == null) {
            loadPath = null;
        } else {
            loadPath = resourceManager.resolveExecutionPath(
                    source.getExecution(),
                    source.getLoadPath()
            );
        }
        return new DataUnit(pplDataUnit, debugVirtualPathSuffix,
                saveDirectory, loadPath, relativeDataPath);
    }

}
