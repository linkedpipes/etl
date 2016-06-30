package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.executor.api.v1.rdf.SerializableToRdf;

/**
 * Used to communicate state of the execution as well as user informations
 * to the application.
 *
 * @author Škoda Petr
 */
public interface Event extends SerializableToRdf {

}
