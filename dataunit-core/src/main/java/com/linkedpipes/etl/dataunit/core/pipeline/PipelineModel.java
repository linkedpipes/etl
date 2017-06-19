package com.linkedpipes.etl.dataunit.core.pipeline;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PipelineModel {

    private final Map<String, Port> ports;

    public PipelineModel(Map<String, Port> ports) {
        this.ports = ports;
    }

    public List<String> getPortTypes(String portIri)
            throws MissingPortException {
        return getPort(portIri).getTypes();
    }

    public List<String> getPortSources(String portIri)
            throws MissingPortException {
        return getPort(portIri).getSources();
    }

    public Integer getPortGroup(String portIri) throws MissingPortException {
        return getPort(portIri).getGroup();
    }

    public List<String> getIrisOfPortsInGroup(Integer group) {
        return ports.values().stream()
                .filter(port -> port.getGroup() == group)
                .map(port -> port.getIri())
                .collect(Collectors.toList());
    }

    private Port getPort(String portIri) throws MissingPortException {
        Port port = ports.get(portIri);
        if (port == null) {
            throw new MissingPortException(portIri);
        } else {
            return port;
        }
    }

}
