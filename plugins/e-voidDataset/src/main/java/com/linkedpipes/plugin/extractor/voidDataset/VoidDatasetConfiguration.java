package com.linkedpipes.plugin.extractor.voidDataset;

import com.linkedpipes.etl.executor.api.v1.rdf.LanguageString;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;


@RdfToPojo.Type(iri = VoidDatasetVocabulary.MY + "Configuration")
public class VoidDatasetConfiguration {

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

    public static class LocalizedString extends LanguageString {

    }

    @RdfToPojo.Type(iri = VoidDatasetVocabulary.MY + "LanguageObject")
    public static class Language {

        public Language(String iri) {
            this.iri = iri;
        }

        @RdfToPojo.Property(iri = VoidDatasetVocabulary.MY + "IRI")
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

    @RdfToPojo.Property(iri = VoidDatasetVocabulary.MY + "distributionIRI")
    private String distributionIRI;

    @RdfToPojo.Property(iri = VoidDatasetVocabulary.MY + "getDistributionIRIFromInput")
    private Boolean getDistributionIRIFromInput;

    @RdfToPojo.Property(iri = VoidDatasetVocabulary.MY + "exampleResourceIRIs")
    private List<String> exampleResourceIRIs = new LinkedList<>();

    @RdfToPojo.Property(iri = VoidDatasetVocabulary.MY + "sparqlEndpointIRI")
    private String sparqlEndpointIRI;

    @RdfToPojo.Property(iri = VoidDatasetVocabulary.MY + "copyDownloadURLsToDataDumps")
    private Boolean copyDownloadURLsToDataDumps;
}
