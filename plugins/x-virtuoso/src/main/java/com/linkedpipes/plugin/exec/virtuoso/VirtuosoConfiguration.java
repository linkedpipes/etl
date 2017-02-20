package com.linkedpipes.plugin.exec.virtuoso;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = VirtuosoVocabulary.CONFIG_CLASS)
public class VirtuosoConfiguration {

    @RdfToPojo.Property(iri = VirtuosoVocabulary.VIRTUOSO_URI)
    private String virtuosoUrl = "";

    @RdfToPojo.Property(iri = VirtuosoVocabulary.USERNAME)
    private String username = "";

    @RdfToPojo.Property(iri = VirtuosoVocabulary.PASSWORD)
    private String password = "";

    @RdfToPojo.Property(iri = VirtuosoVocabulary.CLEAR_GRAPH)
    private boolean clearDestinationGraph = false;

    @RdfToPojo.Property(iri = VirtuosoVocabulary.LOAD_DIRECTORY_PATH)
    private String loadDirectoryPath = "";

    @RdfToPojo.Property(iri = VirtuosoVocabulary.LOAD_FILE_NAME)
    private String loadFileName = "";

    @RdfToPojo.Property(iri = VirtuosoVocabulary.TARGET_GRAPH)
    private String targetGraph = "";

    @RdfToPojo.Property(iri = VirtuosoVocabulary.STATUS_UPDATE_INTERVAL)
    private int statusUpdateInterval = 10;

    @RdfToPojo.Property(iri = VirtuosoVocabulary.CLEAR_LOAD_GRAPH)
    private boolean clearLoadList = true;

    @RdfToPojo.Property(iri = VirtuosoVocabulary.LOADER_COUNT)
    private int loaderCount = 1;

    public VirtuosoConfiguration() {
    }

    public String getVirtuosoUrl() {
        return virtuosoUrl;
    }

    public void setVirtuosoUrl(String virtuosoUrl) {
        this.virtuosoUrl = virtuosoUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isClearDestinationGraph() {
        return clearDestinationGraph;
    }

    public void setClearDestinationGraph(boolean clearDestinationGraph) {
        this.clearDestinationGraph = clearDestinationGraph;
    }

    public String getLoadDirectoryPath() {
        return loadDirectoryPath;
    }

    public void setLoadDirectoryPath(String loadDirectoryPath) {
        this.loadDirectoryPath = loadDirectoryPath;
    }

    public String getLoadFileName() {
        return loadFileName;
    }

    public void setLoadFileName(String loadFileName) {
        this.loadFileName = loadFileName;
    }

    public String getTargetGraph() {
        return targetGraph;
    }

    public void setTargetGraph(String targetGraph) {
        this.targetGraph = targetGraph;
    }

    public int getStatusUpdateInterval() {
        return statusUpdateInterval;
    }

    public void setStatusUpdateInterval(int statusUpdateInterval) {
        this.statusUpdateInterval = statusUpdateInterval;
    }

    public boolean isClearLoadList() {
        return clearLoadList;
    }

    public void setClearLoadList(boolean clearLoadList) {
        this.clearLoadList = clearLoadList;
    }

    public int getLoaderCount() {
        return loaderCount;
    }

    public void setLoaderCount(int loaderCount) {
        this.loaderCount = loaderCount;
    }
}
