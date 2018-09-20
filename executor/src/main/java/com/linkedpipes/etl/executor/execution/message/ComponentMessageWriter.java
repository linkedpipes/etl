package com.linkedpipes.etl.executor.execution.message;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ComponentMessageWriter extends BaseMessageWriter {

    // TODO Remove and replace with references to vocabulary.
    private static final String LP_PREFIX =
            "http://linkedpipes.com/ontology/";

    private final DefaultComponentTripleWriter writer;

    public ComponentMessageWriter(
            String executionIri, AtomicInteger eventCounter, File file) {
        super(executionIri, eventCounter, file);
        this.writer = new DefaultComponentTripleWriter(
                this.statements, this.executionIri);
    }

    public void addEvent(ExecutionComponent component, Event event) {
        int index = this.messageCounter.getAndIncrement();
        IRI iri = this.createEventIri(index);
        this.createBaseEvent(iri, index);

        this.statements.add(this.valueFactory.createStatement(iri,
                this.valueFactory.createIRI(LP_PIPELINE.HAS_COMPONENT),
                this.valueFactory.createIRI(component.getIri()),
                this.executionIri));

        event.setIri(iri.stringValue());
        event.write(this.writer);
    }

    public void onComponentBegin(ExecutionComponent component) {
        int index = this.messageCounter.getAndIncrement();
        IRI iri = this.createEventIri(index);
        this.createBaseEvent(iri, index);

        this.statements.addIri(iri, RDF.TYPE,
                LP_PREFIX + "events/ComponentBegin");
        this.statements.addIri(iri,
                "http://linkedpipes.com/ontology/component",
                component.getIri());
    }

    public void onComponentEnd(ExecutionComponent component) {
        int index = this.messageCounter.getAndIncrement();
        IRI iri = this.createEventIri(index);
        this.createBaseEvent(iri, index);

        this.statements.addIri(iri, RDF.TYPE,
                LP_PREFIX + "events/ComponentEnd");
        this.statements.addIri(iri,
                "http://linkedpipes.com/ontology/component",
                component.getIri());
    }

    public void onComponentFailed(
            ExecutionComponent component, LpException exception) {
        int index = this.messageCounter.getAndIncrement();
        IRI iri = this.createEventIri(index);
        this.createBaseEvent(iri, index);

        this.statements.addIri(iri, RDF.TYPE,
                LP_PREFIX + "events/ComponentFailed");
        this.statements.addIri(iri,
                "http://linkedpipes.com/ontology/component",
                component.getIri());

        // Get exceptions.
        LpException lpException = null;
        Throwable rootCause = exception;
        while (rootCause.getCause() != null) {
            if (lpException == null && rootCause instanceof LpException) {
                lpException = (LpException) rootCause;
            }
            rootCause = rootCause.getCause();
        }

        // Format into a message.
        if (lpException != null) {
            this.statements.addString(iri,
                    LP_PREFIX + "events/reason",
                    lpException.getMessage());
        }
        if (rootCause.getMessage() == null) {
            this.statements.addString(iri,
                    LP_PREFIX + "events/rootException",
                    rootCause.getClass().getSimpleName());
        } else {
            String message = rootCause.getClass().getSimpleName() +
                    " : " + rootCause.getMessage();
            this.statements.addString(iri,
                    LP_PREFIX + "events/rootException",
                    message);
        }
    }

    @Override
    public void save() throws IOException {
        // Do not save on empty messages.
        if (this.statements.isEmpty()) {
            return;
        }
        super.save();
    }
}
