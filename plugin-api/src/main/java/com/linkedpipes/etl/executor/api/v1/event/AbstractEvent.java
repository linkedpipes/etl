package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP;
import com.linkedpipes.etl.executor.api.v1.vocabulary.RDF;
import com.linkedpipes.etl.executor.api.v1.vocabulary.SKOS;

import java.util.Date;

/**
 * Abstract implementation of an event.
 */
public abstract class AbstractEvent implements Event {

    protected String iri;

    protected final Date created;

    protected final String type;

    protected String label;

    public AbstractEvent(String type) {
        this.type = type;
        this.created = new Date();
    }

    @Override
    public void setIri(String iri) {
        this.iri = iri;
    }

    @Override
    public void write(TripleWriter writer) {
        writer.iri(iri, RDF.TYPE, type);
        if (label != null && !label.isEmpty()) {
            writer.string(iri, SKOS.PREF_LABEL, label, "en");
        }
        writer.date(iri, LP.HAS_CREATED, created);
    }

}
