package cz.skodape.hdt.rdf.rdf4j.model;

import cz.skodape.hdt.core.PrimitiveReference;
import org.eclipse.rdf4j.model.Resource;

/**
 * Represents primitive value as a string.
 */
public class Rdf4jPrimitive implements Rdf4jReference, PrimitiveReference {

    private final Resource graph;

    private final String value;

    public Rdf4jPrimitive(Resource graph, String value) {
        this.graph = graph;
        this.value = value;
    }

    public Resource getGraph() {
        return graph;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String asDebugString() {
        return "RdfPrimitive '" + value + "'";
    }

    @Override
    public boolean isObjectReference() {
        return false;
    }

    @Override
    public boolean isArrayReference() {
        return false;
    }

    @Override
    public boolean isPrimitiveReference() {
        return true;
    }

}
