package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.rdf4j.ClosableRdf4jSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.unpacker.model.GraphCollection;
import com.linkedpipes.etl.storage.unpacker.model.ModelLoader;
import com.linkedpipes.etl.storage.unpacker.model.designer.*;
import com.linkedpipes.etl.storage.unpacker.model.execution.Execution;
import com.linkedpipes.etl.storage.unpacker.model.execution.ExecutionComponent;
import com.linkedpipes.etl.storage.unpacker.model.execution.ExecutionPort;
import com.linkedpipes.etl.storage.unpacker.model.executor.*;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DesignerToExecutor {

    private DesignerPipeline source;

    private GraphCollection graphs;

    private ExecutorPipeline target;

    private List<DesignerRunAfter> runAfter;

    private final TemplateExpander jarTemplateExpander;

    private final ExecutionSource executionSource;

    public DesignerToExecutor(TemplateSource templateSource,
            ExecutionSource executionSource) {
        this.jarTemplateExpander = new TemplateExpander(templateSource);
        this.executionSource = executionSource;
    }

    public void transform(DesignerPipeline pipeline, GraphCollection graphs,
            UnpackOptions options) throws BaseException {
        this.source = pipeline;
        this.graphs = graphs;
        this.target = new ExecutorPipeline(getExecutionIri(options));
        this.runAfter = new ArrayList<>(pipeline.getRunAfter());
        //
        initializePipeline();
        initializePipelineProfile();
        convertConnections();
        convertAndExpandComponents();
        createExecutionMetadata(options);
        computeExecutionFlow(options);
        configurePorts(options);
        removeConnectionsForNonExecutedComponents();
        computeDataUnitGroups();
    }

    private String getExecutionIri(UnpackOptions options) {
        // TODO Create execution IRI and save pipeline IRI as a property.
        String executionIri = options.getExecutionIri();
        if (executionIri == null) {
            executionIri = source.getIri();
        }
        return executionIri;
    }

    private void initializePipeline() {
        target.setLabel(source.getLabel());
    }

    private void initializePipelineProfile() {
        ExecutionProfile source = this.source.getExecutionProfile();
        ExecutorProfile target = this.target.getExecutorProfile();
        //
        target.setRepositoryPolicy(source.getRdfRepositoryPolicy());
        target.setRepositoryType(source.getRdfRepositoryType());
    }

    private void convertConnections() {
        for (DesignerConnection connection : source.getConnections()) {
            target.addConnection(convertConnection(connection));
        }
    }

    private ExecutorConnection convertConnection(
            DesignerConnection srcConnection) {
        ExecutorConnection newConnection = new ExecutorConnection();

        newConnection.setIri(srcConnection.getIri());
        newConnection.setSourceComponent(srcConnection.getSourceComponent());
        newConnection.setSourceBinding(srcConnection.getSourceBinding());
        newConnection.setTargetBinding(srcConnection.getTargetBinding());
        newConnection.setTargetComponent(srcConnection.getTargetComponent());

        return newConnection;
    }

    private void convertAndExpandComponents() throws BaseException {
        jarTemplateExpander.setGraphs(graphs);
        for (DesignerComponent component : source.getComponents()) {
            // TODO Also pass connections and runAfter for possible modification
            target.addComponent(jarTemplateExpander.expand(component));
        }
    }

    private void createExecutionMetadata(UnpackOptions options) {
        ExecutorMetadata metadata = target.getExecutorMetadata();
        metadata.setTargetComponent(options.getRunToComponent());
        metadata.setDeleteWorkingData(options.isDeleteWorkingDirectory());
        metadata.setSaveDebugData(options.isSaveDebugData());
        if (options.getRunToComponent() == null) {
            if (options.getExecutionMapping().isEmpty()) {
                target.getExecutorMetadata().setExecutionType(
                        LP_EXEC.EXECUTION_FULL);
            } else {
                target.getExecutorMetadata().setExecutionType(
                        LP_EXEC.EXECUTION_DEBUG_FROM);
            }
        } else {
            if (options.getExecutionMapping().isEmpty()) {
                target.getExecutorMetadata().setExecutionType(
                        LP_EXEC.EXECUTION_DEBUG_TO);
            } else {
                target.getExecutorMetadata().setExecutionType(
                        LP_EXEC.EXECUTION_DEBUG_FROM_TO);
            }
        }
    }

    private void computeExecutionFlow(UnpackOptions options)
            throws BaseException {
        ExecutionFlow flowComputer = new ExecutionFlow(
                source, target, runAfter, options);
        flowComputer.computeExecutionTypeAndOrder();
    }

    private void configurePorts(UnpackOptions options) throws BaseException {
        for (ExecutorComponent component : target.getComponents()) {
            for (ExecutorPort port : component.getPorts()) {
                port.setSaveDebugData(options.isSaveDebugData());
            }
        }
        setPortMapping(options);
    }

    private void setPortMapping(UnpackOptions options) throws BaseException {
        // TODO Extract to another method
        for (UnpackOptions.ExecutionMapping executionMapping
                : options.getExecutionMapping()) {
            Execution execution = getExecution(executionMapping.getExecution());
            mapExecution(execution, executionMapping);
        }
    }

    private Execution getExecution(String iri)
            throws BaseException {
        Collection<Statement> statements = executionSource.getExecution(iri);
        ClosableRdf4jSource source = Rdf4jSource.wrapInMemory(statements);
        try {
            return ModelLoader.loadExecution(source);
        } catch (RdfUtilsException ex) {
            throw new BaseException("Can't load execution.", ex);
        } finally {
            source.close();
        }
    }

    private void mapExecution(Execution execution,
            UnpackOptions.ExecutionMapping executionMapping) {
        for (UnpackOptions.ComponentMapping mapping
                : executionMapping.getComponents()) {
            ExecutionComponent sourceComponent = execution.getComponent(
                    mapping.getSource());
            ExecutorComponent targetComponent = target.getComponent(
                    mapping.getTarget()
            );
            targetComponent.setExecutionType(LP_EXEC.TYPE_MAPPED);
            //
            for (ExecutorPort targetPort : targetComponent.getPorts()) {
                ExecutionPort sourcePort = sourceComponent.getPortByBinding(
                        targetPort.getBinding());
                String sourceExecution;
                if (sourcePort.getExecution() == null) {
                    sourceExecution = executionMapping.getExecution();
                } else {
                    sourceExecution = sourcePort.getExecution();
                }
                targetPort.setDataSource(new ExecutorDataSource(
                        sourcePort.getDataPath(), sourceExecution));
            }
        }
    }

    /**
     * Components must have set execution type.
     */
    private void removeConnectionsForNonExecutedComponents() {
        List<ExecutorConnection> connectionToRemove = new ArrayList<>();
        for (ExecutorConnection connection : target.getConnections()) {
            if (!componentActive(connection.getSourceComponent()) ||
                    !componentActive(connection.getTargetComponent())) {
                connectionToRemove.add(connection);
            }
        }
        target.getConnections().removeAll(connectionToRemove);
    }

    private boolean componentActive(String componentIri) {
        String type = target.getComponent(componentIri).getExecutionType();
        return LP_EXEC.TYPE_EXECUTE.equals(type) ||
                LP_EXEC.TYPE_MAPPED.equals(type);
    }

    private void computeDataUnitGroups() {
        DataUnitGroup dataUnitGroup = new DataUnitGroup(target);
        dataUnitGroup.compute();
    }

    public ExecutorPipeline getTarget() {
        return target;
    }

}
