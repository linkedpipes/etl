package com.linkedpipes.etl.executor.rdf.entity;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.*;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Can be used to merge RDF entities based on the configuration.
 */
public class EntityMerger {

    private static final Logger LOG =
            LoggerFactory.getLogger(EntityMerger.class);

    private final MergeControlFactory descriptorFactory;

    private MergeControl descriptor;

    private Map<String, List<RdfValue>> values;

    private Map<String, List<EntityReference>> entitiesToCopy;

    private Map<String, List<EntityReference>> entitiesToMerge;

    private EntityReference reference;

    public EntityMerger(MergeControlFactory descriptorFactory) {
        this.descriptorFactory = descriptorFactory;
    }

    public void merge(
            List<EntityReference> references,
            String outputIri,
            TripleWriter writer
    ) throws RdfUtilsException {
        if (references.isEmpty()) {
            throw new RdfUtilsException("Nothing to merge!");
        }

        descriptor = getDescriptor(references.get(0));
        descriptor.init(references);

        initializeLists();

        for (EntityReference ref : references) {
            descriptor.onReference(ref.getResource(), ref.getGraph());
            loadEntity(ref);
        }

        writeResult(outputIri, writer);
    }

    private MergeControl getDescriptor(EntityReference reference)
            throws RdfUtilsException {
        List<String> types = getTypes(reference);
        for (String item : types) {
            final MergeControl descriptor = descriptorFactory.create(item);
            if (descriptor != null) {
                return descriptor;
            }
        }
        throw new RdfUtilsException("Can't get descriptor for: {} in {}",
                reference.getResource(), reference.getGraph());
    }

    private List<String> getTypes(EntityReference reference)
            throws RdfUtilsException {
        List<String> types = new LinkedList<>();
        reference.getSource().triples(
                reference.getResource(),
                reference.getGraph(),
                triple -> {
                    if (RDF.TYPE.equals(triple.getPredicate())) {
                        types.add(triple.getObject().asString());
                    }
                });
        return types;
    }

    private void initializeLists() {
        values = new HashMap<>();
        entitiesToCopy = new HashMap<>();
        entitiesToMerge = new HashMap<>();
    }

    private void loadEntity(EntityReference reference)
            throws RdfUtilsException {
        this.reference = reference;
        reference.getSource().triples(
                reference.getResource(),
                reference.getGraph(),
                triple -> {
                    handleStatement(triple);
                });
    }

    private void handleStatement(RdfTriple triple)
            throws RdfUtilsException {
        switch (descriptor.onProperty(triple.getPredicate())) {
            case LOAD:
                loadStatement(triple);
                break;
            case MERGE:
                mergeStatement(triple);
                break;
            case SKIP:
            default:
                break;
        }
    }

    private void loadStatement(RdfTriple triple) {
        String predicate = triple.getPredicate();
        if (!values.containsKey(predicate)) {
            values.put(predicate, new ArrayList<>(4));
        }
        values.get(predicate).add(triple.getObject());
        if (triple.getObject().isIri()) {
            addEntityToCopy(triple);
        }
    }

    private void addEntityToCopy(RdfTriple triple) {
        String predicate = triple.getPredicate();
        if (!entitiesToCopy.containsKey(predicate)) {
            entitiesToCopy.put(predicate, new ArrayList<>(4));
        }
        entitiesToCopy.get(predicate).add(new EntityReference(
                triple.getObject().asString(),
                reference.getGraph(),
                reference.getSource()));
    }

    private void mergeStatement(RdfTriple triple) {
        if (triple.getObject().isIri()) {
            addEntityToMerge(triple);
        } else {
            LOG.error("Invalid reference ignored {} {} {} : {}",
                    triple.getSubject(),
                    triple.getPredicate(),
                    triple.getObject().asString(),
                    reference.getGraph());
        }
    }

    private void addEntityToMerge(RdfTriple triple) {
        String predicate = triple.getPredicate();
        if (!entitiesToMerge.containsKey(predicate)) {
            entitiesToMerge.put(predicate, new ArrayList<>(4));
        }
        entitiesToMerge.get(predicate).add(new EntityReference(
                triple.getObject().asString(),
                reference.getGraph(),
                reference.getSource()));
    }

    private void writeResult(String outputIri, TripleWriter writer)
            throws RdfUtilsException {
        for (Map.Entry<String, List<RdfValue>> entry : values.entrySet()) {
            for (RdfValue value : entry.getValue()) {
                writer.add(outputIri, entry.getKey(), value);
            }
        }
        for (List<EntityReference> references : entitiesToCopy.values()) {
            for (EntityReference value : references) {
                copyEntityRecursive(value.getSource(),
                        value.getResource(), value.getGraph(), writer);
            }
        }
        int counter = 0;
        for (Map.Entry<String, List<EntityReference>> entry
                : entitiesToMerge.entrySet()) {
            String iri = outputIri + "/" + ++counter;
            writer.iri(outputIri, entry.getKey(), iri);
            merge(entry.getValue(), iri, writer);
        }
        writer.flush();
    }

    private static void copyEntityRecursive(RdfSource source, String resource,
            String graph, TripleWriter writer) throws RdfUtilsException {
        source.triples(resource, graph, (triple) -> {
            writer.add(triple);
            if (triple.getObject().isIri()) {
                copyEntityRecursive(source, triple.getObject().asString(),
                        graph, writer);
            }
        });
    }

}
