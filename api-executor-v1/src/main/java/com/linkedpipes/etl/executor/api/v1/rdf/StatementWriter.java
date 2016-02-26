package com.linkedpipes.etl.executor.api.v1.rdf;

/**
 * Used to convert events into RDF statements.
 *
 * @author Škoda Petr
 */
public interface StatementWriter {

    public void add(String subject, String predicate, String object, String type);

    public void addUri(String subject, String predicate, String object);

    /**
     *
     * @param subject
     * @param predicate
     * @param object
     * @param language If null no information about language is stored.
     */
    public void addString(String subject, String predicate, String object, String language);

}
