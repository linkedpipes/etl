package com.linkedpipes.plugin.transformer.tabularuv;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class TabularOntology {

    private TabularOntology() {

    }

    public static final IRI BLANK_CELL;

    public static final IRI ROW_NUMBER;

    public static final IRI RDF_ROW_LABEL;

    public static final IRI TABLE_HAS_ROW;

    public static final IRI TABLE_SYMBOLIC_NAME;

    public static final IRI TABLE_CLASS;

    public static final IRI ROW_CLASS;

    static {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        BLANK_CELL = valueFactory.createIRI(
                "http://linked.opendata.cz/ontology/odcs/tabular/blank-cell");
        ROW_NUMBER = valueFactory.createIRI(
                "http://linked.opendata.cz/ontology/odcs/tabular/row");
        RDF_ROW_LABEL = valueFactory.createIRI(
                "http://www.w3.org/2000/01/rdf-schema#label");
        TABLE_HAS_ROW = valueFactory.createIRI(
                "http://linked.opendata.cz/ontology/odcs/tabular/hasRow");
        TABLE_SYMBOLIC_NAME = valueFactory.createIRI(
                "http://linked.opendata.cz/ontology/odcs/tabular/symbolicName");
        TABLE_CLASS = valueFactory.createIRI(
                "http://unifiedviews.eu/ontology/t-tabular/Table");
        ROW_CLASS = valueFactory.createIRI(
                "http://unifiedviews.eu/ontology/t-tabular/Row");
    }

}
