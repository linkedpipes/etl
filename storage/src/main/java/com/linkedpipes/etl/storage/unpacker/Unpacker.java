package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.storage.component.template.Template;
import com.linkedpipes.etl.storage.component.template.TemplateFacade;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import com.linkedpipes.etl.storage.rdf.StatementsCollection;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.Repositories;
import org.openrdf.sail.memory.MemoryStore;

import java.util.*;

/**
 * @author Petr Škoda
 */
class Unpacker {

    private static Resource PIPELINE;

    private static IRI HAS_COMPONENT;

    private static Resource JAR_TEMPLATE;

    private static Resource COMPONENT;

    private static Resource TEMPLATE;

    private static Resource CONNECTION;

    private static IRI HAS_CONFIG_GRAPH;

    private static IRI CONFIG;

    private static IRI HAS_TEMPLATE;

    private static IRI HAS_PORT;

    private static IRI HAS_BINDING;

    private static IRI HAS_ORDER;

    private static IRI HAS_EXEC_TYPE;

    private static IRI INPUT;

    private static IRI OUTPUT;

    static {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        PIPELINE = vf.createIRI("http://linkedpipes.com/ontology/Pipeline");
        HAS_COMPONENT = vf.createIRI(
                "http://linkedpipes.com/ontology/component");
        JAR_TEMPLATE = vf.createIRI(
                "http://linkedpipes.com/ontology/JarTemplate");
        COMPONENT = vf.createIRI(
                "http://linkedpipes.com/ontology/Component");
        TEMPLATE = vf.createIRI("http://linkedpipes.com/ontology/Template");
        HAS_CONFIG_GRAPH = vf.createIRI(
                "http://linkedpipes.com/ontology/configurationGraph");
        CONFIG = vf.createIRI("http://linkedpipes.com/ontology/configuration");
        HAS_TEMPLATE = vf.createIRI("http://linkedpipes.com/ontology/template");
        HAS_PORT = vf.createIRI("http://linkedpipes.com/ontology/port");
        CONNECTION = vf.createIRI("http://linkedpipes.com/ontology/Connection");
        HAS_BINDING = vf.createIRI("http://linkedpipes.com/ontology/binding");
        HAS_ORDER =
                vf.createIRI("http://linkedpipes.com/ontology/executionOrder");
        HAS_EXEC_TYPE =
                vf.createIRI("http://linkedpipes.com/ontology/executionType");
        INPUT = vf.createIRI("http://linkedpipes.com/ontology/Input");
        OUTPUT = vf.createIRI("http://linkedpipes.com/ontology/Output");
    }

    private final IRI pipelineIri;

    /**
     * Pipeline object.
     */
    private final RdfObjects pipelineObject;

    /**
     * Store configurations.
     */
    private final StatementsCollection configurations;

    /**
     * Monitor added configurations to not add a single configuration
     * more then one time.
     */
    private final Set<Resource> adddedConfigurations = new HashSet<>();

    private final ValueFactory vf = SimpleValueFactory.getInstance();

    /**
     * Reference to the template service.
     */
    private final TemplateFacade templates;

    /**
     * @param statements          Pipeline definition.
     * @param pipelineIriAsString Pipeline resource.
     * @param templates
     */
    private Unpacker(Collection<Statement> statements,
            String pipelineIriAsString,
            TemplateFacade templates) {
        this.pipelineIri = vf.createIRI(pipelineIriAsString);
        this.templates = templates;
        //
        final StatementsCollection all = new StatementsCollection(statements);
        // Filter configurations.
        this.configurations = all.filter((s) -> {
            return !s.getContext().equals(this.pipelineIri);
        });
        // Filter pipeline graph only.
        final StatementsCollection pipelineRdf = all.filter((s) -> {
            return s.getContext().equals(this.pipelineIri);
        });
        this.pipelineObject = new RdfObjects(pipelineRdf.getStatements());
    }

    /**
     * For given types convert the reference to {@link #HAS_CONFIG_GRAPH}
     * to configuration object.
     *
     * @param types
     */
    private void configGraphToObject(Resource... types) {
        pipelineObject.getTyped(types).forEach((instance) -> {
            configGraphToObject(pipelineObject, instance, vf);
        });
    }

    /**
     * @param pipelineObject
     * @param instance       Component with {@link #HAS_CONFIG_GRAPH}.
     */
    private static void configGraphToObject(RdfObjects pipelineObject,
            RdfObjects.Entity instance, ValueFactory valueFactory) {
        // Use an offset so there is some space.
        final int initialSize = instance.getReferences(CONFIG).size() + 1024;
        final Collection<RdfObjects.Entity> result = new LinkedList<>();
        instance.getReferences(HAS_CONFIG_GRAPH).forEach((reference) -> {
            result.add((new RdfObjects.Builder(pipelineObject))
                    .addResource(RDF.TYPE,
                            "http://linkedpipes.com/ontology/Configuration")
                    .addResource(
                            "http://linkedpipes.com/ontology/configuration/graph",
                            reference.getResource())
                    .add("http://linkedpipes.com/ontology/configuration/order",
                            valueFactory.createLiteral(
                                    result.size() + initialSize))
                    .create());
        });
        //
        instance.deleteReferences(HAS_CONFIG_GRAPH);
        instance.addAll(CONFIG, result);
    }

    /**
     * Recursively expand all templates and instances in the pipeline.
     * Also update the configurations.
     */
    private void expandComponents() {
        Collection<RdfObjects.Entity> toUnpack =
                pipelineObject.getTyped(COMPONENT, TEMPLATE);
        while (!toUnpack.isEmpty()) {
            for (RdfObjects.Entity component : toUnpack) {
                final Resource templateIri =
                        component.getReference(HAS_TEMPLATE).getResource();
                component.deleteReferences(HAS_TEMPLATE);
                final Template template
                        = templates.getTemplate(templateIri.stringValue());
                // Expand template
                // TODO Here we can use caching.
                final RdfObjects templateObject
                        = new RdfObjects(templates.getDefinition(template));
                // There should be only one instance.
                templateObject.getTyped(TEMPLATE, JAR_TEMPLATE).forEach((item) -> {
                    // Preserve type from the template,
                    // otherwise merge everything.
                    component.add(item, Collections.EMPTY_LIST,
                            Arrays.asList(RDF.TYPE));
                });
                // Update references to the configurations.
                configGraphToObject(pipelineObject, component, vf);
                // Generate new resources for ports as they have to be unique.
                component.getReferences(HAS_PORT).forEach((item) -> {
                    pipelineObject.changeResource(item);
                });
                // Add template configuration to other configurations,
                // if it has not already been added.
                // We also have to add description, so the executor
                // can work with the configuration.
                if (!adddedConfigurations.contains(templateIri)) {
                    configurations.addAll(templates.getConfig(template));
                    // TODO Do not add same description twice -
                    // under different names. Requires change
                    // in a component description.
                    configurations.addAll(templates.getConfigDesc(template));
                    adddedConfigurations.add(templateIri);
                }
            }
            toUnpack = pipelineObject.getTyped(COMPONENT, TEMPLATE);
        }
    }

    private void addPipelineComponentLinks() {
        final RdfObjects.Entity pipeline =
                pipelineObject.getTypeSingle(PIPELINE);
        final Collection<RdfObjects.Entity> components =
                pipelineObject.getTyped(JAR_TEMPLATE);
        components.forEach((component) -> {
            pipeline.add(HAS_COMPONENT, component);
        });
    }

    /**
     * Convert connections to port. Remove all connections objects.
     * Create and return dependency map.
     * <p>
     * Returned dependency map is {@Link TreeMap} to secure same
     * ordering for multiple calls as the elements are
     * sorted by IRI.
     *
     * @return
     */
    private Map<RdfObjects.Entity, Set<RdfObjects.Entity>> connectionsToPorts() {
        final Map<RdfObjects.Entity, Set<RdfObjects.Entity>>
                dependencies = new TreeMap<>();
        pipelineObject.getTyped(CONNECTION).forEach((connection) -> {
            final RdfObjects.Entity source =
                    connection.getReference(vf.createIRI(
                            "http://linkedpipes.com/ontology/sourceComponent"));
            final Value sourceBinding =
                    connection.getProperty(vf.createIRI(
                            "http://linkedpipes.com/ontology/sourceBinding"));
            final RdfObjects.Entity target =
                    connection.getReference(vf.createIRI(
                            "http://linkedpipes.com/ontology/targetComponent"));
            final Value targetBinding =
                    connection.getProperty(vf.createIRI(
                            "http://linkedpipes.com/ontology/targetBinding"));
            //
            final RdfObjects.Entity sourcePort = getPort(source, sourceBinding);
            final RdfObjects.Entity targetPort = getPort(target, targetBinding);
            //
            targetPort.add(vf.createIRI(
                    "http://linkedpipes.com/ontology/source"),
                    sourcePort);
            pipelineObject.remove(connection);
            // Add record to dependency list.
            Set<RdfObjects.Entity> componentDeps = dependencies.get(target);
            if (componentDeps == null) {
                componentDeps = new HashSet<>();
                dependencies.put(target, componentDeps);
            }
            componentDeps.add(source);
        });
        // Add components that have no connection.
        pipelineObject.getTyped(JAR_TEMPLATE).forEach((component) -> {
            if (!dependencies.containsKey(component)) {
                dependencies.put(component, Collections.EMPTY_SET);
            }
        });
        //
        return dependencies;
    }

    /**
     * @param component
     * @param binding
     * @return Port of given binding name for given component.
     */
    private static RdfObjects.Entity getPort(
            RdfObjects.Entity component, Value binding) {
        for (RdfObjects.Entity port : component.getReferences(HAS_PORT)) {
            if (port.getProperty(HAS_BINDING).equals(binding)) {
                return port;
            }
        }
        // TODO Use custom exception !
        throw new RuntimeException("Missing port.");
    }

    /**
     * Given dependencies as returned from {@link #connectionsToPorts()}
     * add execution order to all components.
     *
     * @param dependencies
     */
    private void createExecutionOrder(
            Map<RdfObjects.Entity, Set<RdfObjects.Entity>> dependencies) {
        pipelineObject.getTyped(JAR_TEMPLATE).forEach((component) -> {
            if (!dependencies.containsKey(component)) {
                dependencies.put(component, Collections.EMPTY_SET);
            }
        });
        Integer executionOrder = 0;
        while (!dependencies.isEmpty()) {
            final List<RdfObjects.Entity> toRemove = new ArrayList<>(16);
            for (Map.Entry<RdfObjects.Entity, Set<RdfObjects.Entity>> entry
                    : dependencies.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    toRemove.add(entry.getKey());
                    entry.getKey().add(vf.createIRI(
                            "http://linkedpipes.com/ontology/executionOrder"),
                            vf.createLiteral(++executionOrder));
                }
            }
            // Remove.
            toRemove.forEach((item) -> {
                dependencies.remove(item);
            });
            dependencies.entrySet().forEach((entry) -> {
                entry.getValue().removeAll(toRemove);
            });
            //
            if (toRemove.isEmpty()) {
                throw new RuntimeException("Cycle detected.");
            }
        }
    }

    /**
     * Add execution type to components without the execution type.
     */
    private void fillExecutionType() {
        pipelineObject.getTyped(JAR_TEMPLATE).forEach((component) -> {
            if (component.getReferences(HAS_EXEC_TYPE).isEmpty()) {
                component.add(HAS_EXEC_TYPE, vf.createIRI(
                        "http://linkedpipes.com/resources/execution/type/execute"));
            }
        });
    }

    /**
     * Add the uriFragment and requirement to each port.
     */
    private void addStaticToPorts() {
        // TODO We could add reference to the RdfObject.Entry instead.

        // TODO Add PORT type - check if used ..
        Integer counter = 0;
        for (RdfObjects.Entity port : pipelineObject.getTyped(INPUT, OUTPUT)) {
            port.add(
                    vf.createIRI("http://linkedpipes.com/ontology/uriFragment"),
                    vf.createLiteral("" + ++counter));
            //
            port.add(
                    vf.createIRI(
                            "http://linkedpipes.com/ontology/requirement"),
                    vf.createIRI(
                            "http://linkedpipes.com/resources/requirement/debug"));
        }
    }

    private void addExecutionMetadata() {
        final RdfObjects.Entity pipeline =
                pipelineObject.getTypeSingle(PIPELINE);
        final RdfObjects.Entity metadata =
                (new RdfObjects.Builder(pipelineObject)).create();
        //
        metadata.add(RDF.TYPE,
                vf.createIRI("http://linkedpipes.com/ontology/ExecutionMetadata"));

        metadata.add(vf.createIRI("http://linkedpipes.com/ontology/execution/type"),
                vf.createIRI("http://linkedpipes.com/resources/executionType/Full"));
        //
        pipeline.add(
                vf.createIRI("http://linkedpipes.com/ontology/executionMetadata"),
                metadata);
    }

    /**
     * Change all blank nodes to the IRI instances.
     */
    private void instantiateBlankNodes() {
        pipelineObject.updateBlankNodes(pipelineIri.stringValue() + "/blank/");
    }

    private void addRepositoryInfo() {
        final Collection<Statement> toAdd = new LinkedList<>();

        // TODO Add repository type!
        // "http://linkedpipes.com/ontology/Repository",
        // "http://linkedpipes.com/ontology/dataUnit/sesame/1.0/Repository"

        final IRI sesame = vf.createIRI(
                "http://localhost/repository/sesame");
        toAdd.add(vf.createStatement(sesame, RDF.TYPE, vf.createIRI(
                "http://linkedpipes.com/ontology/Repository"),
                pipelineIri
        ));
        toAdd.add(vf.createStatement(sesame, RDF.TYPE, vf.createIRI(
                "http://linkedpipes.com/ontology/dataUnit/sesame/1.0/Repository"),
                pipelineIri
        ));
        toAdd.add(vf.createStatement(sesame, vf.createIRI(
                "http://linkedpipes.com/ontology/requirement"),
                vf.createIRI(
                        "http://linkedpipes.com/resources/requirement/workingDirectory"),
                pipelineIri
        ));
        toAdd.add(vf.createStatement(pipelineIri, vf.createIRI(
                "http://linkedpipes.com/ontology/repository"),
                sesame, pipelineIri
        ));
        pipelineObject.addAll(toAdd);
    }

    private void addRequirementObjects() {
        final Collection<Statement> toAdd = new ArrayList<>(16);
        final IRI workingDir = vf.createIRI(
                "http://linkedpipes.com/resources/requirement/workingDirectory");
        toAdd.add(vf.createStatement(
                workingDir, RDF.TYPE,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/requirements/Requirement"),
                pipelineIri
        ));
        toAdd.add(vf.createStatement(
                workingDir, RDF.TYPE,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/requirements/TempDirectory"),
                pipelineIri
        ));
        toAdd.add(vf.createStatement(
                workingDir,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/requirements/target"),
                vf.createIRI(
                        "http://linkedpipes.com/ontology/workingDirectory"),
                pipelineIri
        ));
        final IRI pplInput = vf.createIRI(
                "http://linkedpipes.com/resources/components/e-pipelineInput/inputDirectory");
        toAdd.add(vf.createStatement(
                pplInput, RDF.TYPE,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/requirements/Requirement"),
                pipelineIri
        ));
        toAdd.add(vf.createStatement(
                pplInput, RDF.TYPE,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/requirements/InputDirectory"),
                pipelineIri
        ));
        toAdd.add(vf.createStatement(
                pplInput,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/requirements/target"),
                vf.createIRI(
                        "http://linkedpipes.com/resources/components/e-pipelineInput/inputDirectory"),
                pipelineIri
        ));
        final IRI debugDir = vf.createIRI(
                "http://linkedpipes.com/resources/requirement/debug");
        toAdd.add(vf.createStatement(
                debugDir, RDF.TYPE,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/requirements/Requirement"),
                pipelineIri
        ));
        toAdd.add(vf.createStatement(
                debugDir, RDF.TYPE,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/requirements/TempDirectory"),
                pipelineIri
        ));
        toAdd.add(vf.createStatement(
                debugDir,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/requirements/target"),
                vf.createIRI(
                        "http://linkedpipes.com/ontology/debugDirectory"),
                pipelineIri
        ));
        //
        pipelineObject.addAll(toAdd);
    }

    private Collection<Statement> collect() {
        // TODO Remove usage of repository for pretty print.
        final Collection<Statement> result
                = pipelineObject.asStatements(pipelineIri);
        result.addAll(configurations.getStatements());
        final Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        Repositories.consume(repo, (connection) -> connection.add(result));
        repo.shutDown();
        //
        return result;
    }

    /**
     * Return collection that represents the pipeline.
     *
     * @param statements          Unmodifiable representation of pipeline.
     * @param templates
     * @param pipelineIriAsString
     * @return
     */
    public static Collection<Statement> update(
            Collection<Statement> statements,
            TemplateFacade templates,
            String pipelineIriAsString,
            UnpackOptions options) {
        final Unpacker unpacker = new Unpacker(statements,
                pipelineIriAsString, templates);

        // Resolve references to other executions - from options.

        // Build the list of disabled and mapped components.

        // Unpack initial references to configurations.
        // Those configurations does not have stored description,
        // but the configurations are of same instances as configurations
        // of respective templates. So the descriptions from templates
        // can be resued.
        unpacker.configGraphToObject(COMPONENT);

        // Expand instance and templates.
        unpacker.expandComponents();

        // Add pipeline to component links.
        // The current pipeline editor does not store links
        // from a pipeline resource to components.
        unpacker.addPipelineComponentLinks();

        // Convert connections to ports and build dependencies list while doing
        // so. As we use the IRI to sort components here
        // it must not change before the dependencies object is used.
        final Map<RdfObjects.Entity, Set<RdfObjects.Entity>> dependencies =
                unpacker.connectionsToPorts();

        // In case of debug-from we need to replaces some port sources
        // with directories.

        // Create an execution order.
        unpacker.createExecutionOrder(dependencies);

        // Based on the given options (debug from/to) add execution types.

        // Add execution type to all components that does not have one
        // already - ie. that are not disabled etc ...
        unpacker.fillExecutionType();

        // Add uriFragment and debug directory to ports.
        unpacker.addStaticToPorts();

        // Add execution metadata.
        unpacker.addExecutionMetadata();

        // Replace blank nodes with IRIs.
        unpacker.instantiateBlankNodes();

        // Add information about used repository.
        unpacker.addRepositoryInfo();

        // Add static resources (requirements).
        unpacker.addRequirementObjects();

        // Return collected pipeline.
        return unpacker.collect();
    }

}
