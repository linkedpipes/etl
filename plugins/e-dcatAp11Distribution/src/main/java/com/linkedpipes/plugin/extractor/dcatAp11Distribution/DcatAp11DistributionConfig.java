package com.linkedpipes.plugin.extractor.dcatAp11Distribution;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(iri = DcatAp11DistributionVocabulary.MY + "Configuration")
public class DcatAp11DistributionConfig {

    public String getDistributionIRI() {
        return distributionIRI;
    }

    public void setDistributionIRI(String distributionIRI) {
        this.distributionIRI = distributionIRI;
    }

    public Boolean getGenDistroIRI() {
        return genDistroIRI;
    }

    public void setGenDistroIRI(Boolean genDistroIRI) {
        this.genDistroIRI = genDistroIRI;
    }

    public List<String> getAccessURLs() {
        return accessURLs;
    }

    public void setAccessURLs(List<String> accessURLs) {
        this.accessURLs = accessURLs;
    }

    public String getFormatIRI() {
        return formatIRI;
    }

    public void setFormatIRI(String formatIRI) {
        this.formatIRI = formatIRI;
    }

    public String getLicenseIRI() {
        return licenseIRI;
    }

    public void setLicenseIRI(String licenseIRI) {
        this.licenseIRI = licenseIRI;
    }

    public List<LocalizedString> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<LocalizedString> descriptions) {
        this.descriptions = descriptions;
    }

    public List<String> getDownloadURLs() {
        return downloadURLs;
    }

    public void setDownloadURLs(List<String> downloadURLs) {
        this.downloadURLs = downloadURLs;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public List<LocalizedString> getTitles() {
        return titles;
    }

    public void setTitles(List<LocalizedString> titles) {
        this.titles = titles;
    }

    public List<String> getDocumentationIRIs() {
        return documentationIRIs;
    }

    public void setDocumentationIRIs(List<String> documentationIRIs) {
        this.documentationIRIs = documentationIRIs;
    }

    public Boolean getLanguagesFromDataset() {
        return languagesFromDataset;
    }

    public void setLanguagesFromDataset(Boolean languagesFromDataset) {
        this.languagesFromDataset = languagesFromDataset;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getConformsToIRIs() {
        return conformsToIRIs;
    }

    public void setConformsToIRIs(List<String> conformsToIRIs) {
        this.conformsToIRIs = conformsToIRIs;
    }

    public String getStatusIRI() {
        return statusIRI;
    }

    public void setStatusIRI(String statusIRI) {
        this.statusIRI = statusIRI;
    }

    public Boolean getIssuedFromDataset() {
        return issuedFromDataset;
    }

    public void setIssuedFromDataset(Boolean issuedFromDataset) {
        this.issuedFromDataset = issuedFromDataset;
    }

    public Date getIssued() {
        return issued;
    }

    public void setIssued(Date issued) {
        this.issued = issued;
    }

    public Boolean getModifiedFromDataset() {
        return modifiedFromDataset;
    }

    public void setModifiedFromDataset(Boolean modifiedFromDataset) {
        this.modifiedFromDataset = modifiedFromDataset;
    }

    public Boolean getModifiedNow() {
        return modifiedNow;
    }

    public void setModifiedNow(Boolean modifiedNow) {
        this.modifiedNow = modifiedNow;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getRightsIRI() {
        return rightsIRI;
    }

    public void setRightsIRI(String rightsIRI) {
        this.rightsIRI = rightsIRI;
    }

    public Integer getByteSize() {
        return byteSize;
    }

    public void setByteSize(Integer byteSize) {
        this.byteSize = byteSize;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public List<String> getSpatialIRIs() {
        return spatialIRIs;
    }

    public void setSpatialIRIs(List<String> spatialIRIs) {
        this.spatialIRIs = spatialIRIs;
    }

    public Date getTemporalStart() {
        return temporalStart;
    }

    public void setTemporalStart(Date temporalStart) {
        this.temporalStart = temporalStart;
    }

    public Date getTemporalEnd() {
        return temporalEnd;
    }

    public void setTemporalEnd(Date temporalEnd) {
        this.temporalEnd = temporalEnd;
    }

    public String getDatasetIRI() {
        return datasetIRI;
    }

    public void setDatasetIRI(String datasetIRI) {
        this.datasetIRI = datasetIRI;
    }

    public Boolean getGetDatasetIRIFromInput() {
        return getDatasetIRIFromInput;
    }

    public void setGetDatasetIRIFromInput(Boolean getDatasetIRIFromInput) {
        this.getDatasetIRIFromInput = getDatasetIRIFromInput;
    }

    public String getLicenseTypeIRI() {
        return licenseTypeIRI;
    }

    public void setLicenseTypeIRI(String licenseTypeIRI) {
        this.licenseTypeIRI = licenseTypeIRI;
    }

    public String getDistributionTypeIRI() {
        return distributionTypeIRI;
    }

    public void setDistributionTypeIRI(String distributionTypeIRI) {
        this.distributionTypeIRI = distributionTypeIRI;
    }

    public static class LocalizedString extends RdfToPojo.LangString {
        public LocalizedString() {
        }

        public LocalizedString(String value, String language) {
            super(value, language);
        }
    }

    @RdfToPojo.Type(iri = DcatAp11DistributionVocabulary.MY + "LanguageObject")
    public static class Language {

        public Language(String iri) {
            this.iri = iri;
        }

        @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "IRI")
        private String iri;

        public Language() {
            iri = null;
        }

        public String getIri() {
            return iri;
        }

        public void setIri(String iri) {
            this.iri = iri;
        }

    }

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "datasetIRI")
    private String datasetIRI;

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "getDatasetIRIFromInput")
    private Boolean getDatasetIRIFromInput;

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "distributionIRI")
    private String distributionIRI;

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "genDistroIRI")
    private Boolean genDistroIRI;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "accessURLs")
    private List<String> accessURLs = new LinkedList<>();

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "formatIRI")
    private String formatIRI;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "licenseIRI")
    private String licenseIRI;

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "licenseTypeIRI")
    private String licenseTypeIRI;

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "descriptions")
    private List<LocalizedString> descriptions = new LinkedList<>();

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "downloadURLs")
    private List<String> downloadURLs = new LinkedList<>();

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "mediaType")
    private String mediaType;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "titles")
    private List<LocalizedString> titles = new LinkedList<>();

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "documentationIRIs")
    private List<String> documentationIRIs = new LinkedList<>();

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "languagesFromDataset")
    private Boolean languagesFromDataset;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "languages")
    private List<String> languages = new LinkedList<>();

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "conformsToIRIs")
    private List<String> conformsToIRIs = new LinkedList<>();

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "statusIRI")
    private String statusIRI;

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "issuedFromDataset")
    private Boolean issuedFromDataset;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "issued")
    private Date issued;

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "modifiedFromDataset")
    private Boolean modifiedFromDataset;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "modifiedNow")
    private Boolean modifiedNow;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "modified")
    private Date modified;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "rightsIRI")
    private String rightsIRI;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "byteSize")
    private Integer byteSize;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "checksum")
    private String checksum;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "spatialIRIs")
    private List<String> spatialIRIs = new LinkedList<>();

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "temporalStart")
    private Date temporalStart;

    @RdfToPojo.Property(iri = DcatAp11DistributionVocabulary.MY + "temporalEnd")
    private Date temporalEnd;

    @RdfToPojo.Property(
            iri = DcatAp11DistributionVocabulary.MY + "distributionTypeIRI")
    private String distributionTypeIRI;

}
