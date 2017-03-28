package com.linkedpipes.etl.rdf.utils.entity;

import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
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

    private EntityMerger() {

    }

    /**
     * @param references References of resources to merge.
     * @param descriptorFactory Factory for object descriptors.
     * @param iri Final resource for merged entity.
     * @param writer Writer to write the output.
     * @param type Type used to transfer values.
     */
    @SuppressWarnings("unchecked")
    public static <ValueType> void merge(List<EntityReference> references,
            EntityControlFactory descriptorFactory, String iri,
            RdfSource.TypedTripleWriter<ValueType> writer,
            Class<ValueType> type) throws RdfUtilsException {
        if (references.isEmpty()) {
            throw new RdfUtilsException("No references to merge!");
        }
        // Get descriptors.
        final EntityControl descriptor = getDescriptor(references.get(0),
                descriptorFactory);
        if (descriptor == null) {
            throw new RdfUtilsException("Can't get descriptor for: {} in {}",
                    references.get(0).getResource(),
                    references.get(0).getGraph());
        }
        descriptor.init(references);
        final RdfSource.ValueToString converter =
                references.get(0).getSource().toStringConverter(type);
        final RdfSource.ValueInfo<ValueType> valueInfo =
                references.get(0).getSource().getValueInfo();
        if (descriptor == null) {
            throw new RdfUtilsException("Can't get descriptor for: {}",
                    references.get(0).getResource());
        }
        //
        final Map<String, List<ValueType>> values = new HashMap<>();
        final Map<String, List<EntityReference>> entitiesToCopy =
                new HashMap<>();
        final Map<String, List<EntityReference>> entitiesToMerge =
                new HashMap<>();
        // Iterate over object and merge.
        for (final EntityReference ref : references) {
            descriptor.onReference(ref.getResource(), ref.getGraph());
            ref.getSource().triples(ref.getResource(), ref.getGraph(), type,
                    (s, p, o) -> {
                        final ValueType value = (ValueType) o;
                        switch (descriptor.onProperty(p)) {
                            case LOAD:
                                // In every case we add as a value.
                                if (!values.containsKey(p)) {
                                    values.put(p, new ArrayList<>(4));
                                }
                                values.get(p).add(value);
                                // If reference add info about reference.
                                if (valueInfo.isIri(value)) {
                                    if (!entitiesToCopy.containsKey(p)) {
                                        entitiesToCopy
                                                .put(p, new ArrayList<>(4));
                                    }
                                    entitiesToCopy.get(p)
                                            .add(new EntityReference(
                                                    converter.asString(o),
                                                    ref.getGraph(),
                                                    ref.getSource()));
                                }
                                break;
                            case MERGE:
                                // Check for the reference.
                                if (!valueInfo.isIri(value)) {
                                    LOG.error(
                                            "Invalid reference ignored {} {} {} : {}",
                                            s, p, value, ref.getGraph());
                                }
                                if (!entitiesToMerge.containsKey(p)) {
                                    entitiesToMerge.put(p, new ArrayList<>(4));
                                }
                                entitiesToMerge.get(p).add(new EntityReference(
                                        converter.asString(o),
                                        ref.getGraph(),
                                        ref.getSource()));
                                break;
                            case SKIP:
                            default:
                                break;
                        }
                    });
        }
        // Write entity and referenced entities.
        for (Map.Entry<String, List<ValueType>> entry : values.entrySet()) {
            for (ValueType value : entry.getValue()) {
                writer.add(iri, entry.getKey(), value);
            }
        }
        for (Map.Entry<String, List<EntityReference>> entry : entitiesToCopy
                .entrySet()) {
            for (EntityReference value : entry.getValue()) {
                RdfUtils.copyEntityRecursive(value.getResource(),
                        value.getGraph(), value.getSource(), writer, type);
            }
        }
        writer.submit();
        // Merge referenced entities.
        int counter = 0;
        final Map<String, String> entryIris = new HashMap<>();
        for (Map.Entry<String, List<EntityReference>> entry
                : entitiesToMerge.entrySet()) {
            final String entryIri = iri + "/" + ++counter;
            entryIris.put(entry.getKey(), entryIri);
            // Merge.
            merge(entry.getValue(), descriptorFactory, entryIri, writer, type);
        }
        for (Map.Entry<String, String> entry : entryIris.entrySet()) {
            writer.iri(iri, entry.getKey(), entry.getValue());
        }
        writer.submit();

    }

    /**
     * @param reference
     * @param descriptorFactory
     * @return Descriptor for given reference or null.
     */
    private static EntityControl getDescriptor(EntityReference reference,
            EntityControlFactory descriptorFactory) throws RdfUtilsException {
        final Class<?> type = reference.getSource().getDefaultType();
        final RdfSource.ValueToString converter =
                reference.getSource().toStringConverter(type);
        final List<String> types = new LinkedList<>();
        reference.getSource().triples(reference.getResource(),
                reference.getGraph(), type,
                (s, p, i) -> {
                    if (RDF.TYPE.equals(p)) {
                        types.add(converter.asString(i));
                    }
                });
        //
        for (String item : types) {
            final EntityControl descriptor = descriptorFactory.create(item);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }


}
