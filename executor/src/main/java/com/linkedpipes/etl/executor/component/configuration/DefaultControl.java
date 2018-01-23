package com.linkedpipes.etl.executor.component.configuration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.executor.rdf.entity.EntityReference;
import com.linkedpipes.etl.executor.rdf.entity.MergeControl;
import com.linkedpipes.etl.executor.rdf.entity.MergeType;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Control loading of RDF data into entities.
 *
 * TODO Update to work with only two levels (pipeline, runtime) of configuration.
 */
class DefaultControl implements MergeControl {

    /**
     * Represent a control of given property.
     */
    private static class PropertyControl {

        private final String predicate;

        private final String control;

        public PropertyControl(String predicate, String control) {
            this.predicate = predicate;
            this.control = control;
        }
    }

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultControl.class);

    private static final List<String> ALWAYS_LOAD_PROPERTIES =
            Arrays.asList(RDF.TYPE);

    private List<PropertyControl> controlledPredicates;

    private List<String> complexPredicates;

    /**
     * For pair definitionGraph-resource store list of properties and their
     * merge type.
     */
    private final Map<String, Map<String, MergeType>>
            control = new HashMap<>();

    /**
     * Store reference to current object as definitionGraph-resource.
     */
    private String currentKey;

    public DefaultControl() {
    }

    /**
     * Load definition of entity of given type.
     *
     * @param source
     * @param type
     */
    public void loadDefinition(BackendRdfSource source, String type)
            throws RdfUtilsException {
        // TODO Do not search for all graphs, ie. use definition from
        // given component.
        loadControlledProperties(source, type);
        loadComplexProperties(source, type);
    }

    @Override
    public void init(List<EntityReference> references)
            throws RdfUtilsException {
        prepareControlObject(references);
        loadComplexProperties(references);
        // Load controlledPredicates from entities.
        final List<Map<String, Configuration.Status>> controlsInReferences =
                new ArrayList<>(references.size());
        for (EntityReference ref : references) {
            controlsInReferences.add(loadControlInferDefaults(ref));
        }
        // Build predicate list.
        final Set<String> allPredicates = new HashSet<>();
        for (Map<String, Configuration.Status> map : controlsInReferences) {
            allPredicates.addAll(map.keySet());
        }
        // For each predicate determine status (load/skip/merge).
        for (String predicate : allPredicates) {
            // Search for indexes for:
            int forceFrom = -1;
            int lastLoad = -1;
            final List<Integer> mergeFrom =
                    new ArrayList<>(references.size());
            for (int i = 0; i < references.size(); ++i) {
                final Configuration.Status status =
                        controlsInReferences.get(i).get(predicate);
                // In every other case clear merge list.
                mergeFrom.clear();
                if (status == Configuration.Status.FORCE) {
                    forceFrom = i;
                    break;
                } else if (status == Configuration.Status.INHERIT_AND_FORCE) {
                    forceFrom = i - 1;
                    break;
                } else if (status == Configuration.Status.NONE ||
                        status == Configuration.Status.FORCED) {
                    lastLoad = i;
                }
            }
            // By default skip all.
            final Set<Integer> loadFrom = new HashSet<>();
            if (forceFrom != -1) {
                loadFrom.add(forceFrom);
            } else if (lastLoad != -1) {
                loadFrom.add(lastLoad);
            } else {
                LOG.info("Predicate: {}", predicate);
                for (EntityReference ref : references) {
                    LOG.info("\tref: {} {}", ref.getResource(),
                            ref.getGraph());
                }
                throw new RdfUtilsException("Can't determine loading " +
                        "sequence: {} {}", forceFrom, lastLoad);
            }
            //
            int counter = 0;
            for (EntityReference ref : references) {
                final String key = ref.getGraph() + "-" + ref.getResource();
                if (loadFrom.contains(counter)) {
                    control.get(key).put(predicate, MergeType.LOAD);
                } else {
                    control.get(key).put(predicate, MergeType.SKIP);
                }
                ++counter;
            }
        }
    }

    @Override
    public void onReference(String resource, String graph)
            throws RdfUtilsException {
        currentKey = graph + "-" + resource;
        if (!control.containsKey(currentKey)) {
            throw new RdfUtilsException("Missing reference to resource: {} {}",
                    resource, graph);
        }
    }

    @Override
    public MergeType onProperty(String property)
            throws RdfUtilsException {
        if (ALWAYS_LOAD_PROPERTIES.contains(property)) {
            return MergeType.LOAD;
        }
        final MergeType type = control.get(currentKey).get(property);
        if (type == null) {
            return MergeType.SKIP;
        } else {
            return type;
        }
    }

    private void loadControlledProperties(BackendRdfSource source,
            String type) throws RdfUtilsException {
        final String query = "SELECT ?property ?control WHERE { \n" +
                " GRAPH ?g {" +
                "  ?entity a <" + LP_OBJECTS.DESCRIPTION + "> ;\n" +
                "   <" + LP_OBJECTS.HAS_DESCRIBE + "> <" + type + "> ;\n" +
                "   <" + LP_OBJECTS.HAS_MEMBER + "> ?member .\n" +
                "\n" +
                "  ?member <" + LP_OBJECTS.HAS_PROPERTY + "> ?property ;\n" +
                "    <" + LP_OBJECTS.HAS_CONTROL + "> ?control.\n" +
                "\n" +
                "} }";
        controlledPredicates = new ArrayList<>(16);
        for (Map<String, String> item : RdfUtils.sparqlSelect(source, query)) {
            controlledPredicates.add(new PropertyControl(item.get("property"),
                    item.get("control")));
        }
    }

    private void loadComplexProperties(BackendRdfSource source,
            String type) throws RdfUtilsException {
        final String query = "SELECT ?property ?control WHERE { \n" +
                " GRAPH ?g {" +
                "  ?entity a <" + LP_OBJECTS.DESCRIPTION + "> ;\n" +
                "   <" + LP_OBJECTS.HAS_DESCRIBE + "> <" + type + "> ;\n" +
                "   <" + LP_OBJECTS.HAS_MEMBER + "> ?member .\n" +
                "\n" +
                "  ?member <" + LP_OBJECTS.HAS_PROPERTY + "> ?property ;\n" +
                "    <" + LP_OBJECTS.IS_COMPLEX + "> true.\n" +
                "\n" +
                "} }";
        complexPredicates = new ArrayList<>(16);
        for (Map<String, String> item : RdfUtils.sparqlSelect(source, query)) {
            complexPredicates.add(item.get("property"));
        }
    }

    private void prepareControlObject(List<EntityReference> references) {
        control.clear();
        for (EntityReference ref : references) {
            final String key = ref.getGraph() + "-" + ref.getResource();
            control.put(key, new HashMap<>());
        }
    }

    /**
     * Complex properties represent controlled object, that must be merged
     * per-property.
     *
     * @param references
     */
    private void loadComplexProperties(
            List<EntityReference> references) throws RdfUtilsException {
        for (EntityReference ref : references) {
            final String key = ref.getGraph() + "-" + ref.getResource();
            for (String predicate : complexPredicates) {
                control.get(key).put(predicate, MergeType.MERGE);
            }
        }
    }

    private Map<String, Configuration.Status> loadControlInferDefaults(
            EntityReference reference) throws RdfUtilsException {
        final Map<String, Configuration.Status> controls =
                loadControl(reference);
        // Not all properties must have control values set (invalid
        // configuration, runtime configuration, ... )m so for those
        // we use NONE as default.
        final Set<String> properties =
                loadControlledExistingProperties(reference);
        for (String property : properties) {
            if (controls.containsKey(property)) {
                continue;
            }
            controls.put(property, Configuration.Status.NONE);
        }
        return controls;
    }

    private Set<String> loadControlledExistingProperties(
            EntityReference reference) throws RdfUtilsException {
        final String query = buildLoadExistingControlledPropertiesQuery(
                controlledPredicates, reference.getGraph(),
                reference.getResource());
        final List<Map<String, String>> queryResult = RdfUtils.sparqlSelect(
                reference.getSource(), query);
        final Set<String> output = new HashSet<>();
        for (Map<String, String> entry : queryResult) {
            final String control = entry.get("property");
            output.add(control);
        }
        return output;
    }

    /**
     * @param reference
     * @return For each predicate control value.
     */
    private Map<String, Configuration.Status> loadControl(
            EntityReference reference) throws RdfUtilsException {
        final String query = buildLoadControlsQuery(controlledPredicates,
                reference.getGraph(), reference.getResource());
        final List<Map<String, String>> queryResult = RdfUtils.sparqlSelect(
                reference.getSource(), query);
        final Map<String, Configuration.Status> output = new HashMap<>();
        for (Map<String, String> entry : queryResult) {
            final String property = entry.get("property");
            switch (entry.get("control")) {
                case LP_OBJECTS.FORCE:
                    output.put(property, Configuration.Status.FORCE);
                    break;
                case LP_OBJECTS.INHERIT:
                    output.put(property, Configuration.Status.INHERIT);
                    break;
                case LP_OBJECTS.INHERIT_AND_FORCE:
                    output.put(property,
                            Configuration.Status.INHERIT_AND_FORCE);
                    break;
                case LP_OBJECTS.NONE:
                    output.put(property, Configuration.Status.NONE);
                    break;
                case LP_OBJECTS.FORCED:
                    output.put(property, Configuration.Status.FORCED);
                    break;
                default:
                    throw new RdfUtilsException("Unsupported type: {}",
                            entry.get("control"));
            }
        }
        return output;
    }

    /**
     * @param controlDefinitions
     * @param graph
     * @param resource
     * @return Query for control values of all properties.
     */
    private static String buildLoadControlsQuery(
            List<PropertyControl> controlDefinitions,
            String graph, String resource) {
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT ?property ?control WHERE { " +
                "GRAPH <" + graph + "> {\n" +
                "  <" + resource + "> a ?type ;\n" +
                "  ?controlPredicate ?control .\n" +
                "}\n" +
                "VALUES( ?property ?controlPredicate ) {\n");
        for (PropertyControl item : controlDefinitions) {
            builder.append("  ( <").append(item.predicate).append("> <")
                    .append(item.control).append("> )\n");
        }
        builder.append("} }");
        return builder.toString();
    }

    private static String buildLoadExistingControlledPropertiesQuery(
            List<PropertyControl> controlDefinitions,
            String graph, String resource) {
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT ?property WHERE { " +
                "GRAPH <" + graph + "> {\n" +
                "  <" + resource + "> a ?type ;\n" +
                "  ?property ?value .\n" +
                "}\n" +
                "VALUES( ?property ) {\n");
        for (PropertyControl item : controlDefinitions) {
            builder.append("  ( <").append(item.predicate).append("> )\n");
        }
        builder.append("} }");
        return builder.toString();
    }

}

