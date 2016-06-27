package com.linkedpipes.plugin.extractor.dcatAp11DatasetMetadata;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

@RdfToPojo.Type(uri = DcatAp11DatasetMetadataVocabulary.MY + "Configuration")
public class DcatAp11DatasetMetadataConfig {

    @RdfToPojo.Value
    public static class LocalizedString {

        @RdfToPojo.Value
        private String value;

        @RdfToPojo.Lang
        private String language;

        public LocalizedString() {
            value = null;
            language = null;
        }

        public LocalizedString(String value, String language) {
            this.value = value;
            this.language = language;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

    }

    @RdfToPojo.Type(uri = DcatAp11DatasetMetadataVocabulary.MY + "LanguageObject")
    public static class Language {

        @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "IRI")
        private String iri;

        public String getIri() {
            return iri;
        }

        public void setIri(String iri) {
            this.iri = iri;
        }

    }

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "datasetIRI")
    private String datasetIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "titles")
    private List<LocalizedString> titles = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "descriptions")
    private List<LocalizedString> descriptions = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "contactPointTypeIRI")
    private String contactPointTypeIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "contactPointName")
    private String contactPointName;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "contactPointEmail")
    private String contactPointEmail;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "keywords")
    private List<LocalizedString> keywords = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "euThemeIRI")
    private String euThemeIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "otherThemeIRIs")
    private List<String> otherThemeIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "publisherIRI")
    private String publisherIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "publisherNames")
    private List<LocalizedString> publisherNames = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "publisherTypeIRI")
    private String publisherTypeIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "languages")
    private List<Language> languages = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "accrualPeriodicityIRI")
    private String accrualPeriodicityIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "issued")
	private Date issued = new Date() ;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "modified")
	private Date modified = new Date() ;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "spatialIRIs")
    private List<String> spatialIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "temporalStart")
	private Date temporalStart = new Date() ;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "temporalEnd")
	private Date temporalEnd = new Date() ;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "documentationIRIs")
    private List<String> documentationIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "accessRightsIRI")
    private String accessRightsIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "identifier")
    private String identifier;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "datasetTypeIRI")
    private String datasetTypeIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "provenances")
    private List<LocalizedString> provenance = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "sampleIRIs")
    private List<String> sampleIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "landingPageIRIs")
    private List<String> landingPageIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "relatedIRIs")
    private List<String> relatedIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "confromsToIRIs")
    private List<String> confromsToIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "sourceIRIs")
    private List<String> sourceIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "hasVersionIRIs")
    private List<String> hasVersionIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "isVersionOfIRIs")
    private List<String> isVersionOfIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "version")
    private String version;

    @RdfToPojo.Property(uri = DcatAp11DatasetMetadataVocabulary.MY + "versionNotes")
    private List<LocalizedString> versionNotes = new LinkedList<>();

    public String getDatasetIRI() {
        return datasetIRI;
    }

    public void setDatasetIRI(String datasetIRI) {
        this.datasetIRI = datasetIRI;
    }

    public List<LocalizedString> getTitles() {
        return titles;
    }

    public void setTitles(List<LocalizedString> titles) {
        this.titles = titles;
    }

    public List<LocalizedString> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<LocalizedString> descriptions) {
        this.descriptions = descriptions;
    }

    public List<LocalizedString> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<LocalizedString> keywords) {
        this.keywords = keywords;
    }

    public String getEuThemeIRI() {
        return euThemeIRI;
    }

    public void setEuThemeIRI(String euThemeIRI) {
        this.euThemeIRI = euThemeIRI;
    }

    public List<String> getOtherThemeIRIs() {
        return otherThemeIRIs;
    }

    public void setOtherThemeIRIs(List<String> otherThemeIRIs) {
        this.otherThemeIRIs = otherThemeIRIs;
    }

    public String getPublisherIRI() {
        return publisherIRI;
    }

    public void setPublisherIRI(String publisherIRI) {
        this.publisherIRI = publisherIRI;
    }

    public List<LocalizedString> getPublisherNames() {
        return publisherNames;
    }

    public void setPublisherNames(List<LocalizedString> publisherNames) {
        this.publisherNames = publisherNames;
    }

    public String getPublisherTypeIRI() {
        return publisherTypeIRI;
    }

    public void setPublisherTypeIRI(String publisherTypeIRI) {
        this.publisherTypeIRI = publisherTypeIRI;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public String getAccrualPeriodicityIRI() {
        return accrualPeriodicityIRI;
    }

    public void setAccrualPeriodicityIRI(String accrualPeriodicityIRI) {
        this.accrualPeriodicityIRI = accrualPeriodicityIRI;
    }

    public Date getIssued() {
        return issued;
    }

    public void setIssued(Date issued) {
        this.issued = issued;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
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

    public List<String> getDocumentationIRIs() {
        return documentationIRIs;
    }

    public void setDocumentationIRIs(List<String> documentationIRIs) {
        this.documentationIRIs = documentationIRIs;
    }

    public String getAccessRightsIRI() {
        return accessRightsIRI;
    }

    public void setAccessRightsIRI(String accessRightsIRI) {
        this.accessRightsIRI = accessRightsIRI;
    }

    public List<String> getSampleIRIs() {
        return sampleIRIs;
    }

    public void setSampleIRIs(List<String> sampleIRIs) {
        this.sampleIRIs = sampleIRIs;
    }

    public List<String> getLandingPageIRIs() {
        return landingPageIRIs;
    }

    public void setLandingPageIRIs(List<String> landingPageIRIs) {
        this.landingPageIRIs = landingPageIRIs;
    }

    public List<String> getRelatedIRIs() {
        return relatedIRIs;
    }

    public void setRelatedIRIs(List<String> relatedIRIs) {
        this.relatedIRIs = relatedIRIs;
    }

    public List<String> getConfromsToIRIs() {
        return confromsToIRIs;
    }

    public void setConfromsToIRIs(List<String> confromsToIRIs) {
        this.confromsToIRIs = confromsToIRIs;
    }

    public List<String> getSourceIRIs() {
        return sourceIRIs;
    }

    public void setSourceIRIs(List<String> sourceIRIs) {
        this.sourceIRIs = sourceIRIs;
    }

    public List<String> getHasVersionIRIs() {
        return hasVersionIRIs;
    }

    public void setHasVersionIRIs(List<String> hasVersionIRIs) {
        this.hasVersionIRIs = hasVersionIRIs;
    }

    public List<String> getIsVersionOfIRIs() {
        return isVersionOfIRIs;
    }

    public void setIsVersionOfIRIs(List<String> isVersionOfIRIs) {
        this.isVersionOfIRIs = isVersionOfIRIs;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<LocalizedString> getVersionNotes() {
        return versionNotes;
    }

    public void setVersionNotes(List<LocalizedString> versionNotes) {
        this.versionNotes = versionNotes;
    }

    public String getContactPointTypeIRI() {
        return contactPointTypeIRI;
    }

    public void setContactPointTypeIRI(String contactPointTypeIRI) {
        this.contactPointTypeIRI = contactPointTypeIRI;
    }

    public String getContactPointName() {
        return contactPointName;
    }

    public void setContactPointName(String contactPointName) {
        this.contactPointName = contactPointName;
    }

    public String getContactPointEmail() {
        return contactPointEmail;
    }

    public void setContactPointEmail(String contactPointEmail) {
        this.contactPointEmail = contactPointEmail;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDatasetTypeIRI() {
        return datasetTypeIRI;
    }

    public void setDatasetTypeIRI(String datasetTypeIRI) {
        this.datasetTypeIRI = datasetTypeIRI;
    }

    public List<LocalizedString> getProvenance() {
        return provenance;
    }

    public void setProvenance(List<LocalizedString> provenance) {
        this.provenance = provenance;
    }
}
