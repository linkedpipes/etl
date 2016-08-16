package com.linkedpipes.etl.storage.component.pipeline;

import com.linkedpipes.etl.storage.BaseException;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Class responsible for to performing update and migration actions
 * on the pipeline in the RDF form.
 *
 * @author Petr Škoda
 */
class PipelineUpdater {

    private static final Logger LOG =
            LoggerFactory.getLogger(PipelineUpdater.class);

    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    public static class OperationFailed extends BaseException {

        OperationFailed(String message, Object... args) {
            super(message, args);
        }

    }

    private final Pipeline pipeline;

    /**
     * List of statements for given resources.
     */
    private Map<Resource, List<Statement>> objects = new HashMap<>();

    /**
     * Reference to resources based on the types.
     */
    private Map<IRI, List<Resource>> objectByType = new HashMap<>();

    /**
     * The resource that identify the pipeline resource.
     */
    private Resource pipelineResource;

    private int version = 0;

    private PipelineUpdater(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Load pipeline from given statements.
     *
     * @param pipelineRdf
     */
    private void loadPipeline(Collection<Statement> pipelineRdf)
            throws OperationFailed {
        parseCollection(pipelineRdf);
        extractPipeline();
    }

    /**
     * Parse the collection of statements into the objects.
     *
     * @param pipelineRdf
     */
    private void parseCollection(Collection<Statement> pipelineRdf) {
        for (Statement statement : pipelineRdf) {
            final Resource resource = statement.getSubject();
            List<Statement> object = objects.get(resource);
            if (object == null) {
                object = new ArrayList<>(15);
                objects.put(resource, object);
            }
            object.add(statement);
            // Check for type reference.
            if (RDF.TYPE.equals(statement.getPredicate())) {
                final IRI type = (IRI) statement.getObject();
                List<Resource> typed = objectByType.get(type);
                if (typed == null) {
                    typed = new LinkedList<>();
                    objectByType.put(type, typed);
                }
                typed.add(statement.getSubject());
            }
        }
    }

    /**
     * Extract information about pipeline resource.
     */
    private void extractPipeline() throws OperationFailed {
        // Get pipeline resource.
        if (!objectByType.containsKey(Pipeline.TYPE)) {
            throw new OperationFailed("Missing pipeline resource.");
        }
        final List<Resource> pipelineObjects = objectByType.get(Pipeline.TYPE);
        if (pipelineObjects.size() != 1) {
            throw new OperationFailed("Invalid number of pipeline resources: "
                    + pipelineObjects.size());
        }
        pipelineResource = pipelineObjects.get(0);
        // Read pipeline definition.
        final List<Statement> pipelineInstance = objects.get(pipelineResource);
        final List<Statement> toRemove = new ArrayList<>(4);
        final List<Statement> toAdd = new ArrayList<>(4);
        for (Statement statement : pipelineInstance) {
            switch (statement.getPredicate().stringValue()) {
                case "http://etl.linkedpipes.com/ontology/version":
                    version = ((Literal) statement.getObject()).intValue();
                    // After end of this function the version is always
                    // the current one.
                    if (version != Pipeline.VERSION_NUMBER) {
                        toRemove.add(statement);
                        toAdd.add(VF.createStatement(
                                statement.getSubject(),
                                statement.getPredicate(),
                                VF.createLiteral(Pipeline.VERSION_NUMBER),
                                statement.getContext()
                        ));
                    }
                    break;
            }
        }
        pipelineInstance.removeAll(toRemove);
        pipelineInstance.addAll(toAdd);
    }

    /**
     * Perform migration.
     */
    private void migrate() throws OperationFailed {
        switch (version) {
            case 0:
                migrate_from_0();
        }

    }

    private void migrate_from_0() throws OperationFailed {
        // Update template IRIs.
        final IRI templateIri
                = VF.createIRI("http://linkedpipes.com/ontology/template");
        for (List<Statement> statements : objects.values()) {
            for (Statement statement : statements) {
                if (statement.getPredicate().equals(templateIri)) {
                    // Parse the string and replace the statement.
                    String iri = statement.getObject().stringValue();
                    iri = iri.substring(iri.lastIndexOf("/" + 1));
                    iri = "http://etl.linkedpipes.com/resources/components/" +
                            iri + "/0.0.0";
                    // Replace the statements - here we break
                    // the iterator but as we quit the iteration in
                    // the next step it is ok.
                    statements.remove(statement);
                    statements.add(VF.createStatement(
                            statement.getSubject(),
                            statement.getPredicate(),
                            VF.createIRI(iri),
                            statement.getContext()
                    ));
                    break;
                }
            }
        }
    }

    /**
     * Perform updates based on the given options.
     *
     * @param options
     */
    private void update(UpdateOptions options) throws OperationFailed {
        if (!options.getLabels().isEmpty()) {
            update_label(options);
        }
        if (options.isImportStream()) {
            update_resources(pipeline.getIri());
        }
    }

    /**
     * Based on given options update pipeline labels.
     *
     * @param options
     */
    private void update_label(UpdateOptions options) throws OperationFailed {
        final List<Statement> pipelineInstance = objects.get(pipelineResource);
        // Remove existing label statements.
        final List<Statement> toRemove = new ArrayList<>(2);
        for (Statement statement : pipelineInstance) {
            if (SKOS.PREF_LABEL.equals(statement.getPredicate())) {
                toRemove.add(statement);
            }
        }
        pipelineInstance.removeAll(toRemove);
        // Add statements.
        final Resource graph = pipelineInstance.get(0).getContext();
        for (Value value : options.getLabels()) {
            pipelineInstance.add(VF.createStatement(pipelineResource,
                    SKOS.PREF_LABEL, value, graph));
        }
    }

    /**
     * Rename graphs and resources to use given baseIri.
     *
     * @param baseIri
     */
    private void update_resources(String baseIri) {
        // Generate new names for graph and all typed
        // objects.
        final Map<Resource, Resource> mapping = new HashMap<>();
        final Set<Resource> graphs = new HashSet<>();
        Integer counter = 0;
        for (List<Statement> statements : objects.values()) {
            for (Statement statement : statements) {
                //
                if (RDF.TYPE.equals(statement.getPredicate())) {
                    if (statement.getObject().equals(Pipeline.TYPE)) {
                        // Skip pipeline.
                        continue;
                    }
                    // Create mapping.
                    String suffix = statement.getObject().stringValue();
                    suffix = suffix.substring(Math.max(
                            suffix.lastIndexOf("/") + 1,
                            suffix.lastIndexOf("#") + 1)).toLowerCase();
                    mapping.put(statement.getSubject(), VF.createIRI(
                            baseIri + "/" + suffix + "/" + (++counter)
                    ));
                }
                graphs.add(statement.getContext());
            }
        }
        // Generate name for graphs.
        Integer graphCounter = 0;
        for (Resource graph : graphs) {
            if (!mapping.containsKey(graph)) {
                mapping.put(graph, VF.createIRI(
                        baseIri + "/graph/" + (++graphCounter)));
            }
        }
        // Set the pipeline mapping.
        final IRI newPipelineResource = VF.createIRI(baseIri);
        mapping.put(pipelineResource, newPipelineResource);
        pipelineResource = newPipelineResource;
        // Replace.
        Map<Resource, List<Statement>> newObjects = new HashMap<>();
        for (Map.Entry<Resource, List<Statement>> entry : objects.entrySet()) {
            final List<Statement> newList
                    = new ArrayList<>(entry.getValue().size());
            for (Statement statement : entry.getValue()) {
                final Resource subject = mapping.getOrDefault(
                        statement.getSubject(), statement.getSubject());
                final Resource graph = mapping.getOrDefault(
                        statement.getContext(), statement.getContext());
                final Value value;
                if (statement.getObject() instanceof Resource) {
                    value = mapping.getOrDefault(
                            statement.getObject(),
                            (Resource) statement.getObject());
                } else {
                    value = statement.getObject();
                }
                newList.add(VF.createStatement(subject,
                        statement.getPredicate(), value, graph));
            }
            newObjects.put(mapping.get(entry.getKey()), newList);
        }
        objects = newObjects;
        // Replace.
        Map<IRI, List<Resource>> newObjectByType = new HashMap<>();
        for (Map.Entry<IRI, List<Resource>> entry : objectByType.entrySet()) {
            final List<Resource> newList
                    = new ArrayList<>(entry.getValue().size());
            for (Resource item : entry.getValue()) {
                newList.add(mapping.get(item));
            }
            newObjectByType.put(entry.getKey(), newList);
        }
        objectByType = newObjectByType;
    }

    /**
     * Collect statements and return them.
     *
     * @return
     */
    private Collection<Statement> collect() {
        final List<Statement> result = new ArrayList<>();
        for (List<Statement> statements : objects.values()) {
            result.addAll(statements);
        }
        return result;
    }

    /**
     * Performs migration and update the pipeline based on the given definition.
     *
     * @param pipeline
     * @param options
     * @param pipelineRdf
     * @return
     */
    public static Collection<Statement> update(Pipeline pipeline,
            UpdateOptions options, Collection<Statement> pipelineRdf)
            throws OperationFailed {
        LOG.info("Updating pipeline {}", pipeline.getIri());
        PipelineUpdater instance = new PipelineUpdater(pipeline);
        instance.loadPipeline(pipelineRdf);
        // Perform migration of pipeline tu current version.
        instance.migrate();
        // Perform update operations.
        instance.update(options);
        final Collection<Statement> result = instance.collect();
        LOG.info("Updating pipeline {} ... done", pipeline.getIri());
        return result;
    }

}
