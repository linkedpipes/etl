package com.linkedpipes.plugin.extractor.distributionMetadata;

import com.linkedpipes.etl.dpu.api.rdf.RdfToPojo;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

@RdfToPojo.Type(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#Configuration")
public class DistributionMetadataConfig {

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#datasetURI")
    private String datasetURI = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#distributionURI")
    private String distributionURI = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#useDatasetURIfromInput")
    private boolean useDatasetURIfromInput = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#language_orig")
    private String language_orig = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#title_orig")
    private String title_orig = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#title_en")
    private String title_en = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#desc_orig")
    private String desc_orig = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#desc_en")
    private String desc_en = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#license")
    private String license = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#sparqlEndpointUrl")
    private String sparqlEndpointUrl = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#mediaType")
    private String mediaType = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#downloadURL")
    private String downloadURL = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#accessURL")
    private String accessURL = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#exampleResources")
    private Collection<String> exampleResources = new LinkedList<>();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#useNow")
    private boolean useNow = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#modified")
    private Date modified = new Date();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#issued")
    private Date issued = new Date();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#titleFromDataset")
    private boolean titleFromDataset = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#generateDistroURIFromDataset")
    private boolean generateDistroURIFromDataset = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#originalLanguageFromDataset")
    private boolean originalLanguageFromDataset = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#issuedFromDataset")
    private boolean issuedFromDataset = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#descriptionFromDataset")
    private boolean descriptionFromDataset = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#licenseFromDataset")
    private boolean licenseFromDataset = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#schemaFromDataset")
    private boolean schemaFromDataset = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#useTemporal")
    private boolean useTemporal = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#useNowTemporalEnd")
    private boolean useNowTemporalEnd = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#temporalFromDataset")
    private boolean temporalFromDataset = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#temporalEnd")
    private Date temporalEnd = new Date();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#temporalStart")
    private Date temporalStart = new Date();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#schema")
    private String schema = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#schemaType")
    private String schemaType = "";

    public DistributionMetadataConfig() {

    }

    public String getDatasetURI() {
        return datasetURI;
    }

    public void setDatasetURI(String datasetURI) {
        this.datasetURI = datasetURI;
    }

    public String getDistributionURI() {
        return distributionURI;
    }

    public void setDistributionURI(String distributionURI) {
        this.distributionURI = distributionURI;
    }

    public boolean isUseDatasetURIfromInput() {
        return useDatasetURIfromInput;
    }

    public void setUseDatasetURIfromInput(boolean useDatasetURIfromInput) {
        this.useDatasetURIfromInput = useDatasetURIfromInput;
    }

    public String getLanguage_orig() {
        return language_orig;
    }

    public void setLanguage_orig(String language_orig) {
        this.language_orig = language_orig;
    }

    public String getTitle_orig() {
        return title_orig;
    }

    public void setTitle_orig(String title_orig) {
        this.title_orig = title_orig;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getDesc_orig() {
        return desc_orig;
    }

    public void setDesc_orig(String desc_orig) {
        this.desc_orig = desc_orig;
    }

    public String getDesc_en() {
        return desc_en;
    }

    public void setDesc_en(String desc_en) {
        this.desc_en = desc_en;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getSparqlEndpointUrl() {
        return sparqlEndpointUrl;
    }

    public void setSparqlEndpointUrl(String sparqlEndpointUrl) {
        this.sparqlEndpointUrl = sparqlEndpointUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getAccessURL() {
        return accessURL;
    }

    public void setAccessURL(String accessURL) {
        this.accessURL = accessURL;
    }

    public Collection<String> getExampleResources() {
        return exampleResources;
    }

    public void setExampleResources(Collection<String> exampleResources) {
        this.exampleResources = exampleResources;
    }

    public boolean isUseNow() {
        return useNow;
    }

    public void setUseNow(boolean useNow) {
        this.useNow = useNow;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getIssued() {
        return issued;
    }

    public void setIssued(Date issued) {
        this.issued = issued;
    }

    public boolean isTitleFromDataset() {
        return titleFromDataset;
    }

    public void setTitleFromDataset(boolean titleFromDataset) {
        this.titleFromDataset = titleFromDataset;
    }

    public boolean isGenerateDistroURIFromDataset() {
        return generateDistroURIFromDataset;
    }

    public void setGenerateDistroURIFromDataset(boolean generateDistroURIFromDataset) {
        this.generateDistroURIFromDataset = generateDistroURIFromDataset;
    }

    public boolean isOriginalLanguageFromDataset() {
        return originalLanguageFromDataset;
    }

    public void setOriginalLanguageFromDataset(boolean originalLanguageFromDataset) {
        this.originalLanguageFromDataset = originalLanguageFromDataset;
    }

    public boolean isIssuedFromDataset() {
        return issuedFromDataset;
    }

    public void setIssuedFromDataset(boolean issuedFromDataset) {
        this.issuedFromDataset = issuedFromDataset;
    }

    public boolean isDescriptionFromDataset() {
        return descriptionFromDataset;
    }

    public void setDescriptionFromDataset(boolean descriptionFromDataset) {
        this.descriptionFromDataset = descriptionFromDataset;
    }

    public boolean isLicenseFromDataset() {
        return licenseFromDataset;
    }

    public void setLicenseFromDataset(boolean licenseFromDataset) {
        this.licenseFromDataset = licenseFromDataset;
    }

    public boolean isSchemaFromDataset() {
        return schemaFromDataset;
    }

    public void setSchemaFromDataset(boolean schemaFromDataset) {
        this.schemaFromDataset = schemaFromDataset;
    }

    public boolean isUseTemporal() {
        return useTemporal;
    }

    public void setUseTemporal(boolean useTemporal) {
        this.useTemporal = useTemporal;
    }

    public boolean isUseNowTemporalEnd() {
        return useNowTemporalEnd;
    }

    public void setUseNowTemporalEnd(boolean useNowTemporalEnd) {
        this.useNowTemporalEnd = useNowTemporalEnd;
    }

    public boolean isTemporalFromDataset() {
        return temporalFromDataset;
    }

    public void setTemporalFromDataset(boolean temporalFromDataset) {
        this.temporalFromDataset = temporalFromDataset;
    }

    public Date getTemporalEnd() {
        return temporalEnd;
    }

    public void setTemporalEnd(Date temporalEnd) {
        this.temporalEnd = temporalEnd;
    }

    public Date getTemporalStart() {
        return temporalStart;
    }

    public void setTemporalStart(Date temporalStart) {
        this.temporalStart = temporalStart;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(String schemaType) {
        this.schemaType = schemaType;
    }

}
