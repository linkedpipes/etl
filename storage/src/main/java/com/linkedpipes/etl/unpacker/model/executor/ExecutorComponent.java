package com.linkedpipes.etl.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.model.vocabulary.SKOS;

import java.util.LinkedList;
import java.util.List;

public class ExecutorComponent {

    private String iri;

    // TODO Remove and load from pipeline where needed.
    private String label;

    private List<String> types = new LinkedList<>();

    private List<ExecutorPort> ports = new LinkedList<>();

    private List<String> requirements = new LinkedList<>();

    private String jar;

    private String template;

    private String configGraph;

    private String configDescriptionGraph;

    private String executionType;

    private Integer executionOrder = -1;

    /**
     * Execution that is component is mapped from.
     */
    private String execution;

    public void write(StatementsBuilder writer) {
        writeSharedInfo(writer);
        switch (executionType) {
            case LP_EXEC.TYPE_EXECUTE:
                writeExecute(writer);
                break;
            case LP_EXEC.TYPE_MAPPED:
                writeMapped(writer);
                break;
            case LP_EXEC.TYPE_SKIP:
                // There are no additional information required.
                break;
            default:
                break;
        }
    }

    private void writeSharedInfo(StatementsBuilder builder) {
        for (String type : types) {
            builder.addType(iri, type);
        }
        builder.addType(iri, LP_PIPELINE.COMPONENT);
        builder.add(iri, LP_EXEC.HAS_ORDER_EXEC, executionOrder);
        builder.addIri(iri, LP_PIPELINE.HAS_JAR_URL, jar);
        builder.addIri(iri, LP_PIPELINE.HAS_TEMPLATE, template);
        builder.addIri(iri, LP_EXEC.HAS_EXECUTION_TYPE, executionType);
        builder.add(iri, SKOS.PREF_LABEL, label);
    }

    private void writeExecute(StatementsBuilder builder) {
        for (String requirement : requirements) {
            builder.addIri(iri, LP_PIPELINE.HAS_REQUIREMENT, requirement);
        }
        builder.addIri(iri, LP_PIPELINE.HAS_CONFIGURATION_GRAPH,
                configGraph);
        builder.addIri(iri, LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION,
                configDescriptionGraph);
        for (ExecutorPort port : ports) {
            builder.addIri(iri, LP_PIPELINE.HAS_DATA_UNIT, port.getIri());
            port.write(builder);
        }
        if (execution != null) {
            builder.addIri(iri, LP_EXEC.HAS_EXECUTION, execution);
        }
    }

    private void writeMapped(StatementsBuilder builder) {
        builder.addIri(iri, LP_EXEC.HAS_EXECUTION, execution);
        for (ExecutorPort port : ports) {
            builder.addIri(iri, LP_PIPELINE.HAS_DATA_UNIT, port.getIri());
            port.write(builder);
        }
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void addPort(ExecutorPort port) {
        ports.add(port);
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getConfigGraph() {
        return configGraph;
    }

    public void setConfigGraph(String configGraph) {
        this.configGraph = configGraph;
    }

    public String getConfigDescriptionGraph() {
        return configDescriptionGraph;
    }

    public void setConfigDescriptionGraph(String configDescriptionGraph) {
        this.configDescriptionGraph = configDescriptionGraph;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public void setExecutionOrder(Integer executionOrder) {
        this.executionOrder = executionOrder;
    }

    public List<ExecutorPort> getPorts() {
        return ports;
    }

    public String getExecutionType() {
        return executionType;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

}
