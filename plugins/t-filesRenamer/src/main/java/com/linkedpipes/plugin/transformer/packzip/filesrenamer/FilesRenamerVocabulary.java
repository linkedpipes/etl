package com.linkedpipes.plugin.transformer.packzip.filesrenamer;

/**
 *
 * @author Petr Škoda
 */
final class FilesRenamerVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-filesRenamer#";

    public static final String CONFIGURATION = PREFIX + "Configuration";

    public static final String HAS_PATTERN = PREFIX + "pattern";

    public static final String HAS_REPLACE_WITH = PREFIX + "replaceWith";

    private FilesRenamerVocabulary() {
    }

}
