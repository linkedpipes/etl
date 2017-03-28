package com.linkedpipes.plugin.transformer.tabularuv.parser;

import com.linkedpipes.etl.executor.api.v1.LpException;

import java.io.File;

public interface Parser {

    /**
     * Parse given file.
     *
     * @param inFile
     */
    void parse(File inFile) throws LpException, ParseFailed;

}
