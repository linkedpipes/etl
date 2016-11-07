package com.linkedpipes.plugin.transformer.tabular;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Holds vocabulary for CSV on the Web.
 *
 */
public class CSVW {

    public static final IRI TABLE_GROUP;

    public static final IRI TABLE;

    public static final IRI HAS_TABLE;

    public static final IRI HAS_URL;

    public static final IRI ROW;

    public static final IRI HAS_ROW;

    public static final IRI HAS_ROWNUM;

    public static final IRI HAS_TITLE;

    public static final IRI HAS_DESCRIBES;

    static {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final String prefix = "http://www.w3.org/ns/csvw#";
        TABLE_GROUP = valueFactory.createIRI(prefix + "TableGroup");
        TABLE = valueFactory.createIRI(prefix + "Table");
        HAS_TABLE = valueFactory.createIRI(prefix + "table");
        HAS_URL = valueFactory.createIRI(prefix + "url");
        ROW = valueFactory.createIRI(prefix + "Row");
        HAS_ROW = valueFactory.createIRI(prefix + "row");
        HAS_ROWNUM = valueFactory.createIRI(prefix + "rownum");
        HAS_TITLE = valueFactory.createIRI(prefix + "title");
        HAS_DESCRIBES = valueFactory.createIRI(prefix + "describes");
    }

    private CSVW() {
    }

}
