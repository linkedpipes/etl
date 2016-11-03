package com.linkedpipes.etl.storage.mapping;

/**
 * Provide access to mapping stored on instance and also loaded.
 *
 * Any change to instance mapping is immediately reflected in this class.
 */
public interface Mapping {

    /**
     * Given the component IRI return IRI of corresponding template
     * on local instance. Can be used to map remote template to local.
     *
     * @param iri
     * @return Given IRI if there is no mapping.
     */
    public String map(String iri);

    /**
     * For given component return its original IRI. Can be used to set
     * original IRI for newly imported templates.
     *
     * @param iri
     * @return Given IRI if there is no record for given IRI.
     */
    public String original(String iri);

}
