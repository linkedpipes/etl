package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;
import org.openrdf.query.impl.SimpleDataset;

/**
 *
 * @author Škoda Petr
 */
public final class SparqlConstruct implements SimpleExecution {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlConstruct.class);

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public SparqlConstructConfiguration configuration;

    @Override
    public void execute(Component.Context context) throws NonRecoverableException {
        // We always perfrom inserts.
        final String query = configuration.getQuery().replaceFirst("(?i)CONSTRUCT", "INSERT");
        LOG.debug("Query: {}", query);
        LOG.debug("{} -> {}", inputRdf.getGraph(), outputRdf.getGraph());
        // Execute query - TODO We should check that they share the same repository!
        inputRdf.execute((connection) -> {
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, query);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(inputRdf.getGraph());
            dataset.setDefaultInsertGraph(outputRdf.getGraph());
            update.setDataset(dataset);
            update.execute();
        });
    }

}
