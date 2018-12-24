package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;

import java.util.HashMap;
import java.util.Map;

class Pipeline implements Loadable {

    private Map<String, Component> components = new HashMap<>();

    private Map<String, Connection> connections = new HashMap<>();

    private Repository repository = null;

    private ExecutionProfile executionProfile = null;

    @Override
    public Loadable load(String predicate, RdfValue value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_COMPONENT:
                Component newComponent = new Component();
                components.put(value.asString(), newComponent);
                return newComponent;
            case LP_PIPELINE.HAS_CONNECTION:
                Connection newConnection = new Connection();
                connections.put(value.asString(), newConnection);
                return newConnection;
            case LP_PIPELINE.HAS_REPOSITORY:
                repository = new Repository();
                return repository;
            case LP_EXEC.HAS_EXECUTION_PROFILE:
                executionProfile = new ExecutionProfile();
                return executionProfile;
            default:
                break;
        }
        return null;
    }

    public Map<String, Component> getComponents() {
        return components;
    }

    public Component getDataUnitOwner(DataUnit dataUnit) {
        for (Component component : components.values()) {
            if (component.getDataUnits().contains(dataUnit)) {
                return component;
            }
        }
        return null;
    }

    public Map<String, Connection> getConnections() {
        return connections;
    }

    public DataUnit getDataUnit(String dataUnitIri) {
        for (Component component : components.values()) {
            for (DataUnit dataUnit : component.getDataUnits()) {
                if (dataUnit.getResource().equals(dataUnitIri)) {
                    return dataUnit;
                }
            }
        }
        return null;
    }

    public Repository getRepository() {
        return repository;
    }

    public ExecutionProfile getExecutionProfile() {
        return executionProfile;
    }

}
