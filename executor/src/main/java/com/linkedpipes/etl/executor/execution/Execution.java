package com.linkedpipes.etl.executor.execution;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.util.*;

/**
 * Represent an execution model.
 * The model is saved to the dist after every modification, if not
 * stated else in the method description.
 *
 * The model represent the execution state of the pipeline.
 */
public class Execution {

    /**
     * Represent a component to execute.
     */
    public class Component {

        private final String iri;

        private final PipelineModel.Component component;

        private final List<DataUnit> dataUnits = new ArrayList<>(4);

        public Component(String iri,
                PipelineModel.Component component) {
            this.iri = iri;
            this.component = component;
        }

        /**
         * @return IRI of executed component.
         */
        public String getComponentIri() {
            return component.getIri();
        }

        /**
         * @return Data units used by the component.
         */
        public List<DataUnit> getDataUnits() {
            return Collections.unmodifiableList(dataUnits);
        }

    }

    /**
     * Represent information about data unit in a execution.
     */
    public class DataUnit {

        private final PipelineModel.DataUnit dataUnit;

        private File saveDirectory;

        /**
         * Also used to create data save directory.
         */
        private final String debugVirtualPath;

        public DataUnit(PipelineModel.DataUnit dataUnit,
                String debugVirtualPath) {
            this.dataUnit = dataUnit;
            this.debugVirtualPath = debugVirtualPath;
        }

        /**
         * @return Path to load content from or null if there is no mapping.
         */
        public File getLoadDirectory() {
            final PipelineModel.DataSource source = dataUnit.getDataSource();
            if (source == null) {
                return null;
            }
            return resourceManager.resolveExecutionPath(
                    source.getExecution(), source.getLoadPath());
        }

        /**
         * @return IRI of data unit in a pipeline.
         */
        public String getDataUnitIri() {
            return dataUnit.getIri();
        }

        /**
         * @return Save directory for given data unit or null if the content
         * should not be saved.
         */
        public File getSaveDirectory() {
            if (saveDirectory == null) {
                saveDirectory = resourceManager.getWorkingDirectory(
                        "save-" + debugVirtualPath);
            }
            return saveDirectory;
        }

        /**
         * @return Path to the data relative to the execution root.
         */
        public String getRelativeDataPath() {
            return resourceManager.relative(getSaveDirectory());
        }

        /**
         * @return Virtual path to the debug directory.
         */
        public String getVirtualDebugPath() {
            return debugVirtualPath;
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(Execution.class);

    private Pipeline pipeline;

    private final ResourceManager resourceManager;

    /**
     * Under the component IRI store information about component execution.
     */
    private final Map<String, Component> components = new HashMap<>();

    /**
     * List of all data units.
     */
    private final Map<String, DataUnit> dataUnits = new HashMap<>();

    /**
     * Execution IRI.
     */
    private final String iri;

    private final ExecutionModelV1 v1Execution;

    public Execution(ResourceManager resourceManager,
            String executionIri) {
        this.resourceManager = resourceManager;
        this.iri = executionIri;
        this.v1Execution = new ExecutionModelV1(executionIri, resourceManager);

    }

    /**
     * Close and save the execution.
     */
    public void close() {
        LOG.info("close");
    }

    /**
     * @return Execution component record for given component.
     */
    public Component getComponent(PipelineModel.Component component) {
        return components.get(component.getIri());
    }

    public List<DataUnit> getUsedDataUnits() {
        final List<DataUnit> usedDataUnits = new LinkedList<>();
        for (Component component : components.values()) {
            final PipelineModel.ExecutionType execType = pipeline.getModel()
                    .getComponent(component.getComponentIri())
                    .getExecutionType();
            if (execType == PipelineModel.ExecutionType.SKIP) {
                continue;
            }
            usedDataUnits.addAll(component.getDataUnits());
        }
        return usedDataUnits;
    }

    public void onEvent(Execution.Component component, Event event) {
        LOG.info("onEvent");
        v1Execution.onEvent(component, event);
    }

    public void onObserverBeginFailed(LpException exception) {
        LOG.info("onObserverBeginFailed", exception);
        v1Execution.onExecutionFailed();
    }

    public void onDataUnitsLoadingFailed(LpException exception) {
        LOG.info("onDataUnitsLoadingFailed", exception);
        v1Execution.onExecutionFailed();
    }

    public void onComponentsLoadingFailed(LpException exception) {
        LOG.info("onComponentsLoadingFailed", exception);
        v1Execution.onExecutionFailed();
    }

    public void onObserverEndFailed(LpException exception) {
        LOG.info("onObserverEndFailed", exception);
        v1Execution.onExecutionFailed();
    }

    /**
     * Called once the component is ready for execution.
     *
     * @param component
     */
    public void onComponentInitialize(Execution.Component component) {
        LOG.info("onComponentInitialize");
    }

    /**
     * Initialization is done, component code is about to be executed.
     *
     * @param component
     */
    public void onComponentBegin(Execution.Component component) {
        LOG.info("onComponentBegin");
        v1Execution.onComponentBegin(component);
    }

    /**
     * Reports successful end of a component execution, called
     * after {@link #onComponentBegin(Component)}}.
     *
     * @param component
     */
    public void onComponentEnd(Execution.Component component) {
        LOG.info("onComponentEnd");
        v1Execution.onComponentEnd(component);
    }

    /**
     * Alternative to {@link #onComponentEnd(Component)}.
     *
     * @param component
     * @param exception
     */
    public void onComponentFailed(Execution.Component component,
            LpException exception) {
        LOG.info("onComponentFailed", exception);
        v1Execution.onComponentFailed(component, exception);
    }

    /**
     * Alternative to {@link #onComponentInitialize(Component)}.
     * Represents an execution type of a component.
     *
     * @param component
     */
    public void onComponentMapped(Execution.Component component) {
        LOG.info("onComponentMapped");
        v1Execution.onComponentMapped(component);
    }

    /**
     * Alternative to {@link #onComponentInitialize(Component)}.
     * Represents an execution type of a component.
     *
     * @param component
     */
    public void onComponentSkipped(Execution.Component component) {
        LOG.info("onComponentSkipped");
    }

    public void onCantPrepareDataUnits(Execution.Component component,
            LpException exception) {
        LOG.info("onCantPrepareDataUnits", exception);
        v1Execution.onComponentFailed(component, exception);
    }

    public void onCantSaveDataUnits(Execution.Component component,
            LpException exception) {
        LOG.info("onCantSaveDataUnits", exception);
        v1Execution.onExecutionFailed();
    }


    //
    //
    //
    //

    public void onCancelRequest() {
        LOG.info("onCancelRequest");
    }

    public void onInitializationBegin() {
        LOG.info("onInitializationBegin");
    }

    /**
     * Bind to an empty pipeline instance.
     *
     * @param pipeline
     */
    public void bindToPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public void onInvalidPipeline(LpException exception) {
        LOG.info("onInvalidPipeline", exception);
        // Try to bing the pipeline to get some information.
        if (pipeline.getModel() != null) {
            v1Execution.bindToPipeline(pipeline.getModel());
        }
        v1Execution.onExecutionFailed();
    }

    /**
     * Called once pipeline instance is loaded.
     */
    public void onPipelineLoaded() {
        this.v1Execution.bindToPipeline(pipeline.getModel());
        //
        String baseIri = "http://execution/";
        Integer componentCounter = 0;
        Integer dataUnitCounter = 0;
        // Construct the execution model from the pipeline.
        for (PipelineModel.Component pplComponent :
                pipeline.getModel().getComponents()) {
            final Component execComponent = new Component(
                    baseIri + "/components/" + ++componentCounter,
                    pplComponent);
            for (PipelineModel.DataUnit pplDataUnit :
                    pplComponent.getDataUnits()) {
                dataUnitCounter++;
                final DataUnit execDataUnit = new DataUnit(pplDataUnit,
                        String.format("%03d", dataUnitCounter));
                execComponent.dataUnits.add(execDataUnit);
                dataUnits.put(pplDataUnit.getIri(), execDataUnit);
            }
            components.put(pplComponent.getIri(), execComponent);
        }
    }

    public void onCantPreparePipeline(LpException exception) {
        LOG.info("onCantPreparePipeline", exception);
    }

    public void onComponentsExecutionBegin() {
        LOG.info("onComponentsExecutionBegin");
        v1Execution.onExecutionBegin();
    }

    public void onInvalidComponent(Execution.Component component,
            LpException exception) {
        LOG.info("onInvalidComponent", exception);
        v1Execution.onExecutionFailed();
    }

    public void onComponentsExecutionEnd() {
        LOG.info("onComponentsExecutionEnd");
        v1Execution.onExecutionBegin();
    }

    /**
     * Called at the end of the execution.
     */
    public void onExecutionEnd() {
        LOG.info("onExecutionEnd");
        v1Execution.onExecutionEnd();
    }

    /**
     * For backward compatibility, write the V1 execution model.
     *
     * @param stream
     * @param format
     */
    public void writeV1Execution(OutputStream stream, RDFFormat format)
            throws ExecutorException {
        v1Execution.write(stream, format);
    }

}
