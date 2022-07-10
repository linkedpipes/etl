package com.linkedpipes.etl.library.pipeline.model;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Retention policy for execution data (debug and logs) for given pipeline.
 */
public enum DataRetentionPolicy {
    /**
     * Delete data once there is new finished execution of the pipeline.
     */
    DELETE_ON_NEW("http://linkedpipes.com/resources/KeepLast"),
    /**
     * Delete data once there is new successfully finished execution of
     * the pipeline.
     */
    DELETE_ON_SUCCESSFUL("http://linkedpipes.com/resources/KeepLast"),
    /**
     * Default, no action.
     */
    DEFAULT("http://linkedpipes.com/resources/Keep");

    private final IRI identifier;

    DataRetentionPolicy(String iriAsStr) {
        this.identifier = SimpleValueFactory.getInstance().createIRI(iriAsStr);
    }

    public String asStr() {
        return identifier.stringValue();
    }

    public IRI asIri() {
        return identifier;
    }

    public static DataRetentionPolicy fromIri(String iriAsStr) {
        for (DataRetentionPolicy item : DataRetentionPolicy.values()) {
            if (item.identifier.stringValue().equals(iriAsStr)) {
                return item;
            }
        }
        return DEFAULT;
    }

}
