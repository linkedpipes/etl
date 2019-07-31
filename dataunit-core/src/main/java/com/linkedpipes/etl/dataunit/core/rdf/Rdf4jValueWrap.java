package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.SuppressFBWarnings;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.util.Calendar;

class Rdf4jValueWrap implements RdfValue {

    private final Value value;

    public Rdf4jValueWrap(Value value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return this.value.stringValue();
    }

    @Override
    public String getLanguage() {
        if (this.value instanceof Literal) {
            return ((Literal) this.value).getLanguage().orElse(null);
        }
        return null;
    }

    @Override
    public String getType() {
        if (this.value instanceof Literal) {
            return ((Literal) this.value).getDatatype().stringValue();
        }
        return null;
    }

    @Override
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL")
    public Boolean asBoolean() {
        if (this.value instanceof Literal) {
            return ((Literal) this.value).booleanValue();
        }
        return null;
    }

    @Override
    public Long asLong() {
        if (this.value instanceof Literal) {
            return ((Literal) this.value).longValue();
        }
        return null;
    }

    @Override
    public Double asDouble() {
        if (this.value instanceof Literal) {
            return ((Literal) this.value).doubleValue();
        }
        return null;
    }

    @Override
    public Calendar asCalendar() throws RdfException {
        if (this.value instanceof Literal) {
            return ((Literal) this.value).calendarValue().toGregorianCalendar();
        }
        return null;
    }

    @Override
    public boolean isBlankNode() {
        return this.value instanceof BNode;
    }

}
