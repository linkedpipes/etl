package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriterFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.IDN;
import java.util.HashMap;
import java.util.Map;

/**
 * Execute given sparql query at the remote repository and save
 * the result as a file.
 */
public final class SparqlEndpointSelect implements Component,
        SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlEndpointSelect.class);

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Configuration
    public SparqlEndpointSelectConfiguration configuration;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() throws LpException {
        if (configuration.getEndpoint() == null
                || configuration.getEndpoint().isEmpty()) {
            throw new LpException("Missing property: {}",
                    SparqlEndpointSelectVocabulary.HAS_ENDPOINT);
        }
        if (configuration.getQuery() == null
                || configuration.getQuery().isEmpty()) {
            throw new LpException("Missing property: {}",
                    SparqlEndpointSelectVocabulary.HAS_QUERY);
        }
        //
        final SPARQLRepository repository = new SPARQLRepository(getEndpoint());
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
            throw new LpException("Can't connect to endpoint.", ex);
        }
        repository.setHttpClient(getHttpClient());
        //
        final File outputFile = outputFiles.createFile(
                configuration.getFileName());
        try {
            queryRemote(repository, outputFile, configuration.getQuery());
        } catch (Throwable t) {
            throw new LpException("Can't query remote SPARQL.", t);
        } finally {
            try {
                repository.shutDown();
            } catch (RepositoryException ex) {
                LOG.error("Can't close repository.", ex);
            }
        }
    }

    private String getEndpoint() {
        String[] tokens = configuration.getEndpoint().split("://", 2);
        String[] url = tokens[1].split("/", 2);
        return tokens[0] + "://" + IDN.toASCII(url[0]) + "/" + url[1];
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
                throw new LpException("Can't save data.", ex);
            }
        }
    }

    private CloseableHttpClient getHttpClient() {
        CredentialsProvider provider = new BasicCredentialsProvider();
        if (configuration.isUseAuthentication()) {
            provider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(
                            configuration.getUsername(),
                            configuration.getPassword()));
        }
        return HttpClients.custom()
                .setDefaultCredentialsProvider(provider).build();
    }

}
