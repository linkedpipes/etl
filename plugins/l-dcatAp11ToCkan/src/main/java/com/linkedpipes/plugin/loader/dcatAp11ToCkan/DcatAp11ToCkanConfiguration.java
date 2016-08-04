package com.linkedpipes.plugin.loader.dcatAp11ToCkan;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Klímek Jakub
 */
@RdfToPojo.Type(uri = DcatAp11ToCkanConfigVocabulary.CONFIG_CLASS)
public class DcatAp11ToCkanConfiguration {

    @RdfToPojo.Property(uri = DcatAp11ToCkanConfigVocabulary.API_URL)
    private String apiUri;

    @RdfToPojo.Property(uri = DcatAp11ToCkanConfigVocabulary.API_KEY)
    private String apiKey ;

    @RdfToPojo.Property(uri = DcatAp11ToCkanConfigVocabulary.CREATE_CKAN_ORG)
    private boolean createCkanOrg ;

    @RdfToPojo.Property(uri = DcatAp11ToCkanConfigVocabulary.DATASET_ID)
    private String datasetID;

    @RdfToPojo.Property(uri = DcatAp11ToCkanConfigVocabulary.ORG_ID)
    private String orgID;

    @RdfToPojo.Property(uri = DcatAp11ToCkanConfigVocabulary.LOAD_LANGUAGE)
    private String loadLanguage;

    @RdfToPojo.Property(uri = DcatAp11ToCkanConfigVocabulary.OVERWRITE)
    private boolean overwrite;

    public DcatAp11ToCkanConfiguration() {
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

    public String getDatasetID() {
        return datasetID;
    }

    public void setDatasetID(String datasetID) {
        this.datasetID = datasetID;
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

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isCreateCkanOrg() {
        return createCkanOrg;
    }

    public void setCreateCkanOrg(boolean createCkanOrg) {
        this.createCkanOrg = createCkanOrg;
    }
}
