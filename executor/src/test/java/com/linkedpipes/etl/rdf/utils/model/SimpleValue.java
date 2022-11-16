package com.linkedpipes.etl.rdf.utils.model;

import java.util.Calendar;

public class SimpleValue implements BackendRdfValue {

    private String value;

    private boolean isIri;

    public SimpleValue(String value, boolean isIri) {
        this.value = value;
        this.isIri = isIri;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public long asLong() {
        return 0;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public Double asDouble() {
        return null;
    }

    @Override
    public Calendar asCalendar() {
        return null;
    }

    @Override
    public boolean isBlankNode() {
        return false;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public boolean isIri() {
        return isIri;
    }

}
