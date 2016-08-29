package com.linkedpipes.plugin.extractor.distributionMetadata;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

public final class DistributionMetadataVocabulary {

    public static final String SCHEMA = "http://schema.org/";

    public static final String DCAT = "http://www.w3.org/ns/dcat#";

    public static final String VOID = "http://rdfs.org/ns/void#";

    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final String POD = "https://project-open-data.cio.gov/v1.1/schema/#";

    public static final String WDRS = "http://www.w3.org/2007/05/powder-s#";

    public static final IRI DCAT_DISTRIBUTION_CLASS;

    public static final IRI DCAT_DATASET_CLASS;

    public static final IRI DCAT_DISTRIBUTION;

    public static final IRI DCAT_DOWNLOADURL;

    public static final IRI DCAT_ACCESSURL;

    public static final IRI DCAT_MEDIATYPE;

    public static final IRI VOID_DATASET_CLASS;

    public static final IRI VOID_EXAMPLERESOURCE;

    public static final IRI VOID_DATADUMP;

    public static final IRI VOID_SPARQLENDPOINT;

    public static final IRI SCHEMA_ENDDATE;

    public static final IRI SCHEMA_STARTDATE;

    public static final IRI POD_DISTRIBUTION_DESCRIBREBYTYPE;

    public static final IRI WDRS_DESCRIBEDBY;

    public static final IRI XSD_DATE;

    static {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        DCAT_DISTRIBUTION_CLASS = valueFactory.createIRI(DCAT + "Distribution");
        DCAT_DATASET_CLASS = valueFactory.createIRI(DCAT + "Dataset");
        DCAT_DISTRIBUTION = valueFactory.createIRI(DCAT + "distribution");
        DCAT_DOWNLOADURL = valueFactory.createIRI(DCAT + "downloadURL");
        DCAT_ACCESSURL = valueFactory.createIRI(DCAT + "accessURL");
        DCAT_MEDIATYPE = valueFactory.createIRI(DCAT + "mediaType");
        VOID_DATASET_CLASS = valueFactory.createIRI(VOID + "Dataset");
        VOID_EXAMPLERESOURCE = valueFactory.createIRI(VOID + "exampleResource");
        VOID_DATADUMP = valueFactory.createIRI(VOID + "dataDump");
        VOID_SPARQLENDPOINT = valueFactory.createIRI(VOID + "sparqlEndpoint");
        XSD_DATE = valueFactory.createIRI(XSD + "date");
        SCHEMA_ENDDATE = valueFactory.createIRI(SCHEMA + "endDate");
        SCHEMA_STARTDATE = valueFactory.createIRI(SCHEMA + "startDate");
        POD_DISTRIBUTION_DESCRIBREBYTYPE = valueFactory.createIRI(POD + "distribution-describedByType");
        WDRS_DESCRIBEDBY = valueFactory.createIRI(WDRS + "describedBy");
    }

}
