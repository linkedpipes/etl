package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EVENTS;
import com.linkedpipes.etl.rdf.utils.RdfFormatter;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;

import java.util.Date;

/**
 * Default abstract implementation of an event.
 */
public abstract class AbstractEvent implements Event {

    protected String iri;

    protected final Date created;

    protected final String type;

    protected String label;

    public AbstractEvent(String type) {
        this.type = type;
        this.created = new Date();
        this.label = null;
    }

    /**
     * @param type
     * @param label Label in english.
     */
    public AbstractEvent(String type, String label) {
        this.type = type;
        this.created = new Date();
        this.label = label;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    @Override
    public void write(TripleWriter writer) throws RdfUtilsException {
        writer.iri(iri, RDF.TYPE, type);
        if (label != null && !label.isEmpty()) {
            writer.string(iri, SKOS.PREF_LABEL, label, "en");
        }
        writer.typed(iri, LP_EVENTS.HAS_CREATED, RdfFormatter.toXsdDate(created),
                XSD.DATETIME);
    }

}
