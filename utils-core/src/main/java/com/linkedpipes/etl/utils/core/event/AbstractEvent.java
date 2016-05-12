package com.linkedpipes.etl.utils.core.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;

/**
 *
 * @author Škoda Petr
 */
public abstract class AbstractEvent implements Event {

    /**
     * Message URI.
     */
    protected String uri;

    protected final Date created;

    /**
     * Given message type.
     */
    protected final String type;

    protected String label;

    protected String labelLanguage;

    protected final static DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd");

    protected final static DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    public AbstractEvent(String type) {
        this.created = new Date();
        this.type = type;
        this.label = null;
        this.labelLanguage = null;
    }

    public AbstractEvent(String type, String label, String labelLanguage) {
        this.created = new Date();
        this.type = type;
        this.label = label;
        this.labelLanguage = labelLanguage;
    }

    @Override
    public void setResource(String subject) {
        this.uri = subject;
    }

    @Override
    public String getResource() {
        return this.uri;
    }

    @Override
    public void write(StatementWriter writer) {
        writer.addUri(uri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", type);
        if (label != null) {
            writer.addString(uri, "http://www.w3.org/2004/02/skos/core#prefLabel", label, labelLanguage);
        }

        final StringBuilder createdAsString = new StringBuilder(25);
        createdAsString.append(DATE_FORMAT.format(created));
        createdAsString.append("T");
        createdAsString.append(TIME_FORMAT.format(created));

        writer.add(uri, LINKEDPIPES.EVENTS.HAS_CREATED, createdAsString.toString(),
                "http://www.w3.org/2001/XMLSchema#datetime");
    }

}
