package com.linkedpipes.etl.executor.api.v1.vocabulary;

public final class LP_PIPELINE {

    private LP_PIPELINE() {

    }

    private static final String PREFIX = "http://linkedpipes.com/ontology/";

    public static final String PIPELINE = PREFIX + "Pipeline";

    public static final String COMPONENT = PREFIX + "Component";

    public static final String CONNECTION = PREFIX + "Connection";

    public static final String CONFIGURATION = PREFIX + "Configuration";

    public static final String JAS_TEMPLATE = PREFIX + "JarTemplate";

    /**
     * TODO Update to ../dataUnit/files/DirectoryMirror
     */
    public static final String FILE_DATA_UNIT =
            PREFIX + "dataUnit/system/1.0/files/DirectoryMirror";

    /**
     * TODO Update to rdf4j
     */
    public static final String SINGLE_GRAPH_DATA_UNIT =
            PREFIX + "dataUnit/sesame/1.0/rdf/SingleGraph";

    /**
     * TODO Update to rdf4j
     */
    public static final String GRAPH_LIST_DATA_UNIT =
            PREFIX + "dataUnit/sesame/1.0/rdf/GraphList";

    public static final String CHUNKED_TRIPLES_DATA_UNIT =
            PREFIX + "dataUnit/sesame/1.0/rdf/Chunked";

    public static final String RDF_REPOSITORY =
            "http://linkedpipes.com/ontology/dataUnit/sesame/1.0/Repository";

    /**
     * Define requirement for working directory.
     */
    public static final String WORKING_DIRECTORY =
            "http://linkedpipes.com/resources/requirement/workingDirectory";

    /**
     * Define requirement for input directory.
     */
    public static final String INPUT_DIRECTORY =
            "http://linkedpipes.com/ontology/requirements/InputDirectory";

    /**
     * Input port.
     */
    public static final String INPUT = "http://linkedpipes.com/ontology/Input";

    /**
     * Output port.
     */
    public static final String OUTPUT =
            "http://linkedpipes.com/ontology/Output";

    /**
     * Pipeline has a component.
     */
    public static final String HAS_COMPONENT = PREFIX + "component";

    /**
     * Pipeline has connections between components.
     */
    public static final String HAS_CONNECTION = PREFIX + "connection";

    /**
     * Port binding.
     */
    public static final String HAS_BINDING = PREFIX + "binding";

    /**
     * Component has a data unit.
     */
    public static final String HAS_DATA_UNIT = PREFIX + "port";

    /**
     * Connection has a source component.
     */
    public static final String HAS_SOURCE_COMPONENT =
            PREFIX + "sourceComponent";

    /**
     * Connection has a source binding.
     */
    public static final String HAS_SOURCE_BINDING = PREFIX + "sourceBinding";

    /**
     * Connection has a target component.
     */
    public static final String HAS_TARGET_COMPONENT =
            PREFIX + "targetComponent";

    /**
     * Connection has a target binding.
     */
    public static final String HAS_TARGET_BINDING = PREFIX + "targetBinding";

    /**
     * Pipeline may have a repository object.
     */
    public static final String HAS_REPOSITORY = PREFIX + "repository";

    /**
     *
     */
    public static final String HAS_REQUIREMENT = PREFIX + "requirement";

    /**
     * Path to JAR of given component.
     */
    public static final String HAS_JAR_URL =
            "http://linkedpipes.com/ontology/jar";

    /**
     * Reference to a graph with a configuration, used to reference
     * configuration by frontend.
     *
     * TODO: Change to lp:configurationGraph
     */
    public static final String HAS_CONFIGURATION_GRAPH =
            "http://linkedpipes.com/ontology/configuration/graph";

    /**
     * Component has a template.
     */
    public static final String HAS_TEMPLATE = PREFIX + "template";

    /**
     * Component has types of it's configuration class instances.
     */
    public static final String HAS_CONFIGURATION_ENTITY_DESCRIPTION =
            PREFIX + "configurationDescription";

}
