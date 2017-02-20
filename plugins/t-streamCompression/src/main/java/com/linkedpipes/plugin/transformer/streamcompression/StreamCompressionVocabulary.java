package com.linkedpipes.plugin.transformer.streamcompression;

public class StreamCompressionVocabulary {

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/t-streamCompression#";

    public static final String CONFIG_CLASS = PREFIX + "Configuration";

    public static final String HAS_FORMAT = PREFIX + "format";

    public static final String FORMAT_GZIP = PREFIX + "gzip";

    public static final String FORMAT_BZ2 = PREFIX + "bzip2";

}
