package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.entity.EntityMerger;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manage configurations for components.
 *
 * For each component the effective configuration is computed
 * before the component execution.
 */
class Configuration {

    private enum Status {
        FORCE,
        FORCED,
        INHERIT,
        INHERIT_AND_FORCE,
        MERGE,
        NONE
    }

    private static class DefaultControl implements EntityMerger.Control {

        private final RdfSource source;

        private final String graph;

        private final String type;

        /**
         * For pair definitionGraph-resource store list of properties and their
         * merge type.
         */
        private final Map<String, Map<String, EntityMerger.MergeType>>
                control = new HashMap<>();

        /**
         * Store reference to current object as definitionGraph-resource.
         */
        private String currentKey;

        public DefaultControl(RdfSource source, String graph, String type) {
            this.source = source;
            this.graph = graph;
            this.type = type;
        }

        @Override
        public void init(List<EntityMerger.Reference> references)
                throws RdfUtilsException {
            // Prepare control objects.
            control.clear();
            for (EntityMerger.Reference ref : references) {
                final String key = ref.getGraph() + "-" + ref.getResource();
                control.put(key, new HashMap<>());
            }
            // Use first reference to load list of complex properties.
            final Collection<String> complexPredicates =
                    loadComplex(references.get(0));
            for (EntityMerger.Reference ref : references) {
                final String key = ref.getGraph() + "-" + ref.getResource();
                for (String predicate : complexPredicates) {
                    control.get(key).put(predicate,
                            EntityMerger.MergeType.MERGE);
                }
            }
            // Load controls from entities.
            final List<Map<String, Status>> refControls =
                    new ArrayList<>(references.size());
            for (EntityMerger.Reference ref : references) {
                // Load the controls from the RDF entity.
                refControls.add(loadControl(ref));
            }
            // Build predicate list.
            final Set<String> predicates = new HashSet<>();
            for (Map<String, Status> map : refControls) {
                predicates.addAll(map.keySet());
            }
            // For each predicate determine status (load/skip/merge).
            for (String predicate : predicates) {
                // Search for indexes for:
                int forceFrom = -1;
                int lastLoad = -1;
                final List<Integer> mergeFrom =
                        new ArrayList<>(references.size());
                for (int i = 0; i < references.size(); ++i) {
                    final Status status = refControls.get(i).get(predicate);
                    // In every other case clear merge list.
                    mergeFrom.clear();
                    if (status == Status.FORCE) {
                        forceFrom = i;
                        break;
                    } else if (status == Status.INHERIT_AND_FORCE) {
                        forceFrom = i - 1;
                        break;
                    } else if (status == Status.NONE) {
                        lastLoad = i;
                    }
                }
                // Initialize skip all.
                final Set<Integer> loadFrom = new HashSet<>();
                if (forceFrom != -1) {
                    loadFrom.add(forceFrom);
                } else if (lastLoad != -1) {
                    loadFrom.add(lastLoad);
                } else {
                    LOG.info("Predicate: {}", predicate);
                    for (EntityMerger.Reference ref : references) {
                        LOG.info("\tref: {} {}", ref.getResource(),
                                ref.getGraph());
                    }
                    throw new RdfUtilsException("Can't determine loading " +
                            "sequence: {} {}", forceFrom, lastLoad);
                }
                //
                int counter = 0;
                for (EntityMerger.Reference ref : references) {
                    final String key = ref.getGraph() + "-" + ref.getResource();
                    if (loadFrom.contains(counter)) {
                        control.get(key).put(predicate,
                                EntityMerger.MergeType.LOAD);
                    } else {
                        control.get(key).put(predicate,
                                EntityMerger.MergeType.SKIP);
                    }
                    ++counter;
                }
                continue;
            }
            return;
        }

        @Override
        public void onReference(String resource, String graph)
                throws RdfUtilsException {
            currentKey = graph + "-" + resource;
            if (!control.containsKey(currentKey)) {
                throw new RdfUtilsException(
                        "Missing reference to resource: {} {}",
                        resource, graph);
            }
        }

        @Override
        public EntityMerger.MergeType onProperty(String property)
                throws RdfUtilsException {
            if (RDF.TYPE.equals(property)) {
                // Always copy type.
                return EntityMerger.MergeType.LOAD;
            }
            //
            final EntityMerger.MergeType type =
                    control.get(currentKey).get(property);
            if (type == null) {
                // Skip all others (like control).
                return EntityMerger.MergeType.SKIP;
            }
            return type;
        }

        /**
         * @param reference
         * @return List of predicates with complex type.
         */
        private Collection<String> loadComplex(
                EntityMerger.Reference reference) throws RdfUtilsException {
            final String query = "SELECT ?property WHERE { " +
                    "GRAPH <" + reference.getGraph() + "> {\n" +
                    "  <" + reference.getResource() + "> a ?type ;\n" +
                    "  ?ctrl ?control .\n" +
                    "}\n" +
                    " GRAPH ?g {" +
                    "  ?entity a <" + LP_OBJECTS.DESCRIPTION + "> ;\n" +
                    "   <" + LP_OBJECTS.HAS_DESCRIBE + "> ?type ;\n" +
                    "   <" + LP_OBJECTS.HAS_MEMBER + "> ?member .\n" +
                    "\n" +
                    "  ?member <" + LP_OBJECTS.HAS_PROPERTY +
                    "> ?property ;\n" +
                    "    <" + LP_OBJECTS.IS_COMPLEX + "> true . \n" +
                    "} }";
            final List<Map<String, String>> queryResult = RdfUtils.sparqlSelect(
                    reference.getSource(), query);
            final List<String> result = new ArrayList<>(queryResult.size());
            for (Map<String, String> item : queryResult) {
                result.add(item.get("property"));
            }
            return result;
        }

        /**
         * Load and return control for given object.
         *
         * @param reference
         * @return
         */
        private Map<String, Status> loadControl(
                EntityMerger.Reference reference) throws RdfUtilsException {
            final String query = "SELECT ?property ?control WHERE { " +
                    "GRAPH <" + reference.getGraph() + "> {\n" +
                    "  <" + reference.getResource() + "> a ?type ;\n" +
                    "  ?ctrl ?control .\n" +
                    "}\n" +
                    " GRAPH ?g {" +
                    "  ?entity a <" + LP_OBJECTS.DESCRIPTION + "> ;\n" +
                    "   <" + LP_OBJECTS.HAS_DESCRIBE + "> ?type ;\n" +
                    "   <" + LP_OBJECTS.HAS_MEMBER + "> ?member .\n" +
                    "\n" +
                    "  ?member <" + LP_OBJECTS.HAS_PROPERTY +
                    "> ?property ;\n" +
                    "    <" + LP_OBJECTS.HAS_CONTROL + "> ?ctrl .\n" +
                    "} }";
            final List<Map<String, String>> queryResult = RdfUtils.sparqlSelect(
                    reference.getSource(), query);
            final Map<String, Status> output = new HashMap<>();
            for (Map<String, String> entry : queryResult) {
                final String property = entry.get("property");
                final String control = entry.get("control");
                //
                switch (control) {
                    case LP_OBJECTS.FORCE:
                        output.put(property, Status.FORCE);
                        break;
                    case LP_OBJECTS.INHERIT:
                        output.put(property, Status.INHERIT);
                        break;
                    case LP_OBJECTS.INHERIT_AND_FORCE:
                        output.put(property, Status.INHERIT_AND_FORCE);
                        break;
                    case LP_OBJECTS.NONE:
                        output.put(property, Status.NONE);
                        break;
                    case LP_OBJECTS.FORCED:
                        output.put(property, Status.FORCED);
                        break;
                    default:
                        throw new RdfUtilsException("Unsupported type: {}",
                                control);
                }
            }
            return output;
        }

    }

    private static class DefaultControlFactory
            implements EntityMerger.ControlFactory {

        /**
         * Source with definitions.
         */
        private final RdfSource definitionSource;

        /**
         * Graph with definitions.
         */
        private final String definitionGraph;

        /**
         * @param source Does not manage the given definitionSource.
         * @param graph
         */
        public DefaultControlFactory(RdfSource source, String graph) {
            this.definitionSource = source;
            this.definitionGraph = graph;
        }

        @Override
        public EntityMerger.Control create(String type) {
            return new DefaultControl(definitionSource, definitionGraph, type);
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(Configuration.class);

    /**
     * Prepare configuration for given component and put it into given definitionGraph.
     *
     * If runtimeSource or runtimeGraph is null no runtime configuration is
     * used. Given runtimeSource is not shut down in this method.
     *
     * @param iri Output configuration iri.
     * @param component Component for which prepare the configuration.
     * @param runtimeSource Source for runtime configuration, can be null.
     * @param runtimeGraph Graph with runtime configuration, can be null.
     * @param writer Writer for the final configuration.
     * @param pipeline Pipeline with definitions.
     */
    public static void prepareConfiguration(String iri,
            PipelineModel.Component component, RdfSource runtimeSource,
            String runtimeGraph, RdfSource.TypedTripleWriter writer,
            Pipeline pipeline)
            throws ExecutorException {
        final List<EntityMerger.Reference> references = new ArrayList<>(3);
        final PipelineModel.ConfigurationDescription description =
                component.getConfigurationDescription();
        final RdfSource pplSource = pipeline.getSource();
        final String configurationType = description.getConfigurationType();
        // Get definitionGraph and resource for each configuration.
        for (PipelineModel.Configuration configuration
                : component.getConfigurations()) {
            final String graph = configuration.getConfigurationGraph();
            final String query = getQueryForConfiguration(
                    configurationType, graph);
            final String resource;
            try {
                resource = RdfUtils.sparqlSelectSingle(pplSource,
                        query, "resource");
            } catch (RdfUtilsException ex) {
                pplSource.shutdown();
                throw new ExecutorException(
                        "Can't get configuration object of type {} in {}",
                        configurationType, graph, ex);
            }
            // Create a reference to the configuration.
            references.add(new EntityMerger.Reference(
                    resource, graph, pplSource));
        }
        // Get definitionGraph and resource for runtime configuration if
        // provided.
        if (runtimeSource != null && runtimeGraph != null) {
            final String query = getQueryForConfiguration(
                    configurationType, runtimeGraph);
            final String resource;
            try {
                resource = RdfUtils.sparqlSelectSingle(runtimeSource,
                        query, "resource");
            } catch (RdfUtilsException ex) {
                pplSource.shutdown();
                throw new ExecutorException(
                        "Can't get runtime configuration object.", ex);
            }
            //
            references.add(new EntityMerger.Reference(
                    resource, runtimeGraph, runtimeSource));
        }
        // Merge.
        final DefaultControlFactory controlFactory =
                new DefaultControlFactory(pplSource,
                        pipeline.getPipelineGraph());
        try {
            EntityMerger.merge(references, controlFactory, iri, writer,
                    pplSource.getDefaultType());
        } catch (RdfUtilsException ex) {
            pplSource.shutdown();
            throw new ExecutorException("Can't merge data.", ex);
        }
        // Close opened sources.
        pplSource.shutdown();
    }

    private static String getQueryForConfiguration(String type, String graph) {
        return "SELECT ?resource WHERE { GRAPH <" + graph + "> {\n" +
                "   ?resource a <" + type + ">\n" +
                "} }";
    }

}
