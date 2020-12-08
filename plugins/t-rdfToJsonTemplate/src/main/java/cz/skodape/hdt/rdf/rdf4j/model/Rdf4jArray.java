package cz.skodape.hdt.rdf.rdf4j.model;

import cz.skodape.hdt.core.ArrayReference;
import cz.skodape.hdt.core.Reference;
import org.eclipse.rdf4j.model.Resource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an array of {@link Rdf4jResource}s. Can be used to represent
 * values of a resource property, inverse property or multiple.
 */
public class Rdf4jArray implements Rdf4jReference, ArrayReference {

    private final Resource graph;

    private final List<Rdf4jReference> references;

    public Rdf4jArray(Resource graph, Rdf4jReference reference) {
        this.graph = graph;
        this.references = Collections.singletonList(reference);
    }

    public Rdf4jArray(Resource graph, List<Rdf4jReference> references) {
        this.graph = graph;
        this.references = references;
    }

    public Resource getGraph() {
        return graph;
    }

    public List<Rdf4jReference> getReferences() {
        return this.references;
    }

    @Override
    public String asDebugString() {
        return "RdfArray\n  - "
                + this.references.stream()
                .map(Reference::asDebugString)
                .collect(Collectors.joining("\n  - "));
    }

    @Override
    public boolean isObjectReference() {
        return false;
    }

    @Override
    public boolean isArrayReference() {
        return true;
    }

    @Override
    public boolean isPrimitiveReference() {
        return false;
    }

}
