package cz.skodape.hdt.rdf.rdf4j.model;

import cz.skodape.hdt.core.ObjectReference;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

public class Rdf4jLiteral implements Rdf4jReference, ObjectReference {

    private final Resource graph;

    private final Literal literal;

    public Rdf4jLiteral(Resource graph, Literal node) {
        this.graph = graph;
        this.literal = node;
    }

    public Literal getLiteral() {
        return literal;
    }

    public Resource getGraph() {
        return graph;
    }

    @Override
    public String asDebugString() {
        return "RdfLiteral: '" + this.literal.stringValue() + "'";
    }

    @Override
    public boolean isObjectReference() {
        return true;
    }

    @Override
    public boolean isArrayReference() {
        return false;
    }

    @Override
    public boolean isPrimitiveReference() {
        return false;
    }

}
