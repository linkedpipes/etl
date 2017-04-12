package com.linkedpipes.etl.rdf.utils.model;

public class SimpleTriple implements RdfTriple {

    private final String subject;

    private final String predicate;

    private final RdfValue object;

    public SimpleTriple(String subject, String predicate, RdfValue object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getPredicate() {
        return predicate;
    }

    @Override
    public RdfValue getObject() {
        return object;
    }

}
