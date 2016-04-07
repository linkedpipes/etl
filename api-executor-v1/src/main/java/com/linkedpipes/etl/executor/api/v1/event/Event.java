package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;

/**
 * Used to communicate state of the execution as well as user informations to the application.
 *
 * @author Škoda Petr
 */
public interface Event {

    public void setResource(String iri);

    /**
     * The IRI that should be referenced from execution. Is not
     * called before {@link #setResource(java.lang.String)}.
     *
     * @return Resource IRI.
     */
    public String getResource();

    public void write(StatementWriter writer);

}
