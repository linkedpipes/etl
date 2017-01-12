package com.linkedpipes.etl.executor.execution;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent an execution model.
 * The model is saved to the dist after every modification, if not
 * stated else in the method description.
 */
public class Execution {

    /**
     * Represent a component to execute.
     */
    public static class Component {

    }

    private static final Logger LOG = LoggerFactory.getLogger(Execution.class);

    private final Pipeline pipeline;

    private final ResourceManager resourceManager;

    /**
     * Under the component IRI store information about component execution.
     */
    private final Map<String, Component> components = new HashMap<>();

    public Execution(Pipeline pipeline, ResourceManager resourceManager) {
        this.pipeline = pipeline;
        this.resourceManager = resourceManager;
    }

    /**
     * @return Execution component record for given component.
     */
    public Component getComponent(PipelineModel.Component component) {
        return components.get(component.getIri());
    }

    /**
     * Close and save the execution.
     */
    public void close() {
        LOG.info("close");
    }

    public void onCancel() {
        LOG.info("onCancel");
    }

    public void onEvent(Execution.Component component, Event event) {
        LOG.info("onEvent");
    }

    public void onInitializationFailed(LpException exception) {
        LOG.info("onInitializationFailed", exception);
    }

    public void onObserverBeginFailed(LpException exception) {
        LOG.info("onObserverBeginFailed", exception);
    }

    public void onComponentsLoadingFailed(LpException exception) {
        LOG.info("onComponentsLoadingFailed", exception);
    }

    public void onObserverEndFailed(LpException exception) {
        LOG.info("onObserverEndFailed", exception);
    }

    public void onComponentInitialize(Execution.Component component) {
        LOG.info("onComponentInitialize");
    }

    public void onComponentBegin(Execution.Component component) {
        LOG.info("onComponentBegin");
    }

    public void onComponentEnd(Execution.Component component) {
        LOG.info("onComponentEnd");
    }

    public void onComponentFailed(Execution.Component component,
            LpException exception) {
        LOG.info("onComponentFailed", exception);
    }

    public void onInvalidComponent(Execution.Component component,
            LpException exception) {
        LOG.info("onInvalidComponent", exception);
    }

    public void onExecutionBegin() {
        LOG.info("onExecutionBegin");
    }

    public void onExecutionEnd() {
        LOG.info("onExecutionEnd");
    }

}
