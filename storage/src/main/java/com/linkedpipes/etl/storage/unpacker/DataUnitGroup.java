package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorComponent;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorConnection;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorPipeline;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Compute data unit connectionGroups.
 */
class DataUnitGroup {

    private final ExecutorPipeline pipeline;

    private Map<String, ExecutorPort> portsByComponentAndBinding;

    private Map<ExecutorPort, Integer> connectionGroups;

    public DataUnitGroup(ExecutorPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public void compute() {
        createPortMapAndList();
        createGroupsForConnections();
        setGroups();
    }

    private void createPortMapAndList() {
        portsByComponentAndBinding = new HashMap<>();
        for (ExecutorComponent component : pipeline.getComponents()) {
            // TODO Replace with component property shouldBeExecuted
            String type = component.getExecutionType();
            if (!type.equals(LP_EXEC.TYPE_EXECUTE)
                    && !type.equals(LP_EXEC.TYPE_MAPPED)) {
                continue;
            }
            for (ExecutorPort port : component.getPorts()) {
                String key = createKey(component, port);
                portsByComponentAndBinding.put(key, port);
            }
        }
    }

    private String createKey(ExecutorComponent component, ExecutorPort port) {
        return component.getIri() + "|" + port.getBinding();
    }

    private void createGroupsForConnections() {
        int counter = 0;
        connectionGroups = new HashMap<>();
        for (ExecutorConnection connection : pipeline.getConnections()) {
            ExecutorPort source =
                    portsByComponentAndBinding.get(getSourceKey(connection));
            ExecutorPort target =
                    portsByComponentAndBinding.get(getTargetKey(connection));
            if (hasGroupSet(source)) {
                if (hasGroupSet(target)) {
                    renamePortGroup(target, connectionGroups.get(source));
                }
                connectionGroups.put(target, connectionGroups.get(source));
            } else if (hasGroupSet(target)) {
                connectionGroups.put(source, connectionGroups.get(target));
            } else {
                int newGroup = ++counter;
                connectionGroups.put(source, newGroup);
                connectionGroups.put(target, newGroup);
            }
        }
    }

    private boolean hasGroupSet(ExecutorPort port) {
        return connectionGroups.containsKey(port);
    }

    private void renamePortGroup(ExecutorPort port, int target) {
        int source = connectionGroups.get(port);
        for (Map.Entry<ExecutorPort, Integer> entry
                : connectionGroups.entrySet()) {
            if (entry.getValue() == source) {
                entry.setValue(target);
            }
        }
    }

    private String getSourceKey(ExecutorConnection connection) {
        return connection.getSourceComponent() + "|"
                + connection.getSourceBinding();
    }

    private String getTargetKey(ExecutorConnection connection) {
        return connection.getTargetComponent() + "|"
                + connection.getTargetBinding();
    }

    private void setGroups() {
        int counter = 0;
        Map<Integer, Integer> connectionGroupToGroups = new HashMap<>();
        for (String key : getSortedPortsKeys()) {
            ExecutorPort port = portsByComponentAndBinding.get(key);
            if (connectionGroups.containsKey(port)) {
                int connectionGroup = connectionGroups.get(port);
                Integer group = connectionGroupToGroups.get(connectionGroup);
                if (group == null) {
                    group = ++counter;
                    connectionGroupToGroups.put(connectionGroup, group);
                }
                port.setGroup(group);
            } else {
                port.setGroup(++counter);
            }
        }
    }

    private List<String> getSortedPortsKeys() {
        List<String> keys = new ArrayList<>(portsByComponentAndBinding.size());
        keys.addAll(portsByComponentAndBinding.keySet());
        Collections.sort(keys);
        return keys;
    }

}
