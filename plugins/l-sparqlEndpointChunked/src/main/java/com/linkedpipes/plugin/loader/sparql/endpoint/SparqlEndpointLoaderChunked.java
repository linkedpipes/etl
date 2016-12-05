package com.linkedpipes.plugin.loader.sparql.endpoint;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.AfterExecution;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.ChunkedStatements;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SparqlEndpointLoaderChunked implements Component.Sequential {

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(id = "InputRdf")
    public ChunkedStatements inputRdf;

    @Component.Configuration
    public SparqlEndpointLoaderChunkedConfiguration configuration;

    @Component.Inject
    public AfterExecution afterExecution;

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
        //
        afterExecution.addAction(() -> {
            sparqlRepository.shutDown();
        });
        //
        try (final CloseableHttpClient httpclient = getHttpClient()) {
            sparqlRepository.setHttpClient(httpclient);
            if (configuration.isClearDestinationGraph()) {
                clearGraph(sparqlRepository,
                        configuration.getTargetGraphName());
            }
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't clear data.", ex);
        }
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
        for (ChunkedStatements.Chunk chunk : inputRdf) {
            final Collection<Statement> statements = chunk.toStatements();
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
                    loadData(repository, statements, graph);
                    toAdd.clear();
                }
            }
            if (!toAdd.isEmpty()) {
                loadData(repository, statements, graph);
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
        if (configuration.isUseAuthentification()) {
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
