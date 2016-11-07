package com.linkedpipes.plugin.extractor.ftpfiles;

/**
 *
 * @author Škoda Petr
 */
public final class FtpFilesVocabulary {

    private FtpFilesVocabulary() {
    }

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-ftpFiles#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_REFERENCE = PREFIX + "reference";

    public static final String REFERENCE = PREFIX + "Reference";

    public static final String HAS_URI = PREFIX + "fileUri";

    public static final String HAS_NAME = PREFIX + "fileName";

    public static final String HAS_PASSIVE_MODE = PREFIX + "passiveMode";

    public static final String HAS_BINARY_MODE = PREFIX + "binaryMode";

    public static final String HAS_KEEP_ALIVE_CONTROL
            = PREFIX + "keepAliveControl";

}
