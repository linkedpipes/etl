package com.linkedpipes.etl.executor.execution.message;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionMessageWriter extends BaseMessageWriter {

    // TODO Remove and replace with references to vocabulary.
    private static final String LP_PREFIX =
            "http://linkedpipes.com/ontology/";

    public ExecutionMessageWriter(
            String executionIri, AtomicInteger eventCounter, File file) {
        super(executionIri, eventCounter, file);
    }

    public void onExecutionBegin() {
        int index = this.messageCounter.getAndIncrement();
        IRI iri = this.createEventIri(index);
        this.createBaseEvent(iri, index);

        this.statements.addIri(iri, RDF.TYPE,
                LP_PREFIX + "events/ExecutionBegin");
    }

    public void onExecutionEnd() {
        int index = this.messageCounter.getAndIncrement();
        IRI iri = this.createEventIri(index);
        this.createBaseEvent(iri, index);

        this.statements.addIri(iri, RDF.TYPE,
                LP_PREFIX + "events/ExecutionEnd");
    }

}
