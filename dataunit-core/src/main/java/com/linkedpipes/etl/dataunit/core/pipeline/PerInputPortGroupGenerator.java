package com.linkedpipes.etl.dataunit.core.pipeline;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Use breadth-first search.
 */
class PerInputPortGroupGenerator implements PortGroupGenerator {

    private Map<String, Port> ports;

    private Integer groupCounter = 0;

    public void generateAndAddPortGroups(Map<String, Port> ports) {
        this.ports = ports;

        for (Port port : ports.values()) {
            if (port.getGroup() != null) {
                continue;
            }
            Collection<Port> group = discoverPortGroup(port);
            assignNewGroup(group);
        }
    }

    private Collection<Port> discoverPortGroup(Port port) {
        Set<Port> visited = new HashSet<>();
        Stack<Port> toVisit = new Stack<>();
        toVisit.add(port);
        while (!toVisit.isEmpty()) {
            Port portToVisit = toVisit.pop();
            visited.add(portToVisit);
            toVisit.addAll(filterVisited(visited, getSources(portToVisit)));
            toVisit.addAll(filterVisited(visited, getTargets(portToVisit)));
        }
        return visited;
    }

    private List<Port> filterVisited(Set<Port> visited, List<Port> toFilter) {
        return toFilter.stream()
                .filter(port -> !visited.contains(port))
                .collect(Collectors.toList());
    }

    private List<Port> getSources(Port targetPort) {
        return targetPort.getSources().stream()
                .map(iri -> ports.get(iri)).collect(Collectors.toList());
    }

    private List<Port> getTargets(Port sourcePort) {
        String sourcePortIri = sourcePort.getIri();
        return ports.values().stream()
                .filter(port -> port.getSources().contains(sourcePortIri))
                .collect(Collectors.toList());
    }

    private void assignNewGroup(Collection<Port> ports) {
        Integer group = ++groupCounter;
        for (Port port : ports) {
            port.setGroup(group);
        }
    }


}
