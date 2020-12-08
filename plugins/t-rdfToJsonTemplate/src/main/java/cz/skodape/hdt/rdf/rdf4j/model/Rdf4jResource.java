package cz.skodape.hdt.rdf.rdf4j.model;

import cz.skodape.hdt.core.ObjectReference;
import org.eclipse.rdf4j.model.Resource;

/**
 * Represent RDF resource i.e. blank or named node.
 */
public class Rdf4jResource implements Rdf4jReference, ObjectReference {

    private final Resource graph;

    private final Resource resource;

    public Rdf4jResource(Resource graph, Resource resource) {
        this.graph = graph;
        this.resource = resource;
    }

    public Resource getResource() {
        return this.resource;
    }

    public Resource getGraph() {
        return graph;
    }

    @Override
    public String asDebugString() {
        return resource.toString();
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
