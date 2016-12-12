package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableChunkedStatements;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Use scrollable cursors to execute SPARQL construct.
 */
public final class SparqlEndpointConstructScrollableCursor
        implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(
            SparqlEndpointConstructScrollableCursor.class);

    @Component.InputPort(id = "OutputFiles")
    public WritableChunkedStatements outputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public SparqlEndpointConstructScrollableCursorConfiguration configuration;

    @Override
    public void execute() throws LpException {
        final Repository repository = new SPARQLRepository(
                configuration.getEndpoint());
        repository.initialize();
        //
        LOG.info("Used query: {}", prepareQuery(0));
        try {
            int offset = 0;
            final List<Statement> buffer = new ArrayList<>(100000);
            while (true) {
                LOG.info("offset: {}", offset);
                executeQuery(repository, offset, buffer);
                if (buffer.isEmpty()) {
                    break;
                } else {
                    outputRdf.submit(buffer);
                }
                offset += configuration.getPageSize();
            }
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

    /**
     * @param repository
     * @param offset
     */
    protected void executeQuery(Repository repository, int offset,
            List<Statement> buffer) throws LpException {
        try (final RepositoryConnection connection =
                     repository.getConnection()) {
            //
            final GraphQuery query = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL, prepareQuery(offset));
            //
            final SimpleDataset dataset = new SimpleDataset();
            for (String iri : configuration.getDefaultGraphs()) {
                if (!iri.isEmpty()) {
                    dataset.addDefaultGraph(
                            SimpleValueFactory.getInstance().createIRI(iri));
                }
            }
            query.setDataset(dataset);
            buffer.clear();
            query.evaluate(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st)
                        throws RDFHandlerException {
                    buffer.add(st);
                }
            });
        }
    }

    protected String prepareQuery(int offset) {
        return configuration.getPrefixes() + "\n CONSTRUCT {\n" +
                configuration.getOuterConstruct() + "\n } WHERE { {" +
                configuration.getInnerSelect() +
                "\n} }" +
                "\nLIMIT " + Integer.toString(configuration.getPageSize()) +
                "\nOFFSET " + Integer.toString(offset);
    }

}
