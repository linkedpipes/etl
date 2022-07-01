package com.linkedpipes.etl.executor.api.v1.rdf.model;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;

import java.util.Calendar;

public interface RdfValue {

    String asString();

    String getLanguage();

    String getType();

    Boolean asBoolean() throws RdfException;

    Long asLong() throws RdfException;

    Double asDouble() throws RdfException;

    Calendar asCalendar() throws RdfException;

    boolean isBlankNode();

}
