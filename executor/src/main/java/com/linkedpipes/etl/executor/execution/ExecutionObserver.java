package com.linkedpipes.etl.executor.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.execution.message.ComponentMessageWriter;
import com.linkedpipes.etl.executor.execution.message.ExecutionMessageWriter;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;
import com.linkedpipes.etl.executor.pipeline.model.PipelineModel;
import com.linkedpipes.etl.rdf4j.Statements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionObserver {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionObserver.class);

    private final AtomicInteger messageCounter = new AtomicInteger();

    private final ExecutionModel execution;

    private final ExecutionStatusMonitor status;

    private final ExecutionOverview overview;

    private final ResourceManager resourceManager;

    private final ExecutionMessageWriter pipelineMessages;

    private final ExecutionInformation information;

    private final Map<ExecutionComponent, ComponentMessageWriter>
            componentMessages = new HashMap<>();

    public ExecutionObserver(ResourceManager resourceManager, String iri) {
        this.resourceManager = resourceManager;
        this.execution = new ExecutionModel(resourceManager, iri);
        this.status = new ExecutionStatusMonitor();
        this.overview = new ExecutionOverview(iri, status);
        this.pipelineMessages = new ExecutionMessageWriter(
                iri, this.messageCounter,
                resourceManager.getPipelineMessageFile());
        this.information = new ExecutionInformation(
                this.status, this.execution,
                resourceManager.getExecutionFile());
    }

    public ExecutionModel getModel() {
        return execution;
    }

    public ExecutionOverview getExecutionOverviewModel() {
        return overview;
    }

    public void close() {
        this.writeOverviewToDisk();
        this.writePipelineMessagesToDisk();
        this.writeInformationToDisk();
        if (!this.componentMessages.isEmpty()) {
            LOG.error("Some components were not closed.");
        }
    }

    private void writeOverviewToDisk() {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = resourceManager.getOverviewFile();
        try (OutputStream stream = new FileOutputStream(file)) {
            objectMapper.writeValue(stream, overview.toJsonLd(objectMapper));
        } catch (IOException ex) {
            LOG.error("Can't save execution overview.", ex);
        }
    }

    private void writePipelineMessagesToDisk() {
        try {
            this.pipelineMessages.save();
        } catch (IOException ex) {
            LOG.error("Can't save pipeline messages.", ex);
        }
    }

    private void writeInformationToDisk() {
        try {
            this.information.save();
        } catch (IOException ex) {
            LOG.error("Can't save pipeline messages.", ex);
        }
    }

    public void onExecutionBegin() {
        LOG.info("onExecutionBegin");
        this.pipelineMessages.onExecutionBegin();
        this.overview.onExecutionBegin(new Date());
        this.writePipelineMessagesToDisk();
        this.writeOverviewToDisk();
    }

    public void onBeforeComponentExecution(ExecutionComponent component) {
        ComponentMessageWriter writer = new ComponentMessageWriter(
                this.execution.getIri(),
                this.messageCounter,
                this.resourceManager.getComponentMessageFile(component));
        this.componentMessages.put(component, writer);
    }

    public void onAfterComponentExecution(
            ExecutionComponent component) throws IOException {
        this.getComponentWriter(component).save();
        this.componentMessages.remove(component);
    }

    private ComponentMessageWriter getComponentWriter(
            ExecutionComponent component) {
        return this.componentMessages.get(component);
    }

    public void onComponentEvent(ExecutionComponent component, Event event) {
        this.getComponentWriter(component).addEvent(component, event);
        this.writeComponentMessagesToDisk(component);
    }

    private void writeComponentMessagesToDisk(ExecutionComponent component) {
        try {
            this.getComponentWriter(component).save();
        } catch (IOException ex) {
            LOG.error("Can't save pipeline messages.", ex);
        }
    }

    public void onMapComponentBegin(ExecutionComponent component) {
        LOG.info("onMapComponentBegin : {}",
                component.getIri());
        // TODO Replace with special status?
        this.information.onComponentBegin(component);
        this.getComponentWriter(component).onComponentBegin(component);
        this.writeInformationToDisk();
        this.writeComponentMessagesToDisk(component);
    }

    public void onMapComponentFailed(
            ExecutionComponent component, LpException exception) {
        LOG.error("onMapComponentFailed : {}",
                component.getIri(), exception);
        this.getComponentWriter(component).onComponentFailed(
                component, exception);
        this.status.onMapComponentFailed();
        this.information.onComponentFailed(component);
        this.writeInformationToDisk();
    }

    public void onMapComponentSuccessful(ExecutionComponent component) {
        LOG.info("onMapComponentSuccessful : {}",
                component.getIri());
        this.overview.onComponentExecutionEnd();
        this.information.onMapComponentSuccessful(component);
        this.writeInformationToDisk();
    }

    public void onExecuteComponentInitializing(ExecutionComponent component) {
        LOG.info("onExecuteComponentInitializing : {}",
                component.getIri());
        this.information.onComponentBegin(component);
        this.getComponentWriter(component).onComponentBegin(component);
        this.writeInformationToDisk();
        this.writeComponentMessagesToDisk(component);
    }

    public void onExecuteComponentFailed(
            ExecutionComponent component, LpException exception) {
        LOG.error("onExecuteComponentFailed : {}",
                component.getIri(), exception);
        this.getComponentWriter(component).onComponentFailed(
                component, exception);
        this.status.onExecuteComponentFailed();
        this.information.onComponentFailed(component);
        this.writeInformationToDisk();
        this.writeComponentMessagesToDisk(component);
    }

    public void onExecuteComponentSuccessful(ExecutionComponent component) {
        LOG.info("onExecuteComponentSuccessful : {}",
                component.getIri());
        this.getComponentWriter(component).onComponentEnd(component);
        this.overview.onComponentExecutionEnd();
        this.information.onComponentEnd(component);
        this.writeOverviewToDisk();
        this.writeInformationToDisk();
    }

    public void onExecuteComponentCantSaveDataUnit(
            ExecutionComponent component, LpException exception) {
        LOG.error("onExecuteComponentFailed : {}",
                component.getIri(), exception);
        this.status.onExecuteComponentCantSaveDataUnit();
        // TODO Add message.
    }

    public void onComponentUserCodeBegin(ExecutionComponent component) {
        LOG.info("onComponentUserCodeBegin : {}",
                component.getIri());
    }

    public void onComponentUserCodeFailed(
            ExecutionComponent component, Throwable throwable) {
        LOG.info("onComponentUserCodeFailed : {}",
                component.getIri());
    }

    public void onComponentUserCodeSuccessful(ExecutionComponent component) {
        LOG.info("onComponentUserCodeSuccessful : {}",
                component.getIri());
    }

    public void onCantCreateComponentExecutor(
            ExecutionComponent component, LpException exception) {
        LOG.error("onCantCreateComponentExecutor : {}",
                component.getIri(), exception);
        this.status.onCantCreateComponentExecutor();
        // TODO Add message.
    }

    public void onPipelineLoaded(PipelineModel pipeline) {
        LOG.info("onPipelineLoaded");
        this.status.onPipelineLoaded();
        this.execution.initialize(pipeline);
        this.overview.onPipelineLoaded(pipeline);
        this.information.onPipelineLoaded(pipeline);
        this.writeInformationToDisk();
        this.writeOverviewToDisk();
    }

    public void onCantLoadPipeline(LpException exception) {
        LOG.info("onCantLoadPipeline", exception);
        this.status.onInvalidPipeline();
        // TODO Add message.
    }

    public void onCantPreparePipeline(LpException exception) {
        LOG.error("onCantLoadPipeline", exception);
        this.status.onCantPreparePipeline();
        // TODO Add message.
    }

    public void onObserverBeginFailed(LpException exception) {
        LOG.error("onObserverBeginFailed", exception);
        this.status.onObserverBeginFailed();
        // TODO Add message.
    }

    public void onDataUnitsLoadingFailed(LpException exception) {
        LOG.error("onDataUnitsLoadingFailed", exception);
        this.status.onDataUnitsLoadingFailed();
        // TODO Add message.
    }

    public void onComponentsLoadingFailed(LpException exception) {
        LOG.error("onComponentsLoadingFailed", exception);
        this.status.onComponentsLoadingFailed();
        // TODO Add message.
    }

    public void onExecutionFailedOnThrowable(Throwable exception) {
        LOG.error("onExecutionFailedOnThrowable", exception);
        this.status.onExecutionFailedOnThrowable();
        // TODO Add message.
    }

    public void onExecutionEnd() {
        LOG.info("onExecutionEnd");
        this.pipelineMessages.onExecutionEnd();
        this.status.onExecutionEnd();
        this.overview.onExecutionEnd(new Date());
        this.writeOverviewToDisk();
    }

    public void onCancelRequest() {
        LOG.info("onCancelRequest");
        this.status.onCancelRequest();
        this.overview.onExecutionCancelling();
        // TODO Add message.
        this.writeOverviewToDisk();
    }

    public void onObserverEndFailed(LpException exception) {
        LOG.error("onObserverEndFailed", exception);
        this.status.onObserverEndFailed();
        // TODO Add message.
    }

    public void onCantSaveComponentMessages(
            ExecutionComponent component, Exception exception) {
        LOG.error("onCantSaveComponentMessages", exception);
        this.status.onCantSaveComponentMessages();
    }


    public void onComponentsExecutionBegin() {
        this.status.onComponentsExecutionBegin();
    }

    public void onComponentsExecutionEnd() {
        // No operation here.
    }

    public boolean isExecutionSuccessful() {
        return this.status.isExecutionSuccessful();
    }

    public ExecutionInformation getInformation() {
        return information;
    }

    public ExecutionMessageWriter getPipelineMessages() {
        return pipelineMessages;
    }

    public Statements getComponentMessages(ExecutionComponent component)
            throws IOException {
        ComponentMessageWriter writer = this.componentMessages.get(component);
        if (writer != null) {
            return writer.getStatements();
        }
        File file = this.resourceManager.getComponentMessageFile(component);
        if (!file.exists()) {
            return new Statements(Collections.emptyList());
        }
        Statements statements = Statements.ArrayList();
        statements.addAll(file);
        return statements;
    }

}
