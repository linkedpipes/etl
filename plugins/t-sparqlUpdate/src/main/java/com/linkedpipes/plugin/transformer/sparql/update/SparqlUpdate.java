package com.linkedpipes.plugin.transformer.sparql.update;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SparqlUpdate implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(SparqlUpdate.class);

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public SparqlUpdateConfiguration configuration;

    @Override
    public void execute() throws LpException {
        final IRI inputGraph = inputRdf.getReadGraph();
        final IRI outputGraph = outputRdf.getWriteGraph();
        LOG.info("Update: {} -> {}", inputGraph, outputGraph);
        LOG.info("Query: {}", configuration.getQuery());
        // Copy data.
        inputRdf.execute((connection) -> {
            connection.add(connection
                            .getStatements(null, null, null, true, inputGraph),
                    outputGraph);
            LOG.info("Input size: {}", connection.size(outputGraph));
        });
        // Perform update.
        inputRdf.execute((connection) -> {
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL,
                    configuration.getQuery());
            final SimpleDataset dataset = new SimpleDataset();
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
