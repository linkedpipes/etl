package com.linkedpipes.etl.library.io;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Provide conversion between IRI and file on a file system.
 */
public final class FileName {

    public static String asFileName(String iri) {
        String encoded = URLEncoder.encode(iri, StandardCharsets.UTF_8);
        return encoded.replaceAll("%", "&");
    }

    public static String asIri(File file) {
        return asIri(file.getName());
    }

    public static String asIri(String fileName) {
        String replaced = fileName.replaceAll("&", "%").replace(".trig", "");
        return URLDecoder.decode(replaced, StandardCharsets.UTF_8);
    }

}
