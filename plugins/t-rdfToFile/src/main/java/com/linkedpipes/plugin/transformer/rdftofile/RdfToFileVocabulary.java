package com.linkedpipes.plugin.transformer.rdftofile;

/**
 *
 * @author Škoda Petr
 */
final class RdfToFileVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-rdfToFile#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_FILE_NAME = PREFIX + "fileName";

    public static final String HAS_FILE_TYPE = PREFIX + "fileType";

    public static final String HAS_GRAPH_URI = PREFIX + "graphUri";

    private RdfToFileVocabulary() {
    }

}
