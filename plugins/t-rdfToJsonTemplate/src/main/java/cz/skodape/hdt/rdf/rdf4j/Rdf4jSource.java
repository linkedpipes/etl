package cz.skodape.hdt.rdf.rdf4j;

import cz.skodape.hdt.core.ArrayReference;
import cz.skodape.hdt.core.MemoryReferenceSource;
import cz.skodape.hdt.core.ObjectReference;
import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.PropertySource;
import cz.skodape.hdt.core.Reference;
import cz.skodape.hdt.core.ReferenceSource;
import cz.skodape.hdt.rdf.rdf4j.model.Rdf4jArray;
import cz.skodape.hdt.rdf.rdf4j.model.Rdf4jLiteral;
import cz.skodape.hdt.rdf.rdf4j.model.Rdf4jPrimitive;
import cz.skodape.hdt.rdf.rdf4j.model.Rdf4jReference;
import cz.skodape.hdt.rdf.rdf4j.model.Rdf4jResource;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Rdf4jSource implements PropertySource {

    protected static class ResourceInGraph {

        public final Resource resource;

        public final Resource graph;

        public ResourceInGraph(Statement statement) {
            this.resource = statement.getSubject();
            this.graph = statement.getContext();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            ResourceInGraph that = (ResourceInGraph) other;
            return resource.equals(that.resource)
                    && graph.equals(that.graph);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resource, graph);
        }
    }

    protected Rdf4jReference wrap(Resource graph, Value value) {
        if (value instanceof Resource) {
            return new Rdf4jResource(graph, (Resource) value);
        }
        if (value instanceof Literal) {
            return new Rdf4jLiteral(graph, (Literal) value);
        }
        throw new RuntimeException(
                "Can't convert rdf4j type: " + value.stringValue());
    }

    @Override
    public ReferenceSource source(Reference reference)
            throws OperationFailed {
        List<Reference> result = new ArrayList<>();
        Rdf4jReference rdfReference = asRdfReference(reference);
        if (rdfReference instanceof Rdf4jArray) {
            Rdf4jArray rdfArray = (Rdf4jArray) rdfReference;
            result.addAll(rdfArray.getReferences());
        } else {
            result.add(rdfReference);
        }
        return new MemoryReferenceSource<>(result);
    }

    protected Rdf4jReference asRdfReference(Reference reference)
            throws OperationFailed {
        if (reference instanceof Rdf4jReference) {
            return (Rdf4jReference) reference;
        }
        if (reference == null) {
            throw new OperationFailed("Reference is null.");
        }
        throw new OperationFailed(
                "Unsupported reference type: {}",
                reference.getClass());
    }

    @Override
    public ArrayReference property(ObjectReference reference, String property)
            throws OperationFailed {
        Rdf4jReference rdfReference = asRdfReference(reference);
        if (rdfReference instanceof Rdf4jResource) {
            return property((Rdf4jResource) rdfReference, property);
        }
        if (rdfReference instanceof Rdf4jLiteral) {
            return property((Rdf4jLiteral) rdfReference, property);
        }
        throw new OperationFailed(
                "Operation not supported for: "
                        + rdfReference.getClass().getName());
    }

    protected ArrayReference property(
            Rdf4jResource resourceReference, String property)
            throws OperationFailed {
        Resource graph = resourceReference.getGraph();
        if ("@value".equals(property)) {
            String id = resourceReference.getResource().stringValue();
            return new Rdf4jArray(graph, new Rdf4jPrimitive(graph, id));
        }
        List<Rdf4jReference> result =
                property(graph, resourceReference.getResource(), property)
                        .stream()
                        .map((value) -> this.wrap(graph, value))
                        .collect(Collectors.toList());
        return new Rdf4jArray(graph, result);
    }

    protected abstract List<Value> property(
            Resource graph, Resource resource, String property)
            throws OperationFailed;

    protected ArrayReference property(
            Rdf4jLiteral resourceReference, String property) {
        Resource graph = resourceReference.getGraph();
        Literal literal = resourceReference.getLiteral();
        List<Rdf4jReference> result = new ArrayList<>(1);
        switch (property) {
            case "@type":
                String type = literal.getDatatype().stringValue();
                result.add(new Rdf4jPrimitive(graph, type));
                break;
            case "@value":
                String value = literal.stringValue();
                result.add(new Rdf4jPrimitive(graph, value));
                break;
            case "@language":
                literal.getLanguage().ifPresent(lang -> {
                    result.add(new Rdf4jPrimitive(graph, lang));
                });
                break;
            default:
                break;
        }
        return new Rdf4jArray(graph, result);
    }

    @Override
    public ArrayReference reverseProperty(Reference reference, String property)
            throws OperationFailed {
        throw new OperationFailed("Operation not supported.");
    }

}
