package com.linkedpipes.plugin.transformer.chunkedtograph;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.ChunkedStatements;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert chunked statements into a single graph repository.
 */
public final class ChunkedToGraph implements Component.Sequential {

    private static final Logger LOG =
            LoggerFactory.getLogger(ChunkedToGraph.class);

    @Component.InputPort(id = "InputRdf")
    public ChunkedStatements inputRdf;

    @Component.OutputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        final IRI outputGraph = outputRdf.getGraph();
        progressReport.start(inputRdf.size());
        for (ChunkedStatements.Chunk chunk : inputRdf) {
            outputRdf.execute((connection) -> {
                connection.add(chunk.toStatements(), outputGraph);
            });
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
