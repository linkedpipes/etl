package com.linkedpipes.plugin.transformer.sparql.constructtofilelist;

public final class SparqlConstructToFileListVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-sparqlConstructToFileList#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String TASK = PREFIX + "Task";

    public static final String HAS_FILE_NAME = PREFIX + "fileName";

    public static final String HAS_FILE_FORMAT = PREFIX + "fileFormat";

    public static final String HAS_TASK_QUERY = PREFIX + "hasTaskQuery";

    public static final String TASK_QUERY = PREFIX + "TaskQuery";

    public static final String HAS_GRAPH_IRI = PREFIX + "outputGraph";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_DEDUPLICATION = PREFIX + "deduplication";

    private SparqlConstructToFileListVocabulary() {

    }

}
