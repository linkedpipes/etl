package com.linkedpipes.etl.library.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ResourceToString {

    private ResourceToString() {
    }

    public static String asBase64Short(Resource resource) {
        if (resource instanceof IRI iri) {
            return iri.getLocalName();
        }
        return asBase64Full(resource);
    }

    public static String asBase64Full(Resource resource) {
        byte[] asBytes = resource.stringValue()
                .getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().encodeToString(asBytes);
    }

}
