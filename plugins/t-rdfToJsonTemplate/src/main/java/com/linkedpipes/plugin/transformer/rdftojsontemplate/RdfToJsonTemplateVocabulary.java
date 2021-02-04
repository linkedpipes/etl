package com.linkedpipes.plugin.transformer.rdftojsontemplate;

public final class RdfToJsonTemplateVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-rdfToJsonTemplate#";

    public static final String CONFIGURATION = PREFIX + "Configuration";

    public static final String MAPPING = PREFIX + "simpleMapping";

    public static final String MULTIPLE_PRIMITIVES =
            PREFIX + "ignoreMultiplePrimitives";

    private RdfToJsonTemplateVocabulary() {
    }

}
