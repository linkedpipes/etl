package com.linkedpipes.plugin.transformer.chunkedtograph;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.IRI;

/**
 * Convert chunked statements into a single graph repository.
 */
public final class ChunkedToGraph implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputRdf")
    public ChunkedTriples inputRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        final IRI outputGraph = outputRdf.getWriteGraph();
        progressReport.start(inputRdf.size());
        for (ChunkedTriples.Chunk chunk : inputRdf) {
            outputRdf.execute((connection) -> {
                connection.add(chunk.toCollection(), outputGraph);
            });
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
