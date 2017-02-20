package com.linkedpipes.plugin.transformer.tabularuv.parser;

public class ParserDbfConfig {

    final String encoding;

    final Integer rowLimit;

    final boolean checkStaticRowCounter;

    public ParserDbfConfig(String encoding, Integer rowLimit,
            boolean checkStaticRowCounter) {
        this.encoding = encoding;
        this.rowLimit = rowLimit;
        this.checkStaticRowCounter = checkStaticRowCounter;
    }

}
