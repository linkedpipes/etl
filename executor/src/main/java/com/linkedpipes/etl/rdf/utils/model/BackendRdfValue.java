package com.linkedpipes.etl.rdf.utils.model;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

import java.util.Calendar;

/**
 * Library independent representation of a RDF object with value.
 */
public interface BackendRdfValue {

    String asString();

    long asLong() throws RdfUtilsException;

    boolean asBoolean() throws RdfUtilsException;

    Double asDouble() throws RdfUtilsException;

    Calendar asCalendar() throws RdfUtilsException;

    boolean isBlankNode();

    /**
     * Can be null.
     */
    String getType();

    /**
     * Can be null.
     */
    String getLanguage();

    boolean isIri();

}
