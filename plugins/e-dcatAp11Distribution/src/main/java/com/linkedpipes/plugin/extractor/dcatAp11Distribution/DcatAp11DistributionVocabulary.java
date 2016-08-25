package com.linkedpipes.plugin.extractor.dcatAp11Distribution;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

public final class DcatAp11DistributionVocabulary {

    public static final String SCHEMA = "http://schema.org/";

    public static final String DCAT = "http://www.w3.org/ns/dcat#";

    public static final String VOID = "http://rdfs.org/ns/void#";

    public static final String ADMS = "http://www.w3.org/ns/adms#";

    public static final String SPDX = "http://spdx.org/rdf/terms#";

    public static final String VCARD = "http://www.w3.org/2006/vcard/ns#";

    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final String MY = "http://etl.linkedpipes.com/resource/components/e-dcatAp11Distribution/";

    public static final IRI DCAT_ACCESSURL;

    public static final IRI DCAT_BYTESIZE;

    public static final IRI DCAT_DOWNLOADURL;

    public static final IRI DCAT_MEDIATYPE;

    public static final IRI DCAT_DATASET_CLASS;

    public static final IRI DCAT_DISTRIBUTION_CLASS;

    public static final IRI DCAT_DISTRIBUTION;

    public static final IRI SCHEMA_ENDDATE;

    public static final IRI SCHEMA_STARTDATE;

    public static final IRI XSD_DATE;

    public static final IRI XSD_HEXBINARY;

    public static final IRI XSD_DECIMAL;

    public static final IRI ADMS_STATUS;

    public static final IRI SPDX_CHECKSUM;

    public static final IRI SPDX_CHECKSUM_CLASS;

    public static final IRI SPDX_ALGORITHM;

    public static final IRI SPDX_CHECKSUM_VALUE;

    public static final IRI SPDX_SHA1;

    static {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        DCAT_ACCESSURL = valueFactory.createIRI(DCAT + "accessURL");
        DCAT_DOWNLOADURL = valueFactory.createIRI(DCAT + "downloadURL");
        DCAT_DATASET_CLASS = valueFactory.createIRI(DCAT + "Dataset");
        DCAT_DISTRIBUTION_CLASS = valueFactory.createIRI(DCAT + "Distribution");
        DCAT_DISTRIBUTION = valueFactory.createIRI(DCAT + "distribution");
        DCAT_BYTESIZE = valueFactory.createIRI(DCAT + "byteSize");
        DCAT_MEDIATYPE = valueFactory.createIRI(DCAT + "mediaType");
        XSD_DATE = valueFactory.createIRI(XSD + "date");
        XSD_HEXBINARY = valueFactory.createIRI(XSD + "hexBinary");
        XSD_DECIMAL = valueFactory.createIRI(XSD + "decimal");
        SCHEMA_ENDDATE = valueFactory.createIRI(SCHEMA + "endDate");
        SCHEMA_STARTDATE = valueFactory.createIRI(SCHEMA + "startDate");
        ADMS_STATUS = valueFactory.createIRI(ADMS + "status");
        SPDX_CHECKSUM = valueFactory.createIRI(SPDX + "checksum");
        SPDX_CHECKSUM_CLASS = valueFactory.createIRI(SPDX + "Checksum");
        SPDX_ALGORITHM = valueFactory.createIRI(SPDX + "algorithm");
        SPDX_CHECKSUM_VALUE = valueFactory.createIRI(SPDX + "checksumValue");
        SPDX_SHA1 = valueFactory.createIRI(SPDX + "checksumAlgorithm_sha1");
    }

    private DcatAp11DistributionVocabulary() {
    }

}
