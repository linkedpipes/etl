package com.linkedpipes.etl.storage.component.pipeline;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe update operation that can be performed on the pipeline.
 *
 * @author Petr Škoda
 */
class UpdateOptions implements PojoLoader.Loadable {

    public static final IRI TYPE;

    static {
        TYPE = SimpleValueFactory.getInstance().createIRI(
                "http://linkedpipes.com/ontology/UpdateOptions");
    }

    /**
     * Labels that should be used as pipeline names, override any
     * other pipeline name.
     */
    private final List<Value> labels = new ArrayList<>(2);

    /**
     * If true import data from the data stream. This cause
     * regeneration of LinkedPipes resources IRIs.
     */
    private boolean importStream = false;

    public List<Value> getLabels() {
        return labels;
    }

    public boolean isImportStream() {
        return importStream;
    }

    @Override
    public PojoLoader.Loadable load(String predicate, Value value)
            throws PojoLoader.CantLoadException {
        switch (predicate) {
            case "http://etl.linkedpipes.com/ontology/import":
                importStream = ((Literal) value).booleanValue();
                break;
            case "http://www.w3.org/2004/02/skos/core#prefLabel":
                labels.add(value);
                break;
        }
        return null;
    }

}
