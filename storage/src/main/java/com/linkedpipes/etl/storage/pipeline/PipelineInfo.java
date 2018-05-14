package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains information about pipeline that are loaded from the
 * pipeline RDF definition.
 */
public class PipelineInfo implements PojoLoader.Loadable {

    /**
     * IRI of the pipeline.
     */
    private String iri;

    private int version = Pipeline.VERSION_NUMBER;

    /**
     * Labels.
     */
    private final List<Value> labels = new ArrayList<>(2);

    private final List<Value> tags = new ArrayList<>(4);

    public PipelineInfo() {
    }

    public String getIri() {
        return iri;
    }

    public int getVersion() {
        return version;
    }

    public List<Value> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    public List<Value> getTags() {
        return tags;
    }

    @Override
    public void loadIri(String iri) {
        this.iri = iri;
    }

    @Override
    public PojoLoader.Loadable load(String predicate, Value value)
            throws PojoLoader.CantLoadException {
        switch (predicate) {
            case "http://etl.linkedpipes.com/ontology/version":
                version = ((Literal) value).intValue();
                break;
            case "http://www.w3.org/2004/02/skos/core#prefLabel":
                labels.add(value);
                break;
            case "http://etl.linkedpipes.com/ontology/tag":
                tags.add(value);
                break;
            default:
                break;
        }
        return null;
    }

}
