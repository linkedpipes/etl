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

    @RdfToPojo.Property(uri = DcatAp11ToCkanConfigVocabulary.DATASET_ID)
    private String datasetID;

    @RdfToPojo.Property(uri = DcatAp11ToCkanConfigVocabulary.LOAD_LANGUAGE)
    private String loadLanguage;

    @RdfToPojo.Property(uri = DcatAp11ToCkanConfigVocabulary.PROFILE)
    private String profile ;

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

    public String getLoadLanguage() {
        return loadLanguage;
    }

    public void setLoadLanguage(String loadLanguage) {
        this.loadLanguage = loadLanguage;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
