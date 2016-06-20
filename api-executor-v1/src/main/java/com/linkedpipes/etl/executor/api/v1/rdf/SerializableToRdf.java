package com.linkedpipes.etl.executor.api.v1.rdf;

/**
 * Interface for object that can be converted to RDF.
 *
 * @author Petr Škoda
 */
public interface SerializableToRdf {

    public interface Writer {

        public void add(String subject, String predicate, String object,
                String type);

        public void addUri(String subject, String predicate, String object);

        /**
         *
         * @param subject
         * @param predicate
         * @param object
         * @param language If null no information about language is stored.
         */
        public void addString(String subject, String predicate, String object,
                String language);

    }

    /**
     * Set event IRI.
     *
     * @param iri
     */
    public void setResource(String iri);

    /**
     * The IRI that should be referenced from execution. Is not
     * called before {@link #setResource(java.lang.String)}.
     *
     * @return Resource IRI.
     */
    public String getResource();

    void serialize(Writer writer);

}
