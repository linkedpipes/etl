package com.linkedpipes.plugin.transformer.sparql.update;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit.SesameDataUnitException;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import org.openrdf.model.IRI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.impl.DatasetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.ExecutionFailed;

/**
 *
 * @author Škoda Petr
 */
public final class SparqlUpdate implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlUpdate.class);

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public SparqlUpdateConfiguration configuration;

    @Override
    public void execute() throws ExecutionFailed, SesameDataUnitException {
        final IRI inputGraph = inputRdf.getGraph();
        final IRI outputGraph = outputRdf.getGraph();
        LOG.info("Update: {} -> {}", inputGraph, outputGraph);
        LOG.info("Query: {}", configuration.getQuery());
        // Copy data.
        inputRdf.execute((connection) -> {
            connection.add(connection.getStatements(null, null, null, true, inputGraph), outputGraph);
            LOG.info("Input size: {}", connection.size(outputGraph));
        });
        // Perform update.
        inputRdf.execute((connection) -> {
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, configuration.getQuery());
            final DatasetImpl dataset = new DatasetImpl();
            dataset.addDefaultGraph(outputGraph);
            dataset.addDefaultRemoveGraph(outputGraph);
            dataset.setDefaultInsertGraph(outputGraph);
            update.setDataset(dataset);
            update.execute();
        });
        inputRdf.execute((connection) -> {
            LOG.info("Output size: {}", connection.size(outputGraph));
        });

    }

}
