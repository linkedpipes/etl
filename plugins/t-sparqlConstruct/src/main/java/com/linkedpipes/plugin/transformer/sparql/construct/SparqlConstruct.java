package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SparqlConstruct implements Component, SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlConstruct.class);

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "OutputRdf")
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
        LOG.debug("{} -> {}", inputRdf.getReadGraph(), outputRdf.getWriteGraph());
        // Execute query - TODO We should check that they share
        // the same repository!
        try {
            inputRdf.execute((connection) -> {
                final Update update = connection.prepareUpdate(
                        QueryLanguage.SPARQL, query);
                final SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(inputRdf.getReadGraph());
                dataset.setDefaultInsertGraph(outputRdf.getWriteGraph());
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
