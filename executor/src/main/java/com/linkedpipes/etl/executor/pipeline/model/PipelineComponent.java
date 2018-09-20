package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PipelineComponent in a pipeline.
 */
public class PipelineComponent implements Loadable {

    private final String iri;

    private String label;

    private final List<Port> ports = new ArrayList<>(2);

    private String configurationGraph;

    private Integer executionOrder;

    private ExecutionType executionType;

    private String jarPath;

    private ConfigurationDescription configurationDescription;

    private String execution;

    public PipelineComponent(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }

    public List<Port> getPorts() {
        return Collections.unmodifiableList(ports);
    }

    public Port getPort(String iri) {
        for (Port dataUnit : ports) {
            if (dataUnit.getIri().equals(iri)) {
                return dataUnit;
            }
        }
        return null;
    }

    public Integer getExecutionOrder() {
        return executionOrder;
    }

    public ExecutionType getExecutionType() {
        return executionType;
    }

    public String getLabel() {
        return label;
    }

    public String getJarPath() {
        return jarPath;
    }

    public boolean shouldExecute() {
        switch (executionType) {
            case EXECUTE:
                return true;
            case SKIP:
            case MAP:
            default:
                return false;
        }
    }

    public ConfigurationDescription getConfigurationDescription() {
        return configurationDescription;
    }

    public String getConfigurationGraph() {
        return configurationGraph;
    }

    @Override
    public Loadable load(String predicate, BackendRdfValue object)
            throws RdfUtilsException {
        switch (predicate) {
            case SKOS.PREF_LABEL:
                label = object.asString();
                return null;
            case LP_EXEC.HAS_ORDER_EXEC:
                executionOrder = (int)object.asLong();
                return null;
            case LP_PIPELINE.HAS_DATA_UNIT:
                final Port newDataUnit = new Port(object.asString(), this);
                ports.add(newDataUnit);
                return newDataUnit;
            case LP_EXEC.HAS_EXECUTION_TYPE:
                switch (object.asString()) {
                    case LP_EXEC.TYPE_EXECUTE:
                        executionType = ExecutionType.EXECUTE;
                        break;
                    case LP_EXEC.TYPE_MAPPED:
                        executionType = ExecutionType.MAP;
                        break;
                    case LP_EXEC.TYPE_SKIP:
                        executionType = ExecutionType.SKIP;
                        break;
                    default:
                        throw new RdfUtilsException(
                                "Invalid exec. type : {} {}", iri, object);
                }
                return null;
            case LP_PIPELINE.HAS_CONFIGURATION_GRAPH:
                configurationGraph = object.asString();
                return null;
            case LP_PIPELINE.HAS_JAR_URL:
                jarPath = object.asString();
                return null;
            case LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION:
                configurationDescription =
                        new ConfigurationDescription(object.asString());
                return configurationDescription;
            case LP_EXEC.HAS_EXECUTION:
                execution = object.asString();
                return null;
            default:
                return null;
        }
    }

    public void afterLoad() throws InvalidPipelineException {
        check();
    }

    private void check() throws InvalidPipelineException {
        if (executionType == null) {
            throw new InvalidPipelineException(
                    "Missing execution type: {}", iri);
        }
        if (executionType == ExecutionType.SKIP) {
            // As the component is not executed we do not need
            // to validate the definition any more.
            return;
        }
        if (executionOrder == null) {
            throw new InvalidPipelineException(
                    "Missing execution executionOrder: {}", iri);
        }
        if (executionType == ExecutionType.MAP) {
            return;
        }
        if (configurationDescription == null) {
            throw new InvalidPipelineException(
                    "Missing configurationGraph description: {} jar: {}",
                    iri, jarPath);
        }
        if (configurationGraph == null) {
            throw new InvalidPipelineException(
                    "Missing configurationGraph for: {}", iri);
        }
        configurationDescription.check();
    }

    public String getExecution() {
        return execution;
    }
}
