package com.linkedpipes.etl.storage.unpacker.model.designer;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.RDF;
import com.linkedpipes.etl.model.vocabulary.SKOS;
import com.linkedpipes.etl.storage.unpacker.rdf.Loadable;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DesignerComponent implements Loadable {

    public static final String TYPE = LP_PIPELINE.COMPONENT;

    private String iri;

    private List<String> types = new LinkedList<>();

    /**
     * Starting from instance all configuration graphs used by this component.
     */
    private List<String> configurationGraphs = new ArrayList<>();

    private String template;

    private String label;

    private boolean disabled = false;

    public DesignerComponent() {
    }

    public DesignerComponent(DesignerComponent component) {
        this.iri = component.iri;
        this.types = new ArrayList<>(component.getTypes().size());
        this.types.addAll(component.getTypes());
        this.configurationGraphs.addAll(component.getConfigurationGraphs());
        this.template = component.template;
        this.label = component.label;
        this.disabled = component.disabled;

    }

    @Override
    public void resource(String resource)  {
        iri = resource;
    }

    @Override
    public Loadable load(String predicate, Value value) {
        switch (predicate) {
            case RDF.TYPE:
                types.add(value.stringValue());
                return null;
            case LP_PIPELINE.HAS_CONFIGURATION_GRAPH:
                configurationGraphs.add(value.stringValue());
                return null;
            case LP_PIPELINE.HAS_TEMPLATE:
                template = value.stringValue();
                return null;
            case SKOS.PREF_LABEL:
                label = value.stringValue();
                return null;
            case LP_PIPELINE.HAS_DISABLED:
                if (value instanceof Literal literal) {
                    disabled = literal.booleanValue();
                }
                return null;
            default:
                return null;
        }
    }

    public String getIri() {
        return iri;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getConfigurationGraphs() {
        return configurationGraphs;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getLabel() {
        return label;
    }

    public boolean isDisabled() {
        return disabled;
    }

}
