package com.linkedpipes.plugin.extractor.dcatAp11Dataset;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

final class DcatAp11DatasetVocabulary {

    public static final String SCHEMA = "http://schema.org/";

    public static final String DCAT = "http://www.w3.org/ns/dcat#";

    public static final String VOID = "http://rdfs.org/ns/void#";

    public static final String ADMS = "http://www.w3.org/ns/adms#";

    public static final String VCARD = "http://www.w3.org/2006/vcard/ns#";

    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final String OA = "http://www.w3.org/ns/oa#";

    public static final String DQV = "http://www.w3.org/ns/dqv#";

    public static final String QB = "http://purl.org/linked-data/cube#";

    public static final String STAT = "http://data.europa.eu/(xyz)/statdcat-ap/";

    public static final String MY = "http://etl.linkedpipes.com/resource/components/e-dcatAp11Dataset/";

    public static final IRI VCARD_VCARD_CLASS;

    public static final IRI VCARD_HAS_EMAIL;

    public static final IRI VCARD_FN;

    public static final IRI VCARD_KIND_CLASS;

    public static final IRI DCAT_KEYWORD;

    public static final IRI DCAT_LANDING_PAGE;

    public static final IRI DCAT_THEME;

    public static final IRI DCAT_DATASET_CLASS;

    public static final IRI DCAT_CATALOG_CLASS;

    public static final IRI DCAT_DATASET;

    public static final IRI SCHEMA_ENDDATE;

    public static final IRI SCHEMA_STARTDATE;

    public static final IRI DCAT_CONTACT_POINT;

    public static final IRI XSD_DATE;

    public static final IRI XSD_INTEGER;

    public static final IRI ADMS_VERSIONNOTES;

    public static final IRI ADMS_SAMPLE;

    public static final IRI OA_ANNOTATION_CLASS;

    public static final IRI DQV_HASQUALITYANNOTATION;

    public static final IRI QB_ATTRIBUTEPROPERTY_CLASS;

    public static final IRI QB_DIMENSIONPROPERTY_CLASS;

    public static final IRI STAT_NUMSERIES;

    public static final IRI STAT_STATMEASURE;

    public static final IRI STAT_DIMENSION;

    public static final IRI STAT_ATTRIBUTE;

    static {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        DCAT_KEYWORD = valueFactory.createIRI(DCAT + "keyword");
        DCAT_THEME = valueFactory.createIRI(DCAT + "theme");
        DCAT_DATASET_CLASS = valueFactory.createIRI(DCAT + "Dataset");
        DCAT_DATASET = valueFactory.createIRI(DCAT + "dataset");
        DCAT_CATALOG_CLASS = valueFactory.createIRI(DCAT + "Catalog");
        DCAT_CONTACT_POINT = valueFactory.createIRI(DCAT + "contactPoint");
        DCAT_LANDING_PAGE = valueFactory.createIRI(DCAT + "landingPage");
        VCARD_VCARD_CLASS = valueFactory.createIRI(VCARD + "VCard");
        VCARD_HAS_EMAIL = valueFactory.createIRI(VCARD + "hasEmail");
        VCARD_FN = valueFactory.createIRI(VCARD + "fn");
        VCARD_KIND_CLASS = valueFactory.createIRI(VCARD + "Kind");
        XSD_DATE = valueFactory.createIRI(XSD + "date");
        SCHEMA_ENDDATE = valueFactory.createIRI(SCHEMA + "endDate");
        SCHEMA_STARTDATE = valueFactory.createIRI(SCHEMA + "startDate");
        ADMS_VERSIONNOTES = valueFactory.createIRI(ADMS + "versionNotes");
        ADMS_SAMPLE = valueFactory.createIRI(ADMS + "sample");
        OA_ANNOTATION_CLASS = valueFactory.createIRI(OA + "Annotation");
        DQV_HASQUALITYANNOTATION = valueFactory.createIRI(DQV + "hasQualityAnnotation");
        QB_ATTRIBUTEPROPERTY_CLASS = valueFactory.createIRI(QB + "AttributeProperty");
        QB_DIMENSIONPROPERTY_CLASS = valueFactory.createIRI(QB + "DimentsionProperty");
        STAT_NUMSERIES = valueFactory.createIRI(STAT + "numSeries");
        STAT_STATMEASURE = valueFactory.createIRI(STAT + "statMeasure");
        STAT_DIMENSION = valueFactory.createIRI(STAT + "dimension");
        STAT_ATTRIBUTE = valueFactory.createIRI(STAT + "attribute");
        XSD_INTEGER = valueFactory.createIRI(XSD + "integer");
    }

    private DcatAp11DatasetVocabulary() {
    }

}
