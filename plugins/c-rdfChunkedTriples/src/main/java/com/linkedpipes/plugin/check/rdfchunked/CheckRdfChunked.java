package com.linkedpipes.plugin.check.rdfchunked;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;

import java.util.Collection;
import java.util.Iterator;

public class CheckRdfChunked implements Component, SequentialExecution {

    @Component.InputPort(iri = "Expected")
    public ChunkedTriples expected;

    @Component.InputPort(iri = "Actual")
    public ChunkedTriples actual;

    @Override
    public void execute() throws LpException {
        checkSize();
        checkContent();
    }

    private void checkSize() throws LpException {
        if (expected.size() != actual.size()) {
            throw new LpException(
                    "Expected and Actual inputs have different size");
        }
    }

    private void checkContent() throws LpException {
        Iterator<ChunkedTriples.Chunk> expectedIterator = expected.iterator();
        Iterator<ChunkedTriples.Chunk> actualIterator = actual.iterator();
        for (int index = 0; index < expected.size(); ++index) {
            ChunkedTriples.Chunk expectedChunk = expectedIterator.next();
            ChunkedTriples.Chunk actualChunk = actualIterator.next();
            Model expectedModel = createModel(expectedChunk.toCollection());
            Model actualModel = createModel(actualChunk.toCollection());
            if (!Models.isomorphic(expectedModel, actualModel)) {
                throw new LpException(
                        "Expected and Actual inputs are not isomorphic.");
            }
        }
    }

    private Model createModel(Collection<Statement> statements) throws LpException {
        Model model = new LinkedHashModel();
        model.addAll(statements);
        return model;
    }

}
