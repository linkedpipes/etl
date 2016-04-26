package com.linkedpipes.plugin.transformer.tabularuv.parser;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;

/**
 *
 * @author Škoda Petr
 */
public interface Parser {

    /**
     * Parse given file.
     *
     * @param inFile
     * @throws com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException
     * @throws ParseFailed
     */
    void parse(File inFile) throws NonRecoverableException, ParseFailed;

}
