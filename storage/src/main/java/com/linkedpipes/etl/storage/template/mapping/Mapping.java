package com.linkedpipes.etl.storage.template.mapping;

import com.linkedpipes.etl.storage.template.Template;

import java.util.Map;

/**
 * Provide access to mapping stored on instance and also loaded.
 * Any change to instance mapping is immediately reflected in this class.
 */
public class Mapping {

    /**
     * Imported mappings, remote to original. Used to get reference
     * from remote to original after import.
     */
    private final Map<String, String> remoteToOriginal;

    /**
     * Resolved mapping, ie. from remote to local. New mapping
     * can be added during import.
     */
    private final Map<String, String> remoteToLocal;

    private final MappingFacade mappingFacade;

    public Mapping(Map<String, String> remoteToOriginal,
                   Map<String, String> remoteToLocal,
                   MappingFacade mappingFacade) {
        this.remoteToOriginal = remoteToOriginal;
        this.remoteToLocal = remoteToLocal;
        this.mappingFacade = mappingFacade;
    }

    /**
     * Try to resolve local IRI for given remote IRI.
     */
    public String remoteToLocal(String iri) {
        String local = remoteToLocal.get(iri);
        if (local == null) {
            // This can happen if the pipeline is exported from the original
            // server, as such the server do not include mappings. That is
            // why it is not included in remoteToLocal, but we need
            // to use the IRI directly as the "original" IRI.
            local = mappingFacade.originalToLocal(iri);
        }
        return local;

    }

    /**
     * Add temporary mapping.
     */
    public void onImport(Template local, String remote) {
        remoteToLocal.put(remote, local.getIri());
        String original = remoteToOriginal.get(remote);
        if (original == null) {
            // There is no link to original, so original must be the source.
            original = remote;
        }
        mappingFacade.add(local, original);
    }

}
