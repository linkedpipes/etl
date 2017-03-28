package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.rdf.utils.pojo.RdfWriter;

/**
 * Used to communicate additional state of the execution as well as user
 * information to the application.
 */
public interface Event extends RdfWriter.Writable {

    /**
     * Set iri for this message.
     *
     * @param iri
     */
    void setIri(String iri);

}
