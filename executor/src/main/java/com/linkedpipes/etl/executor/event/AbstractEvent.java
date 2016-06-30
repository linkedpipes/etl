package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Petr Škoda
 */
abstract class AbstractEvent implements Event {

    private final static DateFormat DATE_FORMAT
            = new SimpleDateFormat("YYYY-MM-dd");

    private final static DateFormat TIME_FORMAT
            = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * Message IRI.
     */
    protected String iri;

    protected final Date created;

    /**
     * Given message type.
     */
    protected final String type;

    /**
     * Label in English.
     */
    protected String label;

    AbstractEvent(String type) {
        this.created = new Date();
        this.type = type;
        this.label = null;
    }

    AbstractEvent(String type, String label) {
        this.created = new Date();
        this.type = type;
        this.label = label;
    }

    @Override
    public void setResource(String subject) {
        this.iri = subject;
    }

    @Override
    public String getResource() {
        return this.iri;
    }

    @Override
    public void serialize(Writer writer) {
        writer.addUri(iri,
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                type);
        if (label != null) {
            writer.addString(iri,
                    "http://www.w3.org/2004/02/skos/core#prefLabel",
                    label,
                    "en");
        }

        final StringBuilder createdAsString = new StringBuilder(25);
        createdAsString.append(DATE_FORMAT.format(created));
        createdAsString.append("T");
        createdAsString.append(TIME_FORMAT.format(created));

        writer.add(iri,
                LINKEDPIPES.EVENTS.HAS_CREATED,
                createdAsString.toString(),
                "http://www.w3.org/2001/XMLSchema#datetime");
    }
}
