package com.linkedpipes.plugin.transformer.tabular;

/**
 *
 * @author Petr Škoda
 */
class MissingNameInHeader extends Exception {

    public MissingNameInHeader(String name) {
        super("Missing column with name '" + name + "'.");
    }

}
