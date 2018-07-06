package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import com.linkedpipes.etl.storage.rdf.StatementsCollection;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.*;

class Unpacker {

    private static Resource PIPELINE;

    private static IRI HAS_COMPONENT;

    private static Resource JAR_TEMPLATE;

    private static Resource COMPONENT;

    private static Resource TEMPLATE;

    private static Resource CONNECTION;

    private static IRI HAS_CONNECTION;

    private static Resource RUN_AFTER;

    private static IRI HAS_CONFIG_GRAPH;

    private static IRI CONFIG;

    private static IRI HAS_TEMPLATE;

    private static IRI HAS_PORT;

    private static IRI HAS_BINDING;

    private static IRI HAS_SAVE_DEBUG_DATA;

    private static IRI HAS_ORDER;

    private static IRI HAS_EXEC_TYPE;

    private static IRI INPUT;

    private static IRI OUTPUT;

    private static IRI HAS_DISABLED;

    private static IRI HAS_SOURCE;

    private static Resource CONFIGURATION;

    private static Resource TASKS;

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
        HAS_CONNECTION = vf.createIRI("http://linkedpipes.com/ontology/connection");
        RUN_AFTER = vf.createIRI("http://linkedpipes.com/ontology/RunAfter");

        HAS_BINDING = vf.createIRI("http://linkedpipes.com/ontology/binding");
        HAS_SAVE_DEBUG_DATA = vf.createIRI(
                "http://linkedpipes.com/ontology/saveDebugData");
        HAS_ORDER =
                vf.createIRI("http://linkedpipes.com/ontology/executionOrder");
        HAS_EXEC_TYPE =
                vf.createIRI("http://linkedpipes.com/ontology/executionType");
        INPUT = vf.createIRI("http://linkedpipes.com/ontology/Input");
        OUTPUT = vf.createIRI("http://linkedpipes.com/ontology/Output");
        HAS_DISABLED = vf.createIRI("http://linkedpipes.com/ontology/disabled");
        HAS_SOURCE = vf.createIRI("http://linkedpipes.com/ontology/source");

        CONFIGURATION = vf.createIRI("http://linkedpipes.com/ontology/RuntimeConfiguration");
        TASKS = vf.createIRI("http://linkedpipes.com/ontology/TaskList");
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
    private final Set<Resource> addedConfigurations = new HashSet<>();

    private final ValueFactory vf = SimpleValueFactory.getInstance();

    /**
     * Reference to the template service.
     */
    private final TemplateFacade templates;

    private final UnpackOptions options;

    /**
     * Store executions that this execution map to.
     */
    private Map<String, RdfObjects> mappedExecutions;

    /**
     * For each component store list of entities that given
     * component depends on.
     */
    private Map<RdfObjects.Entity, Set<RdfObjects.Entity>> dependencies;

    /**
     * Set of all mapped components.
     */
    private Set<RdfObjects.Entity> mappedComponents;

    private final Configuration configuration;

    /**
     * @param statements Pipeline definition.
     * @param pipelineIriAsString Pipeline resource.
     * @param templates
     */
    private Unpacker(Collection<Statement> statements,
            String pipelineIriAsString, TemplateFacade templates,
            UnpackOptions options, Configuration configuration) {
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
        this.options = options;
        this.configuration = configuration;
    }

    private Resource[] getPortTypes() {
        return new Resource[]{INPUT, OUTPUT, CONFIGURATION, TASKS};
    }

    private Resource[] getConnectionTypes() {
        return new Resource[]{CONNECTION, RUN_AFTER};
    }

    private String getExecutionSourceUrl(String executionIri) {
        String executionId = executionIri.substring(
                executionIri.lastIndexOf("/") + 1);
        return configuration.getExecutorMonitorUrl() + "executions/"
                + executionId;
    }

    /**
     * Download mapped executions and store them into {@link #mappedExecutions}.
     */
    private void downloadExecutions() throws BaseException {
        mappedExecutions = new HashMap<>();
        for (UnpackOptions.ExecutionMapping execution
                : options.getExecutionMapping()) {
            // Download and parse information about the execution.
            final Collection<Statement> executionRdf;
            final HttpClientBuilder builder = HttpClientBuilder.create();
            try (CloseableHttpClient client = builder.build()) {
                final HttpGet request = new HttpGet(
                        getExecutionSourceUrl(execution.getExecution()));
                request.addHeader("Accept",
                        RDFFormat.JSONLD.getDefaultMIMEType());
                final HttpResponse response = client.execute(request);
                final int responseCode =
                        response.getStatusLine().getStatusCode();
                if (responseCode < 200 && responseCode > 299) {
                    // TODO Check and follow redirects ?
                    throw new BaseException("Invalid response code: {} " +
                            " from {}", responseCode, execution.getExecution());
                }
                try (InputStream stream = response.getEntity().getContent()) {
                    executionRdf = Rio.parse(stream, "http://localhost/base",
                            RDFFormat.JSONLD);
                }
            } catch (MalformedURLException ex) {
                throw new BaseException("Invalid execution IRI.", ex);
            } catch (IOException ex) {
                throw new BaseException("Can't get mapped execution.", ex);
            }
            // Store the execution.
            mappedExecutions.put(execution.getExecution(),
                    new RdfObjects(executionRdf));
        }
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
     * @param instance Component with {@link #HAS_CONFIG_GRAPH}.
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
        result.forEach((e) -> instance.add(CONFIG, e));
    }

    /**
     * Recursively expand all templates and instances in the pipeline.
     * Also update the configurations.
     */
    private void expandComponents() throws BaseException {
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
                // There should be only one instance ie. one
                // jar template or a template.
                templateObject.getTyped(TEMPLATE, JAR_TEMPLATE)
                        .forEach((item) -> {
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
                if (!addedConfigurations.contains(templateIri)) {
                    configurations.addAll(templates
                            .getConfig(template));
                    // TODO Do not add same description twice -
                    // under different names. Requires change
                    // in a component description.
                    configurations.addAll(templates
                            .getConfigDescription(template));
                    addedConfigurations.add(templateIri);
                }
                // Add configuration description.
                pipelineObject.addAll(
                        templates.getConfigDescription(template));
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
     * Use connections and runAfter to build up a dependency tree.
     */
    private void buildDependencies() {
        dependencies = new TreeMap<>();
        pipelineObject.getTyped(getConnectionTypes()).forEach((connection) -> {
            final RdfObjects.Entity source =
                    connection.getReference(vf.createIRI(
                            "http://linkedpipes.com/ontology/sourceComponent"));
            final RdfObjects.Entity target =
                    connection.getReference(vf.createIRI(
                            "http://linkedpipes.com/ontology/targetComponent"));
            // Add record to dependency list.
            Set<RdfObjects.Entity> componentDeps = dependencies.get(target);
            if (componentDeps == null) {
                componentDeps = new HashSet<>();
                dependencies.put(target, componentDeps);
            }
            componentDeps.add(source);

        });
        // Add objects with no dependency.
        pipelineObject.getTyped(JAR_TEMPLATE).forEach((component) -> {
            if (!dependencies.containsKey(component)) {
                dependencies.put(component, Collections.EMPTY_SET);
            }
        });
    }

    private void buildMappedSet() {
        mappedComponents = new HashSet<>();
        for (UnpackOptions.ExecutionMapping execMap
                : options.getExecutionMapping()) {
            for (UnpackOptions.ComponentMapping comMap :
                    execMap.getComponents()) {
                mappedComponents.add(
                        pipelineObject.getByIri(comMap.getSource()));
            }
        }
    }

    /**
     * Compute and set execution order for all components.
     */
    private void createExecutionOrder() {
        // Copy dependencies object. As we will destroy one in the
        // process.
        final Map<RdfObjects.Entity, Set<RdfObjects.Entity>> deps
                = new HashMap<>();
        for (Map.Entry<RdfObjects.Entity, Set<RdfObjects.Entity>> entry
                : dependencies.entrySet()) {
            final Set<RdfObjects.Entity> entities = new HashSet<>();
            entities.addAll(entry.getValue());
            deps.put(entry.getKey(), entities);
        }
        // Search for components without dependencies, to those
        // add execution order and remove them from the dependencies.
        Integer executionOrder = 0;
        while (!deps.isEmpty()) {
            final List<RdfObjects.Entity> toRemove = new ArrayList<>(16);
            for (Map.Entry<RdfObjects.Entity, Set<RdfObjects.Entity>> entry
                    : deps.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    toRemove.add(entry.getKey());
                    entry.getKey().add(vf.createIRI(
                            "http://linkedpipes.com/ontology/executionOrder"),
                            vf.createLiteral(++executionOrder));
                }
            }
            // Remove.
            toRemove.forEach((item) -> {
                deps.remove(item);
            });
            // Remove references to removed components.
            deps.entrySet().forEach((entry) -> {
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
        // List of all components we need to assign
        final Collection<RdfObjects.Entity> components
                = pipelineObject.getTyped(JAR_TEMPLATE);
        // List of components to execute.
        final Collection<RdfObjects.Entity> toExecute;
        if (options.getRunToComponent() == null) {
            // Execute all.
            toExecute = new HashSet<>(components);
        } else {
            final RdfObjects.Entity execTo = pipelineObject.getByIri(
                    options.getRunToComponent());
            toExecute = new HashSet<>();
            toExecute.add(execTo);
            // Add transitive dependencies.
            int initialSize;
            do {
                initialSize = toExecute.size();
                //
                final List<RdfObjects.Entity> toAdd = new LinkedList<>();
                toExecute.forEach((c) -> {
                    toAdd.addAll(dependencies.get(c));
                });
                toExecute.addAll(toAdd);
                // As toExecute is a set, we will end as there are no more
                // dependencies.
            } while (toExecute.size() != initialSize);
        }
        //
        components.forEach((component) -> {
            boolean disabled = false;
            try {
                disabled = ((Literal) component.getProperty(HAS_DISABLED))
                        .booleanValue();
            } catch (Exception ex) {
                // Ignore exception.
                // TODO Remove with update of getProperty
            }
            //
            if (disabled) {
                component.add(HAS_EXEC_TYPE, vf.createIRI(
                        "http://linkedpipes.com/resources/execution/type/skip"));
            } else if (mappedComponents.contains(component)) {
                component.add(HAS_EXEC_TYPE, vf.createIRI(
                        "http://linkedpipes.com/resources/execution/type/mapped"));
            } else if (toExecute.contains(component)) {
                component.add(HAS_EXEC_TYPE, vf.createIRI(
                        "http://linkedpipes.com/resources/execution/type/execute"));
            } else {
                // Can happen in case of "execute-to" run.
                component.add(HAS_EXEC_TYPE, vf.createIRI(
                        "http://linkedpipes.com/resources/execution/type/skip"));
            }
        });
    }

    private void addPortSourceForMapped() {
        for (UnpackOptions.ExecutionMapping execMap
                : options.getExecutionMapping()) {
            final RdfObjects execution =
                    mappedExecutions.get((execMap.getExecution()));
            for (UnpackOptions.ComponentMapping comMap :
                    execMap.getComponents()) {
                //
                final RdfObjects.Entity source =
                        execution.getByIri(comMap.getSource());
                final RdfObjects.Entity target =
                        pipelineObject.getByIri(comMap.getTarget());
                // TODO Add null checks.
                // Update ports.
                final Collection<RdfObjects.Entity> sourcePorts
                        = source.getReferences(vf.createIRI(
                        "http://etl.linkedpipes.com/ontology/dataUnit"));
                final Collection<RdfObjects.Entity> targetPorts
                        = target.getReferences(HAS_PORT);
                for (RdfObjects.Entity targetPort : targetPorts) {
                    // Find source port.
                    RdfObjects.Entity sourcePort = null;
                    for (RdfObjects.Entity port : sourcePorts) {
                        if (targetPort.getProperty(HAS_BINDING).equals(
                                port.getProperty(vf.createIRI(
                                        "http://etl.linkedpipes.com/ontology/binding")))) {
                            sourcePort = port;
                            break;
                        }
                    }
                    if (sourcePort == null) {
                        continue;
                    }
                    //
                    targetPort.deleteReferences(HAS_SOURCE);
                    final RdfObjects.Builder builder
                            = new RdfObjects.Builder(pipelineObject);
                    builder.add(RDF.TYPE, vf.createIRI(
                            "http://linkedpipes.com/ontology/PortSource"));
                    //
                    Value executionIri;
                    try {
                        executionIri = sourcePort.getReference(vf.createIRI(
                                "http://etl.linkedpipes.com/ontology/execution"))
                                .getResource();
                    } catch (Exception ex) {
                        executionIri = vf.createIRI(execMap.getExecution());
                    }
                    builder.add("http://linkedpipes.com/ontology/execution",
                            executionIri);

                    builder.add("http://linkedpipes.com/ontology/debug",
                            sourcePort.getProperty(vf.createIRI(
                                    "http://etl.linkedpipes.com/ontology/debug")));

                    builder.add("http://linkedpipes.com/ontology/loadPath",
                            sourcePort.getProperty(vf.createIRI(
                                    "http://etl.linkedpipes.com/ontology/dataPath")));

                    builder.add("http://linkedpipes.com/ontology/debugPath",
                            sourcePort.getProperty(vf.createIRI(
                                    "http://etl.linkedpipes.com/ontology/dataPath")));
                    //
                    final RdfObjects.Entity portSource = builder.create();
                    targetPort.add(vf.createIRI(
                            "http://linkedpipes.com/ontology/dataSource"),
                            portSource);
                }
            }
        }
    }

    private void addExecutionMetadata() {
        final RdfObjects.Entity pipeline =
                pipelineObject.getTypeSingle(PIPELINE);
        final RdfObjects.Entity metadata =
                (new RdfObjects.Builder(pipelineObject)).create();
        //
        metadata.add(RDF.TYPE,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/ExecutionMetadata"));
        if (options.getRunToComponent() == null) {
            if (options.getExecutionMapping().isEmpty()) {
                metadata.add(
                        vf.createIRI(
                                "http://linkedpipes.com/ontology/execution/type"),
                        vf.createIRI(
                                "http://linkedpipes.com/resources/executionType/Full"));
            } else {
                metadata.add(
                        vf.createIRI(
                                "http://linkedpipes.com/ontology/execution/type"),
                        vf.createIRI(
                                "http://linkedpipes.com/resources/executionType/DebugFrom"));
            }
        } else {
            metadata.add(
                    vf.createIRI(
                            "http://linkedpipes.com/ontology/execution/targetComponent"),
                    vf.createIRI(options.getRunToComponent()));

            if (options.getExecutionMapping().isEmpty()) {
                metadata.add(
                        vf.createIRI(
                                "http://linkedpipes.com/ontology/execution/type"),
                        vf.createIRI(
                                "http://linkedpipes.com/resources/executionType/DebugTo"));
            } else {
                metadata.add(
                        vf.createIRI(
                                "http://linkedpipes.com/ontology/execution/type"),
                        vf.createIRI(
                                "http://linkedpipes.com/resources/executionType/DebugFromTo"));
            }
        }

        metadata.add(vf.createIRI(
                "http://linkedpipes.com/ontology/deleteWorkingData"),
                vf.createLiteral(options.isDeleteWorkingDirectory()));

        metadata.add(HAS_SAVE_DEBUG_DATA,
                vf.createLiteral(options.isSaveDebugData()));

        //
        pipeline.add(
                vf.createIRI(
                        "http://linkedpipes.com/ontology/executionMetadata"),
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

    private void addComponentTypeToJarType() {
        final Collection<RdfObjects.Entity> components =
                pipelineObject.getTyped(JAR_TEMPLATE);
        components.forEach((component) -> {
            component.add(RDF.TYPE, COMPONENT);
        });
    }

    /**
     * Remove connections that are not used in pipeline execution.
     *
     * Must be called after {@link #fillExecutionType()}.
     */
    private void removeUnusedConnections() {
        // Find all connection that has as a source component,
        // that is not executed.
        List<RdfObjects.Entity> toDelete = new LinkedList<>();
        Collection<RdfObjects.Entity> connections =
                pipelineObject.getTyped(CONNECTION);
        connections.forEach((connection) -> {
            RdfObjects.Entity source = connection.getReference(vf.createIRI(
                    "http://linkedpipes.com/ontology/sourceComponent"));
            RdfObjects.Entity execType =  source.getReference(HAS_EXEC_TYPE);
            if (execType.getResource().stringValue().equals(
                    "http://linkedpipes.com/resources/execution/type/skip")) {
                toDelete.add(connection);
            }
        });
        // And remove them.
        for (RdfObjects.Entity entity : toDelete) {
            pipelineObject.remove(entity);
        }
    }

    private void referenceConnectionsFromPipeline() {
        final Collection<Statement> toAdd = new LinkedList<>();
        pipelineObject.getTyped(getConnectionTypes()).forEach((connection) -> {
            toAdd.add(vf.createStatement(
                    pipelineIri,
                    HAS_CONNECTION,
                    connection.getResource(),
                    pipelineIri
            ));
        });
        pipelineObject.addAll(toAdd);
    }

    private void setMode() {
        pipelineObject.getTyped(getPortTypes()).forEach((port) -> {
            port.add(HAS_SAVE_DEBUG_DATA, vf.createLiteral(
                    options.isSaveDebugData()
            ));
        });

        final Collection<Statement> toAdd = new LinkedList<>();
        toAdd.add(vf.createStatement(pipelineIri, vf.createIRI(
                "http://linkedpipes.com/ontology/deleteWorkingData"),
                vf.createLiteral(options.isDeleteWorkingDirectory()),
                pipelineIri
        ));
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
     * @param statements Unmodifiable representation of pipeline.
     * @param templates
     * @param pipelineIriAsString
     * @return
     */
    public static Collection<Statement> update(
            Collection<Statement> statements,
            TemplateFacade templates,
            String pipelineIriAsString,
            UnpackOptions options,
            Configuration configuration) throws BaseException {
        final Unpacker unpacker = new Unpacker(statements,
                pipelineIriAsString, templates, options,
                configuration);

        // Resolve references to other executions - from options.
        unpacker.downloadExecutions();

        // Unpack initial references to configurations.
        // Those configurations does not have stored description,
        // but the configurations are of same instances as configurations
        // of respective templates. So the descriptions from templates
        // can be reused.
        unpacker.configGraphToObject(COMPONENT);

        // Expand instances and templates.
        unpacker.expandComponents();

        // Add pipeline to component links. The pipeline editor does not
        // store links from a pipeline resource to components.
        unpacker.addPipelineComponentLinks();

        // Build dependencies list from connections. The component IRI is used
        // to sort the dependencies, so it must not change after this point.
        unpacker.buildDependencies();

        // Build list of mapped components.
        unpacker.buildMappedSet();

        // Create an execution order. This ignore disabled or mapped
        // components.
        unpacker.createExecutionOrder();

        // Add execution type to all components.
        unpacker.fillExecutionType();

        // In case of debug-from we need to replaces some port sources
        // with directories.
        unpacker.addPortSourceForMapped();

        // Add execution metadata.
        unpacker.addExecutionMetadata();

        // Replace blank nodes with IRIs.
        unpacker.instantiateBlankNodes();

        // Add information about used repository.
        unpacker.addRepositoryInfo();

        unpacker.addComponentTypeToJarType();

        unpacker.removeUnusedConnections();

        unpacker.referenceConnectionsFromPipeline();

        unpacker.setMode();

        // Return collected pipeline.
        return unpacker.collect();
    }

}
