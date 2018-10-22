package com.linkedpipes.etl.executor.dataunit;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.execution.model.DataUnit;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import com.linkedpipes.etl.executor.pipeline.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Can be used to query pipeline for information about port usage.
 */
class PipelineQuery {

    private final PipelineModel pipeline;

    public PipelineQuery(PipelineModel pipeline) {
        this.pipeline = pipeline;
    }

    public boolean isDataUnitUsed(
            ExecutionComponent component, DataUnit dataUnit)
            throws ExecutorException {
        PipelineComponent pplComponent = getPipelineComponent(component);
        Port port = getPort(pplComponent, dataUnit);
        return isPortUsed(pplComponent, port);
    }

    private PipelineComponent getPipelineComponent(
            ExecutionComponent component) throws ExecutorException {
        PipelineComponent pplComponent =
                this.pipeline.getComponent(component.getIri());
        if (pplComponent == null) {
            throw new ExecutorException(
                    "Missing component definition: {} for {}",
                    component.getIri());
        }
        return pplComponent;
    }

    private Port getPort(PipelineComponent component, DataUnit dataUnit)
            throws ExecutorException {
        // The DataUnit have a reference to port, we just want to be sure
        // that we have the right component.
        Port port = component.getPort(dataUnit.getIri());
        if (port == null) {
            throw new ExecutorException("Missing definition: {} for {}",
                    dataUnit.getIri(), component.getIri());
        }
        return port;
    }

    private boolean isPortUsed(PipelineComponent component, Port port)
            throws ExecutorException {
        switch (component.getExecutionType()) {
            case EXECUTE:
                return true;
            case SKIP:
                return false;
            case MAP:
                break;
            default:
                throw new ExecutorException("Invalid execution type: {} ",
                        component.getExecutionType());
        }
        if (port.isInput()) {
            return false;
        }
        for (Connection connection : findConnections(component, port)) {
            PipelineComponent source =
                    pipeline.getComponent(connection.getSourceComponent());
            if (source.getExecutionType() == ExecutionType.EXECUTE) {
                return true;
            }
            PipelineComponent target =
                    pipeline.getComponent(connection.getTargetComponent());
            if (target.getExecutionType() == ExecutionType.EXECUTE) {
                return true;
            }
        }
        return false;
    }

    private Collection<Connection> findConnections(
            PipelineComponent component, Port port) {
        Collection<Connection> output = new ArrayList<>();
        for (Connection connection : pipeline.getConnections()) {
            if (!connection.isDataConnection()) {
                continue;
            }
            if (isSource(connection, component, port) ||
                    isTarget(connection, component, port)) {
                output.add(connection);
            }
        }
        return output;
    }

    private boolean isSource(
            Connection connection, PipelineComponent component, Port port) {
        return connection.getSourceComponent().equals(component.getIri()) &&
                connection.getSourceBinding().equals(port.getBinding());
    }

    private boolean isTarget(
            Connection connection, PipelineComponent component, Port port) {
        return connection.getTargetComponent().equals(component.getIri()) &&
                connection.getTargetBinding().equals(port.getBinding());
    }

    public boolean isNoLongerUsed(
            Set<String> executedComponents,
            ExecutionComponent component,
            DataUnit dataUnit)
            throws ExecutorException {
        List<PipelineComponent> targetComponents =
                getTargets(component, dataUnit);
        for (PipelineComponent pplTarget : targetComponents) {
            if (executedComponents.contains(pplTarget.getIri())) {
                continue;
            }
            return false;
        }
        return true;
    }

    private List<PipelineComponent> getTargets(
            ExecutionComponent component, DataUnit dataUnit)
            throws ExecutorException {
        PipelineComponent pplComponent = getPipelineComponent(component);
        Port port = getPort(pplComponent, dataUnit);
        return pipeline.getConnections().stream()
                .filter((conn) -> conn.isDataConnection())
                .filter((conn) -> isSource(conn, pplComponent, port))
                .map((conn) -> pipeline.getComponent(conn.getTargetComponent()))
                .collect(Collectors.toList());
    }

}
