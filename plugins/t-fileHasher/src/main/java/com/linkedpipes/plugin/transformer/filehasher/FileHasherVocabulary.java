package com.linkedpipes.plugin.transformer.filehasher;

/**
 *
 * @author Petr Škoda
 */
public final class FileHasherVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-filehash#";

    static final String HAS_FILE_NAME = PREFIX + "fileName";

    private static final String SPDX
            = "http://spdx.org/rdf/terms#";

    static final String CHECKSUM = SPDX + "Checksum";

    static final String HAS_CHECKSUM = SPDX + "checksum";

    static final String HAS_ALGORITHM = SPDX + "algorithm";

    static final String HAS_CHECKSUM_VALUE = SPDX + "checksumValue";

    static final String SHA1 = SPDX + "checksumAlgorithm_sha1";

    private FileHasherVocabulary() {
    }

}
