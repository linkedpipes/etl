package com.linkedpipes.plugin.extractor.sparql.endpoint;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;

/**
 *
 * @author Škoda Petr
 */
public final class SparqlEndpoint implements SimpleExecution {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlEndpoint.class);

    @Component.InputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public SparqlEndpointConfiguration configuration;

    @Override
    public void execute(Component.Context context) throws NonRecoverableException {
        //
        final SPARQLRepository repository = new SPARQLRepository(configuration.getEndpoint());
        try {
            repository.initialize();
        } catch (OpenRDFException ex) {
            throw new Component.ExecutionFailed("Can't connnect to endpoint.", ex);
        }
        //
        try {
            queryRemote(repository);
        } finally {
            try {
                repository.shutDown();
            } catch (RepositoryException ex) {
                LOG.error("Can't close repository.", ex);
            }
        }
    }

    public void queryRemote(SPARQLRepository repository) throws ExecutionFailed {
        final URI graph = outputRdf.getGraph();
        try (RepositoryConnection localConnection = outputRdf.getRepository().getConnection()) {
            localConnection.begin();
            // We can't use Repositories.graphQuery (Repositories.get) here, as Virtuoso fail with
            // 'No permission to execute procedure DB.DBA.SPARUL_RUN'
            // as sesame try to execute given action in a transaction.
            try (RepositoryConnection remoteConnection = repository.getConnection()) {
                final GraphQuery preparedQuery = remoteConnection.prepareGraphQuery(QueryLanguage.SPARQL,
                        configuration.getQuery());
                final GraphQueryResult result =  preparedQuery.evaluate();
                localConnection.add(result, graph);
            }
            localConnection.commit();
        }
    }

}
