package com.linkedpipes.etl.executor.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;
import com.linkedpipes.etl.executor.execution.model.ExecutionOverviewModel;
import com.linkedpipes.etl.executor.execution.model.ExecutionStatusMonitor;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Represent an execution model.
 * The model is saved to the dist after every modification, if not
 * stated else in the method description.
 *
 * The model represent the execution state of the pipeline.
 */
public class Execution implements ExecutionObserver {

    private static final Logger LOG = LoggerFactory.getLogger(Execution.class);

    private ExecutionModel executionModel;

    private final ExecutionModelV1 executionModelV1;

    private ExecutionStatusMonitor statusMonitor;

    private ExecutionOverviewModel executionOverviewModel;

    private ResourceManager resourceManager;

    public Execution(ResourceManager resourceManager, String iri) {
        this.resourceManager = resourceManager;
        this.executionModel = new ExecutionModel(resourceManager, iri);
        this.executionModelV1 = new ExecutionModelV1(iri, resourceManager);
        this.statusMonitor = new ExecutionStatusMonitor();
        this.executionOverviewModel = new ExecutionOverviewModel(iri,
                statusMonitor);
    }

    public ExecutionModel getModel() {
        return executionModel;
    }

    public ExecutionOverviewModel getExecutionOverviewModel() {
        return executionOverviewModel;
    }

    public void close() {
        writeToDisk();
    }

    @Override
    public void onExecutionBegin() {
        executionOverviewModel.onExecutionBegin(new Date());
    }

    @Override
    public void onMapComponentBegin(ExecutionModel.Component component) {
        executionModelV1.onComponentBegin(component);
    }

    @Override
    public void onMapComponentFailed(ExecutionModel.Component component,
            LpException exception) {
        LOG.error("Component mapping failed.", exception);
        executionModelV1.onComponentFailed(component, exception);
        statusMonitor.onMapComponentFailed();
        writeToDisk();
    }

    @Override
    public void onMapComponentSuccessful(ExecutionModel.Component component) {
        executionModelV1.onComponentMapped(component);
        executionOverviewModel.onComponentExecutionEnd();
        writeToDisk();
    }

    @Override
    public void onExecuteComponentInitializing(
            ExecutionModel.Component component) {
        executionModelV1.onComponentBegin(component);
    }

    @Override
    public void onExecuteComponentFailed(ExecutionModel.Component component,
            LpException exception) {
        LOG.error("Component execution failed.", exception);
        executionModelV1.onComponentFailed(component, exception);
        statusMonitor.onExecuteComponentFailed();
        writeToDisk();
    }

    @Override
    public void onExecuteComponentSuccessful(
            ExecutionModel.Component component) {
        executionModelV1.onComponentEnd(component);
        executionOverviewModel.onComponentExecutionEnd();
        writeToDisk();
    }

    @Override
    public void onExecuteComponentCantSaveDataUnit(
            ExecutionModel.Component component, LpException exception) {
        executionModelV1.onExecutionFailed();
        statusMonitor.onExecuteComponentCantSaveDataUnit();
    }

    @Override
    public void onComponentUserCodeBegin(ExecutionModel.Component component) {
        // No operation here.
    }

    @Override
    public void onComponentUserCodeFailed(ExecutionModel.Component component) {
        // No operation here.
    }

    @Override
    public void onComponentUserCodeSuccessful(
            ExecutionModel.Component component) {
        // No operation here.
    }

    @Override
    public void onEvent(ExecutionModel.Component component, Event event) {
        executionModelV1.onEvent(component, event);
    }

    @Override
    public void onCantCreateComponentExecutor(
            ExecutionModel.Component component, LpException exception) {
        executionModelV1.onExecutionFailed();
        statusMonitor.onCantCreateComponentExecutor();
    }

    @Override
    public void onPipelineLoaded(PipelineModel pipeline) {
        executionModelV1.bindToPipeline(pipeline);
        statusMonitor.onPipelineLoaded();
        executionModel.initialize(pipeline);
        executionOverviewModel.onPipelineLoaded(pipeline);
        writeToDisk();
    }

    @Override
    public void onInvalidPipeline(PipelineModel pipeline,
            LpException exception) {
        if (pipeline != null) {
            executionModelV1.bindToPipeline(pipeline);
        }
        executionModelV1.onExecutionFailed();
        statusMonitor.onInvalidPipeline();
        writeToDisk();
    }

    @Override
    public void onCantPreparePipeline(LpException exception) {
        executionModelV1.onExecutionFailed();
        statusMonitor.onCantPreparePipeline();
    }

    @Override
    public void onObserverBeginFailed(LpException exception) {
        executionModelV1.onExecutionFailed();
        statusMonitor.onObserverBeginFailed();
    }

    @Override
    public void onDataUnitsLoadingFailed(LpException exception) {
        executionModelV1.onExecutionFailed();
        statusMonitor.onDataUnitsLoadingFailed();
    }

    @Override
    public void onComponentsLoadingFailed(LpException exception) {
        executionModelV1.onExecutionFailed();
        statusMonitor.onComponentsLoadingFailed();
    }

    @Override
    public void onExecutionEnd() {
        executionModelV1.onExecutionEnd();
        statusMonitor.onExecutionEnd();
        executionOverviewModel.onExecutionEnd(new Date());
    }

    @Override
    public void onCancelRequest() {
        executionModelV1.onExecutionCancelled();
        statusMonitor.onCancelRequest();
        executionOverviewModel.onExecutionCancelling();
        writeToDisk();
    }

    @Override
    public void onObserverEndFailed(LpException exception) {
        executionModelV1.onExecutionFailed();
        statusMonitor.onObserverEndFailed();
    }

    @Override
    public void onComponentsExecutionModelBegin() {
        executionModelV1.onExecutionBegin();
        statusMonitor.onComponentsExecutionBegin();
    }

    @Override
    public void onComponentsExecutionModelEnd() {
        // No operation here.
    }

    /**
     * For backward compatibility, writeToDisk the V1 execution model.
     *
     * @param stream
     * @param format
     */
    public void writeV1Execution(OutputStream stream, RDFFormat format)
            throws ExecutorException {
        executionModelV1.writeToDisk(stream, format);
    }

    public void writeToDisk() {
        executionModelV1.writeToDisk();
        writeExecutionOverviewToDisk();
    }

    private void writeExecutionOverviewToDisk() {
        final ObjectMapper objectMapper = new ObjectMapper();
        try (final OutputStream stream = new FileOutputStream(
                resourceManager.getExecutionOverviewJsonFile())) {
            objectMapper.writeValue(stream,
                    executionOverviewModel.toJson(objectMapper));
        } catch (IOException ex) {
            LOG.error("Can't save execution overview", ex);
        }
    }

}
