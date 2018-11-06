package com.linkedpipes.etl.storage.pipeline.transformation;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains settings for pipeline operations.
 */
class ImportOptions implements PojoLoader.Loadable {

    public static final IRI TYPE;

    static {
        TYPE = SimpleValueFactory.getInstance().createIRI(
                "http://linkedpipes.com/ontology/UpdateOptions");
    }

    /**
     * Labels that should be used as pipeline labels, override labels
     * from pipeline definition.
     */
    private final List<Literal> labels = new ArrayList<>(2);

    /**
     * If true pipeline is local and there is no need to update anything.
     * If false the templates alignment need to be done.
     */
    private boolean local = true;

    private boolean importTemplates = false;

    private boolean updateTemplates = false;

    public List<Literal> getLabels() {
        return labels;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isImportTemplates() {
        return importTemplates;
    }

    public boolean isUpdateTemplates() {
        return updateTemplates;
    }

    @Override
    public PojoLoader.Loadable load(String predicate, Value value) {
        switch (predicate) {
            case "http://etl.linkedpipes.com/ontology/local":
                if (value instanceof Literal) {
                    local = ((Literal) value).booleanValue();
                }
                break;
            case "http://www.w3.org/2004/02/skos/core#prefLabel":
                if (value instanceof Literal) {
                    labels.add((Literal) value);
                }
                break;
            case "http://etl.linkedpipes.com/ontology/importTemplates":
                if (value instanceof Literal) {
                    importTemplates = ((Literal) value).booleanValue();
                }
                break;
            case "http://etl.linkedpipes.com/ontology/updateTemplates":
                if (value instanceof Literal) {
                    updateTemplates = ((Literal) value).booleanValue();
                }
                break;
            default:
                break;
        }
        return null;
    }

}
