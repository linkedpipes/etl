package com.linkedpipes.plugin.transformer.tabular;

import org.openrdf.model.IRI;

/**
 * TODO Implement.
 *
 * @author Petr Škoda
 */
abstract class ColumnList extends ColumnTyped {

    private String separator;

    public ColumnList(String separator, IRI type, String language, String name, boolean required,
            ResourceTemplate aboutUrl, UrlTemplate predicate) {
        super(type, language, name, required, aboutUrl, predicate);
        this.separator = separator;
    }

}
