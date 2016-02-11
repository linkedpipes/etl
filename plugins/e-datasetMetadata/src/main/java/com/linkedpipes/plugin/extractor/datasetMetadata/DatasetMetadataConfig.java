package com.linkedpipes.plugin.extractor.datasetMetadata;

import com.linkedpipes.etl.dpu.api.rdf.RdfToPojo;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

@RdfToPojo.Type(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#Configuration")
public class DatasetMetadataConfig {

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#datasetURI")
    private String datasetURI = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#language_orig")
    private String language_orig = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#title_cs")
    private String title_cs = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#title_en")
    private String title_en = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#desc_cs")
    private String desc_cs = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#desc_en")
    private String desc_en = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#authors")
    private Collection<String> authors = new LinkedList<>();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#publisherURI")
    private String publisherURI = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#publisherName")
    private String publisherName = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#license")
    private String license = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#sources")
    private Collection<String> sources = new LinkedList<>();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#languages")
    private Collection<String> languages = new LinkedList<>();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#keywords_orig")
    private Collection<String> keywords_orig = new LinkedList<>();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#keywords_en")
    private Collection<String> keywords_en = new LinkedList<>();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#themes")
    private Collection<String> themes = new LinkedList<>();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#contactPoint")
    private String contactPoint = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#contactPointName")
    private String contactPointName = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#periodicity")
    private String periodicity = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#useNow")
    private boolean useNow = true;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#useNowTemporalEnd")
    private boolean useNowTemporalEnd = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#useTemporal")
    private boolean useTemporal = true;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#modified")
    private Date modified = new Date();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#issued")
    private Date issued = new Date();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#identifier")
    private String identifier = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#landingPage")
    private String landingPage = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#temporalEnd")
    private Date temporalEnd = new Date();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#temporalStart")
    private Date temporalStart = new Date();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#spatial")
    private String spatial = "";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#schema")
    private String schema = "";

    public DatasetMetadataConfig() {

    }

    public String getDatasetURI() {
        return datasetURI;
    }

    public void setDatasetURI(String datasetURI) {
        this.datasetURI = datasetURI;
    }

    public String getLanguage_orig() {
        return language_orig;
    }

    public void setLanguage_orig(String language_orig) {
        this.language_orig = language_orig;
    }

    public String getTitle_cs() {
        return title_cs;
    }

    public void setTitle_cs(String title_cs) {
        this.title_cs = title_cs;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getDesc_cs() {
        return desc_cs;
    }

    public void setDesc_cs(String desc_cs) {
        this.desc_cs = desc_cs;
    }

    public String getDesc_en() {
        return desc_en;
    }

    public void setDesc_en(String desc_en) {
        this.desc_en = desc_en;
    }

    public Collection<String> getAuthors() {
        return authors;
    }

    public void setAuthors(Collection<String> authors) {
        this.authors = authors;
    }

    public String getPublisherURI() {
        return publisherURI;
    }

    public void setPublisherURI(String publisherURI) {
        this.publisherURI = publisherURI;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Collection<String> getSources() {
        return sources;
    }

    public void setSources(Collection<String> sources) {
        this.sources = sources;
    }

    public Collection<String> getLanguages() {
        return languages;
    }

    public void setLanguages(Collection<String> languages) {
        this.languages = languages;
    }

    public Collection<String> getKeywords_orig() {
        return keywords_orig;
    }

    public void setKeywords_orig(Collection<String> keywords_orig) {
        this.keywords_orig = keywords_orig;
    }

    public Collection<String> getKeywords_en() {
        return keywords_en;
    }

    public void setKeywords_en(Collection<String> keywords_en) {
        this.keywords_en = keywords_en;
    }

    public Collection<String> getThemes() {
        return themes;
    }

    public void setThemes(Collection<String> themes) {
        this.themes = themes;
    }

    public String getContactPoint() {
        return contactPoint;
    }

    public void setContactPoint(String contactPoint) {
        this.contactPoint = contactPoint;
    }

    public String getContactPointName() {
        return contactPointName;
    }

    public void setContactPointName(String contactPointName) {
        this.contactPointName = contactPointName;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public boolean isUseNow() {
        return useNow;
    }

    public void setUseNow(boolean useNow) {
        this.useNow = useNow;
    }

    public boolean isUseNowTemporalEnd() {
        return useNowTemporalEnd;
    }

    public void setUseNowTemporalEnd(boolean useNowTemporalEnd) {
        this.useNowTemporalEnd = useNowTemporalEnd;
    }

    public boolean isUseTemporal() {
        return useTemporal;
    }

    public void setUseTemporal(boolean useTemporal) {
        this.useTemporal = useTemporal;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getLandingPage() {
        return landingPage;
    }

    public void setLandingPage(String landingPage) {
        this.landingPage = landingPage;
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

    public String getSpatial() {
        return spatial;
    }

    public void setSpatial(String spatial) {
        this.spatial = spatial;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

}
