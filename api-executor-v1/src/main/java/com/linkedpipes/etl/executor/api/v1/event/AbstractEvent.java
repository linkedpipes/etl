package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EVENTS;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Default abstract implementation of an event.
 */
public abstract class AbstractEvent implements Event {

    private final static DateFormat DATE_FORMAT
            = new SimpleDateFormat("YYYY-MM-dd");

    private final static DateFormat TIME_FORMAT
            = new SimpleDateFormat("HH:mm:ss.SSS");

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
    public void write(RdfSource.TripleWriter writer) {
        writer.iri(iri, RDF.TYPE, type);
        if (label != null && !label.isEmpty()) {
            writer.string(iri, SKOS.PREF_LABEL, label, "en");
        }
        final StringBuilder createdAsString = new StringBuilder(25);
        createdAsString.append(DATE_FORMAT.format(created));
        createdAsString.append("T");
        createdAsString.append(TIME_FORMAT.format(created));
        writer.typed(iri, LP_EVENTS.HAS_CREATED,
                createdAsString.toString(), XSD.DATETIME);
    }

}
