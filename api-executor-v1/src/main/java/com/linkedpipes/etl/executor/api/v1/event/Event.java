package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;

public interface Event {

    /**
     * Set iri for this message.
     *
     * @param iri
     */
    void setIri(String iri);

    void write(TripleWriter builder);

}
