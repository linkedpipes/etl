package com.linkedpipes.etl.executor.api.v1.event;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;

public interface Event {

    /**
     * Set iri for this message.
     *
     * @param iri
     */
    void setIri(String iri);

    void write(TripleWriter builder) throws RdfUtilsException;

}
