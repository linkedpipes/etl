package com.linkedpipes.etl.rdf.utils.model;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

public class SimpleValue implements RdfValue {

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
    public long asLong() throws RdfUtilsException {
        return 0;
    }

    @Override
    public boolean asBoolean() throws RdfUtilsException {
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
