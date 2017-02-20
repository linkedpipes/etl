package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Buffered output handler.
 */
class RdfOutput {

    private final static ValueFactory VALUE_FACTORY
            = SimpleValueFactory.getInstance();

    private final WritableChunkedTriples dataUnit;

    private final List<Statement> buffer = new ArrayList<>(10000);

    private final int linesPerChunk;

    private int linesCounter = 0;

    RdfOutput(WritableChunkedTriples dataUnit, int linesPerChunk) {
        this.dataUnit = dataUnit;
        this.linesPerChunk = linesPerChunk;
    }

    public void onRowStart() {
        // No operation here.
    }

    public void onRowEnd() throws LpException {
        linesCounter++;
        if (linesCounter >= linesPerChunk) {
            linesCounter = 0;
            flushBuffer();
        }
    }

    public void onFileStart() throws LpException {
        // No operation here.
    }

    public void onFileEnd() throws LpException {
        flushBuffer();
    }

    public void submit(Resource subject, IRI predicate, Value object) {
        buffer.add(VALUE_FACTORY.createStatement(
                subject, predicate, object));
    }

    private void flushBuffer() throws LpException {
        if (!buffer.isEmpty()) {
            dataUnit.submit(buffer);
        }
        buffer.clear();
    }

}
