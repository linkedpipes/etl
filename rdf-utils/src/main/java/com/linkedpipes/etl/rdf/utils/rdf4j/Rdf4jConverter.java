package com.linkedpipes.etl.rdf.utils.rdf4j;

import com.linkedpipes.etl.rdf.utils.RdfSource;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

class Rdf4jConverter implements RdfSource.ValueConverter<Value> {

    @Override
    public Boolean asBoolean(Value value) {
        if (value instanceof Literal) {
            return ((Literal) value).booleanValue();
        }
        return null;
    }

    @Override
    public Integer asInteger(Value value) {
        if (value instanceof Literal) {
            return ((Literal) value).intValue();
        }
        return null;
    }

    @Override
    public Long asLong(Value value) {
        if (value instanceof Literal) {
            return ((Literal) value).longValue();
        }
        return null;
    }

    @Override
    public Float asFloat(Value value) {
        if (value instanceof Literal) {
            return ((Literal) value).floatValue();
        }
        return null;
    }

    @Override
    public Double asDouble(Value value) {
        if (value instanceof Literal) {
            return ((Literal) value).doubleValue();
        }
        return null;
    }

    @Override
    public String asString(Value value) {
        return value.stringValue();
    }

    @Override
    public String langTag(Value value) {
        if (value instanceof Literal) {
            return ((Literal) value).getLanguage().orElse(null);
        }
        return null;
    }
}
