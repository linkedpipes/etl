package com.linkedpipes.etl.rdf.utils.entity;

import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.eclipse.rdf4j.RDF4JConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Can be used to merge RDF entities based on the configuration.
 */
public class EntityMerger {

    /**
     * Describe reference to the RDF entity.
     */
    public static class Reference {

        private final String resource;

        private final String graph;

        private final RdfSource source;

        public Reference(String resource, String graph,
                RdfSource source) {
            this.resource = resource;
            this.graph = graph;
            this.source = source;
        }

        public String getResource() {
            return resource;
        }

        public String getGraph() {
            return graph;
        }

        public RdfSource getSource() {
            return source;
        }
    }

    public enum MergeType {
        /**
         * Value is added to current value list.
         */
        LOAD,
        /**
         * Value is skipped.
         */
        SKIP,
        /**
         * Only for entities. Merge multiple objects.
         */
        MERGE
    }

    /**
     * Control instance for entity of given type.
     *
     * The control should be loaded before use (by the factory method)
     * and thus should have prior knowledge which field should be loaded.
     */
    public interface Control {

        /**
         * Load entities into the control.
         *
         * @param references
         */
        void init(List<Reference> references) throws RdfUtilsException;

        /**
         * Called at start of every reference.
         *
         * @param resource This values have been given
         * @param graph
         */
        void onReference(String resource, String graph)
                throws RdfUtilsException;

        /**
         * @param property
         * @return Decision what to do with the property.
         */
        MergeType onProperty(String property) throws RdfUtilsException;

    }

    @FunctionalInterface
    public interface ControlFactory {

        /**
         * @param type
         * @return Control for object of given type.
         */
        Control create(String type) throws RdfUtilsException;

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(EntityMerger.class);

    private EntityMerger() {

    }

    /**
     * The first reference is used to get the {@link Control} entity,
     * that is then used for all followup references.
     *
     * @param references References of resources to merge.
     * @param descriptorFactory Factory for object descriptors.
     * @param iri Final resource for merged entity.
     * @param writer Writer to write the output.
     * @param type Type used to transfer values.
     */
    @SuppressWarnings("unchecked")
    public static <ValueType> void merge(List<Reference> references,
            ControlFactory descriptorFactory, String iri,
            RdfSource.TypedTripleWriter<ValueType> writer,
            Class<ValueType> type) throws RdfUtilsException {
        if (references.isEmpty()) {
            throw new RDF4JConfigException("No references to merge!");
        }
        // Get descriptors.
        final Control descriptor = getDescriptor(references.get(0),
                descriptorFactory);
        if (descriptor == null) {
            throw new RdfUtilsException("Can't get descriptor for: {} in {}",
                    references.get(0).resource,
                    references.get(0).graph);
        }
        descriptor.init(references);
        final RdfSource.ValueToString converter =
                references.get(0).source.toStringConverter(type);
        final RdfSource.ValueInfo<ValueType> valueInfo =
                references.get(0).source.getValueInfo();
        if (descriptor == null) {
            throw new RdfUtilsException("Can't get descriptor for: {}",
                    references.get(0).resource);
        }
        //
        final Map<String, List<ValueType>> values = new HashMap<>();
        final Map<String, List<Reference>> entities = new HashMap<>();
        final Map<String, List<Reference>> entitiesToMerge = new HashMap<>();
        // Iterate over object and merge.
        for (final Reference ref : references) {
            descriptor.onReference(ref.resource, ref.graph);
            ref.source.triples(ref.resource, ref.graph, type, (s, p, o) -> {
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
                            if (!entities.containsKey(p)) {
                                entities.put(p, new ArrayList<>(4));
                            }
                            entities.get(p).add(new Reference(
                                    converter.asString(o),
                                    ref.graph,
                                    ref.source));
                        }
                        break;
                    case MERGE:
                        // Check for the reference.
                        if (!valueInfo.isIri(value)) {
                            LOG.error("Invalid reference ignored {} {} {} : {}",
                                    s, p, value, ref.graph);
                        }
                        if (!entitiesToMerge.containsKey(p)) {
                            entitiesToMerge.put(p, new ArrayList<>(4));
                        }
                        entitiesToMerge.get(p).add(new Reference(
                                converter.asString(o),
                                ref.graph,
                                ref.source));
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
        for (Map.Entry<String, List<Reference>> entry : entities.entrySet()) {
            for (Reference value : entry.getValue()) {
                RdfUtils.copyEntityRecursive(value.resource,
                        value.graph, value.source, writer, type);
            }
        }
        writer.submit();
        // Merge referenced entities.
        int counter = 0;
        final Map<String, String> entryIris = new HashMap<>();
        for (Map.Entry<String, List<Reference>> entry
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
    private static Control getDescriptor(Reference reference,
            ControlFactory descriptorFactory) throws RdfUtilsException {
        final Class<?> type = reference.source.getDefaultType();
        final RdfSource.ValueToString converter =
                reference.source.toStringConverter(type);
        final List<String> types = new LinkedList<>();
        reference.source.triples(reference.resource, reference.graph, type,
                (s, p, i) -> {
                    if (RDF.TYPE.equals(p)) {
                        types.add(converter.asString(i));
                    }
                });
        //
        for (String item : types) {
            final Control descriptor = descriptorFactory.create(item);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }


}
