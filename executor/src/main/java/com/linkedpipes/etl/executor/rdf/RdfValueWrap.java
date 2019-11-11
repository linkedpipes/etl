package com.linkedpipes.etl.executor.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;

import java.util.Calendar;

public class RdfValueWrap implements RdfValue {

    private final BackendRdfValue value;

    public RdfValueWrap(BackendRdfValue value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return value.asString();
    }

    @Override
    public String getLanguage() {
        return value.getLanguage();
    }

    @Override
    public String getType() {
        return value.getType();
    }

    @Override
    public Boolean asBoolean() throws RdfException {
        try {
            return value.asBoolean();
        } catch (RdfUtilsException ex) {
            throw new RdfException("", ex);
        }
    }

    @Override
    public Long asLong() throws RdfException {
        try {
            return value.asLong();
        } catch (RdfUtilsException ex) {
            throw new RdfException("", ex);
        }
    }

    @Override
    public Double asDouble() throws RdfException {
        try {
            return value.asDouble();
        } catch (RdfUtilsException ex) {
            throw new RdfException("", ex);
        }
    }

    @Override
    public Calendar asCalendar() throws RdfException {
        try {
            return value.asCalendar();
        } catch (RdfUtilsException ex) {
            throw new RdfException("", ex);
        }
    }

    @Override
    public boolean isBlankNode() {
        return value.isBlankNode();
    }

}
