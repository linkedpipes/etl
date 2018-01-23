package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

class Rdf4jValueWrap implements RdfValue {

    private final Value value;

    public Rdf4jValueWrap(Value value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return value.stringValue();
    }

    @Override
    public String getLanguage() {
        if (value instanceof  Literal) {
            return ((Literal) value).getLanguage().orElseGet(() -> null);
        }
        return null;
    }

    @Override
    public String getType() {
        if (value instanceof  Literal) {
            return ((Literal) value).getDatatype().stringValue();
        }
        return null;
    }

    @Override
    public Boolean asBoolean() throws RdfException {
        if (value instanceof  Literal) {
            return ((Literal) value).booleanValue();
        }
        return null;
    }

    @Override
    public Long asLong() throws RdfException {
        if (value instanceof  Literal) {
            return ((Literal) value).longValue();
        }
        return null;
    }

}
