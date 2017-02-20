package com.linkedpipes.plugin.extractor.datasetMetadata;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class DatasetMetadataVocabulary {

    public static final String SCHEMA = "http://schema.org/";

    public static final String DCAT = "http://www.w3.org/ns/dcat#";

    public static final String VOID = "http://rdfs.org/ns/void#";

    public static final String ADMS = "http://www.w3.org/ns/adms#";

    public static final String VCARD = "http://www.w3.org/2006/vcard/ns#";

    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final IRI VCARD_VCARD_CLASS;

    public static final IRI VCARD_HAS_EMAIL;

    public static final IRI VCARD_FN;

    public static final IRI DCAT_KEYWORD;

    public static final IRI DCAT_LANDING_PAGE;

    public static final IRI DCAT_THEME;

    public static final IRI DCAT_DATASET_CLASS;

    public static final IRI SCHEMA_ENDDATE;

    public static final IRI SCHEMA_STARTDATE;

    public static final IRI DCAT_CONTACT_POINT;

    public static final IRI XSD_DATE;

    static {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        DCAT_KEYWORD = valueFactory.createIRI(DCAT + "keyword");
        DCAT_THEME = valueFactory.createIRI(DCAT + "theme");
        DCAT_DATASET_CLASS = valueFactory.createIRI(DCAT + "Dataset");
        DCAT_CONTACT_POINT = valueFactory.createIRI(DCAT + "contactPoint");
        DCAT_LANDING_PAGE = valueFactory.createIRI(DCAT + "landingPage");
        VCARD_VCARD_CLASS = valueFactory.createIRI(VCARD + "VCard");
        VCARD_HAS_EMAIL = valueFactory.createIRI(VCARD + "hasEmail");
        VCARD_FN = valueFactory.createIRI(VCARD + "fn");
        XSD_DATE = valueFactory.createIRI(XSD + "date");
        SCHEMA_ENDDATE = valueFactory.createIRI(SCHEMA + "endDate");
        SCHEMA_STARTDATE = valueFactory.createIRI(SCHEMA + "startDate");
    }

}
