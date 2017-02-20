package com.linkedpipes.plugin.transformer.graphmerger;

import com.linkedpipes.etl.dataunit.core.rdf.GraphListDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public final class GraphMerger implements Component, SequentialExecution {

    private static final String COPY_QUERY
            = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    private static final Logger LOG
            = LoggerFactory.getLogger(GraphMerger.class);

    @Component.InputPort(iri = "InputRdf")
    public GraphListDataUnit inputRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        final IRI outputGraph = outputRdf.getWriteGraph();
        final Collection<IRI> inputGraphs = inputRdf.getReadGraphs();
        progressReport.start(inputGraphs);
        for (final IRI inputGraph : inputGraphs) {
            // Copy data to output graph.
            LOG.info("Copy: {} -> {}", inputGraph, outputGraph);
            outputRdf.execute((connection) -> {
                final Update update = connection.prepareUpdate(
                        QueryLanguage.SPARQL, COPY_QUERY);
                final SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(inputGraph);
                dataset.setDefaultInsertGraph(outputGraph);
                update.setDataset(dataset);
                update.execute();
            });
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
