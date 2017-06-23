package com.linkedpipes.plugin.transformer.chunksplitter;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class ChunkSplitter implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(ChunkSplitter.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
    public ChunkedTriples inputRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.Configuration
    public ChunkSplitterConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    private final Map<Resource, List<Statement>> entities = new HashMap<>();

    @Override
    public void execute() throws LpException {
        progressReport.start(inputRdf.size());
        for (ChunkedTriples.Chunk chunk : inputRdf) {
            splitChunk(chunk);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void splitChunk(ChunkedTriples.Chunk chunk) throws LpException {
        Collection<Statement> statements = chunk.toCollection();
        LOG.info("Splitting chunk to entities ...");
        createEntityMap(statements);
        LOG.info("Collecting output resources ...");
        List<Resource> baseResourcesForChunks =
                getResourcesOfType(configuration.getType());
        LOG.info("Creating output for {} resources",
                baseResourcesForChunks.size());
        for (Resource resource : baseResourcesForChunks) {
            createChunk(resource);
        }
        LOG.info("Chunk has been split to {} new chunks",
                baseResourcesForChunks.size());
    }

    private void createEntityMap(Collection<Statement> statements) {
        entities.clear();
        for (Statement statement : statements) {
            Resource subject = statement.getSubject();
            List<Statement> chunk = entities.get(subject);
            if (chunk == null) {
                chunk = new ArrayList<>();
                entities.put(subject, chunk);
            }
            chunk.add(statement);
        }
    }

    private List<Resource> getResourcesOfType(String type) {
        List<Resource> output = new ArrayList<>();
        for (Map.Entry<Resource, List<Statement>> entry : entities.entrySet()) {
            for (Statement statement : entry.getValue()) {
                if (!RDF.TYPE.equals(statement.getPredicate().toString())) {
                    continue;
                }
                if (type.equals(statement.getObject().stringValue())) {
                    output.add(entry.getKey());
                    break;
                }
            }
        }
        return output;
    }

    private void createChunk(Resource resource) throws LpException {
        List<Statement> output = new ArrayList<>();
        Stack<Resource> resourcesToAdd = new Stack();
        Set<Resource> alreadyAdded = new HashSet<>();
        resourcesToAdd.push(resource);
        while (!resourcesToAdd.isEmpty()) {
            Resource resourceToAdd = resourcesToAdd.pop();
            alreadyAdded.add(resourceToAdd);
            List<Statement> statements = statementsForResource(resourceToAdd);
            output.addAll(statements);
            // Add new referenced resources.
            statements.stream()
                    .map(s -> s.getObject())
                    .filter(r -> r instanceof Resource)
                    .filter(r -> !alreadyAdded.contains(r))
                    .forEach(r -> resourcesToAdd.push((Resource) r));
        }
        outputRdf.submit(output);
    }

    private List<Statement> statementsForResource(Resource resource) {
        return entities.getOrDefault(resource, Collections.EMPTY_LIST);
    }

}
