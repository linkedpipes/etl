package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.OpenRDFException;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriterFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Execute given sparql query at the remote repository and save
 * the result as a file.
 */
public final class SparqlEndpointSelect implements Component.Sequential {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlEndpointSelect.class);

    @Component.InputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public SparqlEndpointSelectConfiguration configuration;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() throws LpException {
        if (configuration.getEndpoint() == null
                || configuration.getEndpoint().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlEndpointSelectVocabulary.HAS_ENDPOINT);
        }
        if (configuration.getQuery() == null
                || configuration.getQuery().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlEndpointSelectVocabulary.HAS_QUERY);
        }
        //
        final SPARQLRepository repository
                = new SPARQLRepository(configuration.getEndpoint());
        // Customize repository.
        final Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (configuration.getFileName() != null) {
            headers.put("Accept", configuration.getFileName());
        }
        repository.setAdditionalHttpHeaders(headers);
        //
        try {
            repository.initialize();
        } catch (OpenRDFException ex) {
            throw exceptionFactory.failure("Can't connect to endpoint.", ex);
        }
        //
        final File outputFile = outputFiles.createFile(
                configuration.getFileName()).toFile();
        try {
            queryRemote(repository, outputFile, configuration.getQuery());
        } catch (Throwable t) {
            throw exceptionFactory.failure("Can't query remote SPARQL.", t);
        } finally {
            try {
                repository.shutDown();
            } catch (RepositoryException ex) {
                LOG.error("Can't close repository.", ex);
            }
        }
    }

    public void queryRemote(SPARQLRepository repository, File outputFile,
            String queryAsString) throws LpException {
        final SPARQLResultsCSVWriterFactory writerFactory =
                new SPARQLResultsCSVWriterFactory();
        try (RepositoryConnection remoteConnection
                = repository.getConnection()) {
            final TupleQuery query = remoteConnection.prepareTupleQuery(
                            QueryLanguage.SPARQL,
                    queryAsString);
            // Construct dataset.
            final SimpleDataset dataset = new SimpleDataset();
            for (String iri : configuration.getDefaultGraphs()) {
                dataset.addDefaultGraph(valueFactory.createIRI(iri));
            }
            query.setDataset(dataset);
            try (final OutputStream outputStream
                         = new FileOutputStream(outputFile)) {
                final TupleQueryResultWriter resultWriter
                        = writerFactory.getWriter(outputStream);
                query.evaluate(resultWriter);
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't save data.", ex);
            }
        }
    }

}
