package com.linkedpipes.plugin.transformer.graphmerger;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.GraphListDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.util.Collection;
import org.openrdf.model.IRI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.impl.DatasetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public final class GraphMerger implements SequentialExecution {

    private static final String COPY_QUERY = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    private static final Logger LOG = LoggerFactory.getLogger(GraphMerger.class);

    @DataProcessingUnit.InputPort(id = "InputRdf")
    public GraphListDataUnit inputRdf;

    @DataProcessingUnit.InputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @DataProcessingUnit.Extension
    public ProgressReport progressReport;

    @Override
    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException {
        final IRI outputGraph = outputRdf.getGraph();
        final Collection<IRI> inputGraphs = inputRdf.getGraphs();
        progressReport.start(inputGraphs);
        for (final IRI inputGraph : inputGraphs) {
            // Copy data to output graph.
            LOG.info("Copy: {} -> {}", inputGraph, outputGraph);
            outputRdf.execute((connection) -> {
                final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, COPY_QUERY);
                final DatasetImpl dataset = new DatasetImpl();
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
