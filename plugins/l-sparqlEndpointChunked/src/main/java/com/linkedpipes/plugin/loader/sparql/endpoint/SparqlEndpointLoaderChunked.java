package com.linkedpipes.plugin.loader.sparql.endpoint;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SparqlEndpointLoaderChunked implements Component,
        SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
    public ChunkedTriples inputRdf;

    @Component.Configuration
    public SparqlEndpointLoaderChunkedConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        // Create repository.
        final SPARQLRepository sparqlRepository = new SPARQLRepository(
                configuration.getEndpoint());
        // No action here.
        try {
            sparqlRepository.initialize();
        } catch (Throwable t) {
            throw exceptionFactory.failure(
                    "Can't connect to remote SPARQL.", t);
        }
        try {
            clearGraph(sparqlRepository);
            loadData(sparqlRepository);
        } finally {
            sparqlRepository.shutDown();
        }
    }

    private void clearGraph(SPARQLRepository sparqlRepository)
            throws LpException {
        try (final CloseableHttpClient httpclient = getHttpClient()) {
            sparqlRepository.setHttpClient(httpclient);
            if (configuration.isClearDestinationGraph()) {
                clearGraph(sparqlRepository,
                        configuration.getTargetGraphName());
            }
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't clear data.", ex);
        }
    }

    private void loadData(SPARQLRepository sparqlRepository)
            throws LpException {
        try (final CloseableHttpClient httpclient = getHttpClient()) {
            sparqlRepository.setHttpClient(httpclient);
            loadData(sparqlRepository);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't load data.", ex);
        }
    }

    private void loadData(Repository repository) throws LpException {
        final IRI graph = SimpleValueFactory.getInstance().createIRI(
                configuration.getTargetGraphName());
        progressReport.start(inputRdf.size());
        for (ChunkedTriples.Chunk chunk : inputRdf) {
            final Collection<Statement> statements = chunk.toCollection();
            if (statements.size() < configuration.getCommitSize()) {
                // Commit all at once.
                loadData(repository, statements, graph);
                progressReport.entryProcessed();
                continue;
            }
            // Split.
            final List<Statement> toAdd = new ArrayList<>(
                    configuration.getCommitSize());
            final Iterator<Statement> iterator = statements.iterator();
            while (iterator.hasNext()) {
                toAdd.add(iterator.next());
                if (toAdd.size() >= configuration.getCommitSize()) {
                    loadData(repository, toAdd, graph);
                    toAdd.clear();
                }
            }
            if (!toAdd.isEmpty()) {
                loadData(repository, toAdd, graph);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void loadData(Repository repository,
            Collection<Statement> statements, IRI graph) throws LpException {
        try (final RepositoryConnection remote = repository.getConnection()) {
            remote.add(statements, graph);
        }
    }

    private static void clearGraph(Repository repository, String graph) {
        try (RepositoryConnection connection = repository.getConnection()) {
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL,
                    "CLEAR GRAPH <" + graph + ">");
            update.execute();
        }
    }

    private CloseableHttpClient getHttpClient() {
        final CredentialsProvider credsProvider =
                new BasicCredentialsProvider();
        if (configuration.isUseAuthentication()) {
            credsProvider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(
                            configuration.getUserName(),
                            configuration.getPassword()));
        }
        return HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();
    }

}
