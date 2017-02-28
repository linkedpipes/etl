package com.linkedpipes.plugin.loader.lodCloud;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class LodCloudVocabulary {

    public static final String SCHEMA = "http://schema.org/";

    public static final String VOID = "http://rdfs.org/ns/void#";

    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final String VCARD = "http://www.w3.org/2006/vcard/ns#";

    public static final String CKAN = "http://etl.linkedpipes.com/ontology/ckan/";

    public static final IRI VOID_DATASET_CLASS;

    public static final IRI VOID_EXAMPLERESOURCE;

    public static final IRI VOID_TRIPLES;

    public static final IRI VOID_DATADUMP;

    public static final IRI VOID_SPARQLENDPOINT;

    public static final IRI SCHEMA_ENDDATE;

    public static final IRI SCHEMA_STARTDATE;

    public static final IRI XSD_DATE;

    public static final IRI VCARD_VCARD_CLASS;

    public static final IRI VCARD_HAS_EMAIL;

    public static final IRI VCARD_FN;

    public static final IRI CKAN_DATASET_ID;

    static {
        final SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

        VOID_DATASET_CLASS = valueFactory.createIRI(VOID + "Dataset");
        VOID_EXAMPLERESOURCE = valueFactory.createIRI(VOID + "exampleResource");
        VOID_DATADUMP = valueFactory.createIRI(VOID + "dataDump");
        VOID_SPARQLENDPOINT = valueFactory.createIRI(VOID + "sparqlEndpoint");
        VOID_TRIPLES = valueFactory.createIRI(VOID + "triples");
        XSD_DATE = valueFactory.createIRI(XSD + "date");
        SCHEMA_ENDDATE = valueFactory.createIRI(SCHEMA + "endDate");
        SCHEMA_STARTDATE = valueFactory.createIRI(SCHEMA + "startDate");
        VCARD_VCARD_CLASS = valueFactory.createIRI(VCARD + "VCard");
        VCARD_HAS_EMAIL = valueFactory.createIRI(VCARD + "hasEmail");
        VCARD_FN = valueFactory.createIRI(VCARD + "fn");
        CKAN_DATASET_ID = valueFactory.createIRI(CKAN + "datasetID");
    }

}
