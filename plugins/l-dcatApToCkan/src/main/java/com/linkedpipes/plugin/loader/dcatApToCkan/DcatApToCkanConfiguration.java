package com.linkedpipes.plugin.loader.dcatApToCkan;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = DcatApToCkanConfigVocabulary.CONFIG_CLASS)
public class DcatApToCkanConfiguration {

    @RdfToPojo.Property(iri = DcatApToCkanConfigVocabulary.API_URL)
    private String apiUri = "";

    @RdfToPojo.Property(iri = DcatApToCkanConfigVocabulary.API_KEY)
    private String apiKey = "";

    @RdfToPojo.Property(iri = DcatApToCkanConfigVocabulary.LOAD_TO_CKAN)
    private boolean loadToCKAN = true;

    @RdfToPojo.Property(iri = DcatApToCkanConfigVocabulary.DATASET_ID)
    private String datasetID = "";

    @RdfToPojo.Property(iri = DcatApToCkanConfigVocabulary.FILENAME)
    private String filename = "ckan-api.json";

    @RdfToPojo.Property(iri = DcatApToCkanConfigVocabulary.ORG_ID)
    private String orgID = "";

    @RdfToPojo.Property(iri = DcatApToCkanConfigVocabulary.LOAD_LANGUAGE)
    private String loadLanguage = "cs";

    @RdfToPojo.Property(
            iri = DcatApToCkanConfigVocabulary.GENERATE_VIRTUOSO_EXAMPLE)
    private boolean generateVirtuosoTurtleExampleResource = true;

    @RdfToPojo.Property(iri = DcatApToCkanConfigVocabulary.GENERATE_EXAMPLE)
    private boolean generateExampleResource = true;

    @RdfToPojo.Property(iri = DcatApToCkanConfigVocabulary.OVERWRITE)
    private boolean overwrite = false;

    public DcatApToCkanConfiguration() {
    }

    public String getApiUri() {
        return apiUri;
    }

    public void setApiUri(String apiUri) {
        this.apiUri = apiUri;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isLoadToCKAN() {
        return loadToCKAN;
    }

    public void setLoadToCKAN(boolean loadToCKAN) {
        this.loadToCKAN = loadToCKAN;
    }

    public String getDatasetID() {
        return datasetID;
    }

    public void setDatasetID(String datasetID) {
        this.datasetID = datasetID;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOrgID() {
        return orgID;
    }

    public void setOrgID(String orgID) {
        this.orgID = orgID;
    }

    public String getLoadLanguage() {
        return loadLanguage;
    }

    public void setLoadLanguage(String loadLanguage) {
        this.loadLanguage = loadLanguage;
    }

    public boolean isGenerateVirtuosoTurtleExampleResource() {
        return generateVirtuosoTurtleExampleResource;
    }

    public void setGenerateVirtuosoTurtleExampleResource(
            boolean generateVirtuosoTurtleExampleResource) {
        this.generateVirtuosoTurtleExampleResource =
                generateVirtuosoTurtleExampleResource;
    }

    public boolean isGenerateExampleResource() {
        return generateExampleResource;
    }

    public void setGenerateExampleResource(boolean generateExampleResource) {
        this.generateExampleResource = generateExampleResource;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

}
