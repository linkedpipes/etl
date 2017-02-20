package com.linkedpipes.plugin.extractor.voidDataset;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

@RdfToPojo.Type(uri = VoidDatasetVocabulary.MY + "Configuration")
public class VoidDatasetConfig {

    public String getDistributionIRI() {
        return distributionIRI;
    }

    public void setDistributionIRI(String distributionIRI) {
        this.distributionIRI = distributionIRI;
    }

    public Boolean getGetDistributionIRIFromInput() {
        return getDistributionIRIFromInput;
    }

    public void setGetDistributionIRIFromInput(Boolean getDistributionIRIFromInput) {
        this.getDistributionIRIFromInput = getDistributionIRIFromInput;
    }

    public List<String> getExampleResourceIRIs() {
        return exampleResourceIRIs;
    }

    public void setExampleResourceIRIs(List<String> exampleResourceIRIs) {
        this.exampleResourceIRIs = exampleResourceIRIs;
    }

    public String getSparqlEndpointIRI() {
        return sparqlEndpointIRI;
    }

    public void setSparqlEndpointIRI(String sparqlEndpointIRI) {
        this.sparqlEndpointIRI = sparqlEndpointIRI;
    }

    public Boolean getCopyDownloadURLsToDataDumps() {
        return copyDownloadURLsToDataDumps;
    }

    public void setCopyDownloadURLsToDataDumps(Boolean copyDownloadURLsToDataDumps) {
        this.copyDownloadURLsToDataDumps = copyDownloadURLsToDataDumps;
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

    @RdfToPojo.Type(uri = VoidDatasetVocabulary.MY + "LanguageObject")
    public static class Language {

        public Language(String iri) {
            this.iri = iri;
        }

        @RdfToPojo.Property(uri = VoidDatasetVocabulary.MY + "IRI")
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

    @RdfToPojo.Property(uri = VoidDatasetVocabulary.MY + "distributionIRI")
    private String distributionIRI;

    @RdfToPojo.Property(uri = VoidDatasetVocabulary.MY + "getDistributionIRIFromInput")
    private Boolean getDistributionIRIFromInput;

    @RdfToPojo.Property(uri = VoidDatasetVocabulary.MY + "exampleResourceIRIs")
    private List<String> exampleResourceIRIs = new LinkedList<>();

    @RdfToPojo.Property(uri = VoidDatasetVocabulary.MY + "sparqlEndpointIRI")
    private String sparqlEndpointIRI;

    @RdfToPojo.Property(uri = VoidDatasetVocabulary.MY + "copyDownloadURLsToDataDumps")
    private Boolean copyDownloadURLsToDataDumps;
}
