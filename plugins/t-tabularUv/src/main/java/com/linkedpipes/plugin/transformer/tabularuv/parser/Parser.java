package com.linkedpipes.plugin.transformer.tabularuv.parser;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
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
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     * @throws ParseFailed
     */
    void parse(File inFile) throws LpException, ParseFailed;

}
