package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerPipeline;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerRunAfter;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorComponent;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorConnection;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorPipeline;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Assign executionOrder and executionType to the components.
 */
class ExecutionFlow {

    private final DesignerPipeline source;

    private final ExecutorPipeline target;

    private final UnpackOptions options;

    private final List<DesignerRunAfter> runAfter;

    public ExecutionFlow(DesignerPipeline source, ExecutorPipeline target,
            List<DesignerRunAfter> runAfter, UnpackOptions options) {
        this.source = source;
        this.target = target;
        this.options = options;
        this.runAfter = runAfter;
    }

    public void computeExecutionTypeAndOrder() throws BaseException {
        setExecutionOrder();
        //
        Map<String, Set<String>> dependencies = createDependencyList();
        dependencies = filterDisabledComponents(dependencies);
        setComponentExecutionType(options, dependencies);
    }

    private void setExecutionOrder() throws BaseException {
        Map<String, Set<String>> dependencies = createDependencyList();
        int executionOrder = 0;
        List<String> toRemove = new ArrayList<>();
        List<String> orderedIriList = target.getComponents().stream()
                .map(component -> component.getIri())
                .sorted()
                .collect(Collectors.toList());
        while (!dependencies.isEmpty()) {
            //
            for (String componentIri : orderedIriList) {
                Set<String> componentDependencies =
                        dependencies.get(componentIri);
                if (componentDependencies == null) {
                    continue;
                }
                if (componentDependencies.isEmpty()) {
                    toRemove.add(componentIri);
                    target.getComponent(componentIri).setExecutionOrder(
                            ++executionOrder);
                }
            }
            //
            toRemove.forEach((item) -> {
                dependencies.remove(item);
            });
            dependencies.entrySet().forEach((entry) -> {
                entry.getValue().removeAll(toRemove);
            });
            //
            if (toRemove.isEmpty()) {
                throw new BaseException("Cycle detected.");
            }
            toRemove.clear();
        }
    }

    private Map<String, Set<String>> createDependencyList() {
        Map<String, Set<String>> dependencies = new HashMap<>();
        for (ExecutorComponent component : target.getComponents()) {
            dependencies.put(component.getIri(), new HashSet<>());
        }
        for (ExecutorConnection connection : target.getConnections()) {
            String targetComponent = connection.getTargetComponent();
            Set<String> ancestors = dependencies.get(targetComponent);
            ancestors.add(connection.getSourceComponent());
        }
        for (DesignerRunAfter connection : runAfter) {
            String targetComponent = connection.getTargetComponent();
            Set<String> ancestors = dependencies.get(targetComponent);
            ancestors.add(connection.getSourceComponent());
        }
        return dependencies;
    }

    private Map<String, Set<String>> filterDisabledComponents(
            Map<String, Set<String>> dependencies) {
        Map<String, Set<String>> filtered = new LinkedHashMap<>();
        for (ExecutorComponent component : target.getComponents()) {
            String iri = component.getIri();
            if (source.getComponent(iri).isDisabled()) {
                continue;
            }
            filtered.put(iri, dependencies.get(iri));
        }
        return filtered;
    }

    private void setComponentExecutionType(UnpackOptions options,
            Map<String, Set<String>> dependencies) throws BaseException {
        Set<String> componentsToExecute =
                getComponentsToExecute(options, dependencies);
        //
        for (ExecutorComponent component : target.getComponents()) {
            String iri = component.getIri();
            boolean enabled = !source.getComponent(iri).isDisabled();
            if (componentsToExecute.contains(component.getIri()) && enabled) {
                component.setExecutionType(LP_EXEC.TYPE_EXECUTE);
            } else {
                component.setExecutionType(LP_EXEC.TYPE_SKIP);
            }
        }
    }

    private Set<String> getComponentsToExecute(UnpackOptions options,
            Map<String, Set<String>> dependencies) {
        if (options.getRunToComponent() == null ||
                options.getRunToComponent().isEmpty()) {
            return dependencies.keySet();
        } else {
            // Use all dependencies.
            Set<String> componentsToExecute = new HashSet<>();
            List<String> toAdd = new ArrayList<>();
            toAdd.add(options.getRunToComponent());
            do {
                List<String> newToAdd = new ArrayList<>();
                for (String component : toAdd) {
                    if (componentsToExecute.contains(component)) {
                        continue;
                    }
                    newToAdd.addAll(dependencies.getOrDefault(
                            component, Collections.EMPTY_SET));
                    componentsToExecute.add(component);
                }
                toAdd = newToAdd;
            } while (!toAdd.isEmpty());
            return componentsToExecute;
        }
    }


}
