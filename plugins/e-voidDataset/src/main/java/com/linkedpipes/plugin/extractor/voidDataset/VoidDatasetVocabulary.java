package com.linkedpipes.plugin.extractor.voidDataset;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

public final class VoidDatasetVocabulary {

    public static final String DCAT = "http://www.w3.org/ns/dcat#";

    public static final String VOID = "http://rdfs.org/ns/void#";

    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final String MY = "http://etl.linkedpipes.com/ontology/components/e-voidDataset/";

    public static final IRI DCAT_DOWNLOADURL;

    public static final IRI DCAT_DISTRIBUTION_CLASS;

    public static final IRI XSD_DATE;

    public static final IRI XSD_HEXBINARY;

    public static final IRI XSD_DECIMAL;

    public static final IRI VOID_DATASET_CLASS;

    public static final IRI VOID_EXAMPLE_RESOURCE;

    public static final IRI VOID_SPARQL_ENDPOINT;

    public static final IRI VOID_DATA_DUMP;

    static {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        DCAT_DOWNLOADURL = valueFactory.createIRI(DCAT + "downloadURL");
        DCAT_DISTRIBUTION_CLASS = valueFactory.createIRI(DCAT + "Distribution");
        XSD_DATE = valueFactory.createIRI(XSD + "date");
        XSD_HEXBINARY = valueFactory.createIRI(XSD + "hexBinary");
        XSD_DECIMAL = valueFactory.createIRI(XSD + "decimal");
        VOID_DATASET_CLASS = valueFactory.createIRI(VOID + "Dataset");
        VOID_EXAMPLE_RESOURCE = valueFactory.createIRI(VOID + "exampleResource");
        VOID_SPARQL_ENDPOINT = valueFactory.createIRI(VOID + "sparqlEndpoint");
        VOID_DATA_DUMP = valueFactory.createIRI(VOID + "dataDump");
    }

    private VoidDatasetVocabulary() {
    }

}
