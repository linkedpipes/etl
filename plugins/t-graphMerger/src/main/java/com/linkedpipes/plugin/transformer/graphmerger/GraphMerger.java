package com.linkedpipes.plugin.transformer.graphmerger;

import com.linkedpipes.etl.dataunit.core.rdf.GraphListDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public final class GraphMerger implements Component, SequentialExecution {

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
        IRI outputGraph = outputRdf.getWriteGraph();
        Collection<IRI> inputGraphs = inputRdf.getReadGraphs();
        progressReport.start(inputGraphs);
        for (final IRI inputGraph : inputGraphs) {
            copyGraph(inputGraph, outputGraph);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void copyGraph(IRI inputGraph, IRI outputGraph)
            throws LpException {
        LOG.info("Copy: {} -> {}", inputGraph, outputGraph);
        inputRdf.execute((inConnection) -> {
            RepositoryResult<Statement> statements =
                    inConnection.getStatements(null, null, null, inputGraph);
            outputRdf.execute((outConnection) -> {
                outConnection.add(statements, outputGraph);
            });
        });
    }

}
