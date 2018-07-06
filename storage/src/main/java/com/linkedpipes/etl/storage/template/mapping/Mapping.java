package com.linkedpipes.etl.storage.template.mapping;

import java.util.Map;

/**
 * Provide access to mapping stored on instance and also loaded.
 * <p>
 * Any change to instance mapping is immediately reflected in this class.
 */
public class Mapping {

    /**
     * Imported mapping.
     */
    private Map<String, String> remoteToOriginal;

    /**
     * Imported mapping.
     */
    private Map<String, String> remoteToLocal;

    /**
     * Our local mapping.
     */
    private Map<String, String> originalToLocal;

    public Mapping(Map<String, String> remoteToOriginal,
                   Map<String, String> remoteToLocal,
                   Map<String, String> originalToLocal) {
        this.remoteToOriginal = remoteToOriginal;
        this.remoteToLocal = remoteToLocal;
        this.originalToLocal = originalToLocal;
    }

    /**
     * Try to resolve local IRI for given remote IRI.
     */
    public String toLocal(String iri) {
        String localMapping = originalToLocal.get(iri);
        if (localMapping != null) {
            return localMapping;
        }
        // Check additional mappings (from the pipeline).
        localMapping = remoteToLocal.get(iri);
        if (localMapping != null) {
            return localMapping;
        }
        // The mapping could be newly added during pipeline import
        // we need to check for original.
        String original = remoteToOriginal.getOrDefault(iri, iri);
        return originalToLocal.getOrDefault(original, original);
    }

    /**
     * For given component return its original IRI. Can be used to set
     * original IRI for newly imported templates.
     */
    public String toOriginal(String iri) {
        // TODO Add information from toLocal ?
        return remoteToOriginal.getOrDefault(iri, iri);
    }

}
