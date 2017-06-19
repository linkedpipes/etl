package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Component in a pipeline.
 */
public class Component implements Loadable {

    private final String iri;

    private String label;

    private final List<Port> ports = new ArrayList<>(2);

    private final List<Configuration> configurations = new ArrayList<>(3);

    private Integer executionOrder;

    private ExecutionType executionType;

    private String jarPath;

    private ConfigurationDescription configurationDescription;

    public Component(String iri) {
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

    /**
     * @return Ordered configuration entities for this component.
     */
    public List<Configuration> getConfigurations() {
        return configurations;
    }

    @Override
    public Loadable load(String predicate, RdfValue object)
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
            case LP_EXEC.HAS_CONFIGURATION:
                final Configuration configuration =
                        new Configuration(object.asString());
                configurations.add(configuration);
                return configuration;
            case LP_PIPELINE.HAS_JAR_URL:
                jarPath = object.asString();
                return null;
            case LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION:
                configurationDescription =
                        new ConfigurationDescription(object.asString());
                return configurationDescription;
            default:
                return null;
        }
    }

    public void afterLoad() throws InvalidPipelineException {
        check();
        sortConfigurations();
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
        if (configurationDescription == null) {
            throw new InvalidPipelineException(
                    "Missing configuration description: {} jar: {}",
                    iri, jarPath);
        }
        configurationDescription.check();
        for (Configuration configuration : configurations) {
            configuration.check();
        }
    }

    private void sortConfigurations() {
        Collections.sort(configurations,
                Comparator.comparingInt(x -> -x.getOrder()));
    }

}
