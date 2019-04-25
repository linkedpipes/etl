package com.linkedpipes.etl.rdf4j;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.Date;

public class StatementsBuilder {

    private final Resource subject;

    private final Statements statements;

    StatementsBuilder(Resource subject, Statements statements) {
        this.subject = subject;
        this.statements = statements;
    }

    public StatementsBuilder addIri(String p, String o) {
        statements.addIri(subject, p, o);
        return this;
    }

    public StatementsBuilder addIri(IRI p, String o) {
        statements.addIri(subject, p, o);
        return this;
    }

    public StatementsBuilder addString(String p, String o) {
        statements.addString(subject, p, o);
        return this;
    }

    public StatementsBuilder addString(IRI p, String o) {
        statements.addString(subject, p, o);
        return this;
    }

    public StatementsBuilder addInt(String p, int o) {
        statements.addInt(subject, p, o);
        return this;
    }

    public StatementsBuilder addInt(IRI p, int o) {
        statements.addInt(subject, p, o);
        return this;
    }

    public StatementsBuilder addBoolean(String p, boolean o) {
        statements.addBoolean(subject, p, o);
        return this;
    }

    public StatementsBuilder addBoolean(IRI p, boolean o) {
        statements.addBoolean(subject, p, o);
        return this;
    }

    public StatementsBuilder addDate(String p, Date o) {
        statements.addDate(subject, p, o);
        return this;
    }

    public StatementsBuilder addDate(IRI p, Date o) {
        statements.addDate(subject, p, o);
        return this;
    }

    public StatementsBuilder addLong(String p, Long o) {
        statements.addLong(subject, p, o);
        return this;
    }

    public StatementsBuilder add(String p, Value o) {
        statements.add(subject, p, o);
        return this;
    }

    public StatementsBuilder add(IRI p, Value o) {
        statements.add(subject, p, o);
        return this;
    }

}
