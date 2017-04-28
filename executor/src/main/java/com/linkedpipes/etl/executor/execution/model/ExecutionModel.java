package com.linkedpipes.etl.executor.execution.model;

import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.executor.pipeline.model.DataSource;
import com.linkedpipes.etl.executor.pipeline.model.ExecutionType;
import com.linkedpipes.etl.executor.pipeline.model.PipelineModel;
import com.linkedpipes.etl.executor.pipeline.model.Port;

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

        private final Port port;

        private final String debugVirtualPathSuffix;

        private final File saveDirectory;

        private final File loadDirectory;

        private final String relativeSaveDataPath;

        public DataUnit(
                Port dataUnit,
                String debugVirtualPathSuffix,
                File saveDirectory,
                File loadDirectory,
                String relativeDataPath) {
            this.port = dataUnit;
            this.debugVirtualPathSuffix = debugVirtualPathSuffix;
            this.saveDirectory = saveDirectory;
            this.loadDirectory = loadDirectory;
            this.relativeSaveDataPath = relativeDataPath;
        }

        public DataUnit(
                Port dataUnit,
                File loadDirectory) {
            this.port = dataUnit;
            this.debugVirtualPathSuffix = null;
            this.saveDirectory = null;
            this.loadDirectory = loadDirectory;
            this.relativeSaveDataPath = null;
        }

        public String getDataUnitIri() {
            return port.getIri();
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

        public String getRelativeSaveDataPath() {
            return relativeSaveDataPath;
        }

        public Port getPort() {
            return port;
        }

    }

    public class Component {

        private final String iri;

        private final com.linkedpipes.etl.executor.pipeline.model.Component
                component;

        private final List<DataUnit> dataUnits = new ArrayList<>(4);

        private ExecutionStatus status;

        public Component(String iri,
                com.linkedpipes.etl.executor.pipeline.model.Component component) {
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

    public Component getComponent(
            com.linkedpipes.etl.executor.pipeline.model.Component component) {
        return components.get(component.getIri());
    }

    public List<DataUnit> getDataUnitsForInitialization() {
        final List<DataUnit> usedDataUnits = new LinkedList<>();
        for (Component component : components.values()) {
            final ExecutionType execType =
                    component.component.getExecutionType();
            if (execType == ExecutionType.SKIP) {
                continue;
            }
            usedDataUnits.addAll(component.getDataUnits());
        }
        return usedDataUnits;
    }

    public void initialize(PipelineModel pipeline) {
        for (com.linkedpipes.etl.executor.pipeline.model.Component pplComponent : pipeline.getComponents()) {
            final Component execComponent = createComponent(pplComponent);
            components.put(pplComponent.getIri(), execComponent);
            initializeComponent(execComponent);
        }
    }

    private Component createComponent(
            com.linkedpipes.etl.executor.pipeline.model.Component pplComponent) {
        final String componentIri = iri + "/component/" + components.size();
        final Component execComponent =
                new Component(componentIri, pplComponent);
        return execComponent;
    }

    private void initializeComponent(Component execComponent) {
        for (Port pplDataUnit : execComponent.component.getPorts()) {
            final DataUnit execDataUnit = createDataUnit(pplDataUnit);
            execComponent.dataUnits.add(execDataUnit);
            dataUnits.put(pplDataUnit.getIri(), execDataUnit);
        }
    }

    private DataUnit createDataUnit(Port pplPort) {

        final File loadPath;
        final DataSource source = pplPort.getDataSource();
        if (source == null) {
            loadPath = null;
        } else {
            loadPath = resourceManager.resolveExecutionPath(
                    source.getExecution(),
                    source.getDataPath()
            );
        }

        if (pplPort.isSaveDebugData()) {
            final String debugVirtualPathSuffix =
                    String.format("%03d", dataUnits.size());
            final File saveDirectory = resourceManager.getWorkingDirectory(
                    "dataunit-" + debugVirtualPathSuffix);
            final String relativeDataPath = resourceManager.relative(
                    saveDirectory);
            return new DataUnit(pplPort, debugVirtualPathSuffix,
                    saveDirectory, loadPath, relativeDataPath);
        } else {
            return new DataUnit(pplPort, loadPath);
        }
    }

}
