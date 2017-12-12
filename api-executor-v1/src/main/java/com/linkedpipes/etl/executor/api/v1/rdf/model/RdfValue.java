package com.linkedpipes.etl.executor.api.v1.rdf.model;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;

public interface RdfValue {

    String asString();

    String getLanguage();

    String getType();

    Boolean asBoolean() throws RdfException;

    Long asLong() throws RdfException;

}
