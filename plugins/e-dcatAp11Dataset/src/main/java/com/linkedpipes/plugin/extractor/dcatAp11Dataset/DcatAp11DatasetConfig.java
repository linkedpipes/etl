package com.linkedpipes.plugin.extractor.dcatAp11Dataset;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

@RdfToPojo.Type(uri = DcatAp11DatasetVocabulary.MY + "Configuration")
public class DcatAp11DatasetConfig {

    public Boolean getModifiedNow() {
        return modifiedNow;
    }

    public void setModifiedNow(Boolean modifiedNow) {
        this.modifiedNow = modifiedNow;
    }

    public String getCatalogIRI() {
        return catalogIRI;
    }

    public void setCatalogIRI(String catalogIRI) {
        this.catalogIRI = catalogIRI;
    }

    public List<String> getAttributeIRIs() {
        return attributeIRIs;
    }

    public void setAttributeIRIs(List<String> attributeIRIs) {
        this.attributeIRIs = attributeIRIs;
    }

    public List<String> getDimensionIRIs() {
        return dimensionIRIs;
    }

    public void setDimensionIRIs(List<String> dimensionIRIs) {
        this.dimensionIRIs = dimensionIRIs;
    }

    public Integer getNumSeries() {
        return numSeries;
    }

    public void setNumSeries(Integer numSeries) {
        this.numSeries = numSeries;
    }

    public List<String> getQualityAnnotationIRIs() {
        return qualityAnnotationIRIs;
    }

    public void setQualityAnnotationIRIs(List<String> qualityAnnotationIRIs) {
        this.qualityAnnotationIRIs = qualityAnnotationIRIs;
    }

    public List<String> getUnitOfMeasurementIRIs() {
        return unitOfMeasurementIRIs;
    }

    public void setUnitOfMeasurementIRIs(List<String> unitOfMeasurementIRIs) {
        this.unitOfMeasurementIRIs = unitOfMeasurementIRIs;
    }

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

    @RdfToPojo.Type(uri = DcatAp11DatasetVocabulary.MY + "LanguageObject")
    public static class Language {

        public Language(String iri) {
            this.iri = iri;
        }

        @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "IRI")
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

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "datasetIRI")
    private String datasetIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "titles")
    private List<LocalizedString> titles = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "descriptions")
    private List<LocalizedString> descriptions = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "contactPointTypeIRI")
    private String contactPointTypeIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "contactPointName")
    private String contactPointName;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "contactPointEmail")
    private String contactPointEmail;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "keywords")
    private List<LocalizedString> keywords = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "euThemeIRI")
    private String euThemeIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "otherThemeIRIs")
    private List<String> otherThemeIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "publisherIRI")
    private String publisherIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "publisherNames")
    private List<LocalizedString> publisherNames = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "publisherTypeIRI")
    private String publisherTypeIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "languages")
    private List<Language> languages = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "accrualPeriodicityIRI")
    private String accrualPeriodicityIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "issued")
    private Date issued;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "modifiedNow")
    private Boolean modifiedNow;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "modified")
    private Date modified;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "spatialIRIs")
    private List<String> spatialIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "temporalStart")
    private Date temporalStart;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "temporalEnd")
    private Date temporalEnd;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "documentationIRIs")
    private List<String> documentationIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "accessRightsIRI")
    private String accessRightsIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "identifier")
    private String identifier;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "datasetTypeIRI")
    private String datasetTypeIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "provenances")
    private List<LocalizedString> provenance = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "catalogIRI")
    private String catalogIRI;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "sampleIRIs")
    private List<String> sampleIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "landingPageIRIs")
    private List<String> landingPageIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "relatedIRIs")
    private List<String> relatedIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "conformsToIRIs")
    private List<String> conformsToIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "sourceIRIs")
    private List<String> sourceIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "hasVersionIRIs")
    private List<String> hasVersionIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "isVersionOfIRIs")
    private List<String> isVersionOfIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "version")
    private String version;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "versionNotes")
    private List<LocalizedString> versionNotes = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "attributeIRIs")
    private List<String> attributeIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "dimensionIRIs")
    private List<String> dimensionIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "numSeries")
    private Integer numSeries;

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "qualityAnnotationIRIs")
    private List<String> qualityAnnotationIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = DcatAp11DatasetVocabulary.MY + "unitOfMeasurementIRIs")
    private List<String> unitOfMeasurementIRIs = new LinkedList<>();


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

    public List<String> getConformsToIRIs() {
        return conformsToIRIs;
    }

    public void setConformsToIRIs(List<String> conformsToIRIs) {
        this.conformsToIRIs = conformsToIRIs;
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
