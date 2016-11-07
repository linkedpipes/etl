package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.query.impl.SimpleDataset;

/**
 *
 * @author Škoda Petr
 */
public final class SparqlConstruct implements Component.Sequential {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlConstruct.class);

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public SparqlConstructConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getQuery() == null
                || configuration.getQuery().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlConstructVocabulary.HAS_QUERY);
        }
        // We always perform inserts.
        final String query = updateQuery(configuration.getQuery());
        LOG.debug("Query: {}", query);
        LOG.debug("{} -> {}", inputRdf.getGraph(), outputRdf.getGraph());
        // Execute query - TODO We should check that they share
        // the same repository!
        try {
            inputRdf.execute((connection) -> {
                final Update update = connection.prepareUpdate(
                        QueryLanguage.SPARQL, query);
                final SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(inputRdf.getGraph());
                dataset.setDefaultInsertGraph(outputRdf.getGraph());
                update.setDataset(dataset);
                update.execute();
            });
        } catch (Throwable t) {
            throw exceptionFactory.failure("Can't execute given query.", t);
        }
    }

    /**
     * Rewrite given SPARQL construct to SPARQL insert.
     *
     * @param query
     * @return
     */
    static String updateQuery(String query) {
        return query.replaceFirst("(?i)CONSTRUCT\\s*\\{", "INSERT \\{");
    }
;

}
