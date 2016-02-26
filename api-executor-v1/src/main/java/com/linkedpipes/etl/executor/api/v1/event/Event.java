package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;

/**
 * Used to communicate state of the execution as well as user informations to the application.
 *
 * @author Škoda Petr
 */
public interface Event {

    public void assignSubject(String messageUri);

    public void write(StatementWriter writer);

}
