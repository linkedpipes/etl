package com.linkedpipes.plugin.transformer.property.linker;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Linker is designed to solve problems, where links SPARQL construct
 * is execute on two big datasets, while it can be executed in isolation
 * on their chunks.
 * <p>
 * For each reference chunk and each data chunk are put together,
 * over these data the given query is executed.
 * <p>
 * However in some cases SPARQL can be slow, for this reason we build index
 * of objects using user provided property. In this index
 * we then search for matching objects to every chunk.
 */
public final class PropertyLinkerChunked implements Component,
        SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(PropertyLinkerChunked.class);

    private static final int EXPECTED_CHUNK_SIZE = 4;

    @Component.InputPort(iri = "DataRdf")
    public ChunkedTriples dataRdf;

    @Component.InputPort(iri = "ReferenceRdf")
    public SingleGraphDataUnit referenceRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.Configuration
    public PropertyLinkedConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    private List<Statement> outputChunk = new ArrayList<>();

    @Override
    public void execute() throws LpException {
        validateConfiguration();
        LOG.info("Building reference index ...");
        Map<Value, List<Statement>> index = createReferenceIndex();
        progressReport.start(dataRdf.size());
        for (ChunkedTriples.Chunk chunk : dataRdf) {
            LOG.info("Linking chunk ...");
            linkChunk(chunk, index);
            progressReport.entryProcessed();
            LOG.info("Linking chunk ... done");
        }
        progressReport.done();
    }

    private void validateConfiguration() throws LpException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        try {
            valueFactory.createIRI(this.configuration.getChunkPredicate());
        } catch (Exception ex) {
            throw new LpException("Invalid configuration 'chunk predicate'.");
        }
        try {
            valueFactory.createIRI(this.configuration.getDataPredicate());
        } catch (Exception ex) {
            throw new LpException("Invalid configuration 'data predicate'.");
        }
    }


    private Map<Value, List<Statement>> createReferenceIndex()
            throws LpException {
        Map<Resource, Value> resourceToValue = new HashMap<>();
        Map<Value, List<Statement>> output = new HashMap<>();
        IRI predicate = SimpleValueFactory.getInstance().createIRI(
                this.configuration.getDataPredicate());

        RDFHandler findRelevantResources = new AbstractRDFHandler() {
            @Override
            public void handleStatement(Statement st) {
                resourceToValue.put(st.getSubject(), st.getObject());
                output.put(st.getObject(), new ArrayList<>(4));
            }
        };

        RDFHandler collectObjects = new AbstractRDFHandler() {
            @Override
            public void handleStatement(Statement st) {
                Value key = resourceToValue.get(st.getSubject());
                if (key == null) {
                    return;
                }
                output.get(key).add(st);
            }
        };

        this.referenceRdf.execute((connection) -> {
            connection.exportStatements(null, predicate, null, false,
                    findRelevantResources, referenceRdf.getReadGraph());
            connection.export(collectObjects, referenceRdf.getReadGraph());
        });

        return output;
    }


    private void linkChunk(
            ChunkedTriples.Chunk chunk, Map<Value, List<Statement>> reference)
            throws LpException {
        Collection<Statement> inputChunk = chunk.toCollection();
        List<Value> chunkValues = findChunkLinkingValues(inputChunk);
        this.outputChunk.clear();
        this.outputChunk.addAll(inputChunk);
        for (Value value : chunkValues) {
            List<Statement> toAdd = reference.getOrDefault(
                    value, Collections.EMPTY_LIST);
            this.outputChunk.addAll(toAdd);
        }
        this.outputRdf.submit(outputChunk);
    }

    private List<Value> findChunkLinkingValues(
            Collection<Statement> statements) {
        IRI predicate = SimpleValueFactory.getInstance().createIRI(
                this.configuration.getChunkPredicate());
        return statements.stream()
                .filter((st) -> st.getPredicate().equals(predicate))
                .map((st) -> st.getObject())
                .collect(Collectors.toList());
    }

}
