package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.impl.DatasetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public final class SparqlConstruct implements SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlConstruct.class);

    @DataProcessingUnit.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @DataProcessingUnit.InputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @DataProcessingUnit.Configuration
    public SparqlConstructConfiguration configuration;

    @Override
    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException {
        // We always perfrom inserts.
        final String query = configuration.getQuery().replaceFirst("(?i)CONSTRUCT", "INSERT");
        LOG.debug("Query: {}", query);
        LOG.debug("{} -> {}", inputRdf.getGraph(), outputRdf.getGraph());
        // Execute query - TODO We should check that they share the same repository!
        inputRdf.execute((connection) -> {
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, query);
            final DatasetImpl dataset = new DatasetImpl();
            dataset.addDefaultGraph(inputRdf.getGraph());
            dataset.setDefaultInsertGraph(outputRdf.getGraph());
            update.setDataset(dataset);
            update.execute();
        });
    }

}
