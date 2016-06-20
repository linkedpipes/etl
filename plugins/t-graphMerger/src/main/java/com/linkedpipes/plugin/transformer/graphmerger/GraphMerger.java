package com.linkedpipes.plugin.transformer.graphmerger;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.GraphListDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import java.util.Collection;
import org.openrdf.model.IRI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.query.impl.SimpleDataset;

/**
 *
 * @author Petr Škoda
 */
public final class GraphMerger implements Component.Sequential {

    private static final String COPY_QUERY
            = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    private static final Logger LOG
            = LoggerFactory.getLogger(GraphMerger.class);

    @Component.InputPort(id = "InputRdf")
    public GraphListDataUnit inputRdf;

    @Component.InputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        final IRI outputGraph = outputRdf.getGraph();
        final Collection<IRI> inputGraphs = inputRdf.getGraphs();
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
