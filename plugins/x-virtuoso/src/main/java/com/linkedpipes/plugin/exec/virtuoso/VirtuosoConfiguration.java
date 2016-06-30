package com.linkedpipes.plugin.exec.virtuoso;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = VirtuosoVocabulary.CONFIG_CLASS)
public class VirtuosoConfiguration {

    @RdfToPojo.Property(uri = VirtuosoVocabulary.VIRTUOSO_URI)
    private String virtuosoUrl = "jdbc:virtuoso://localhost:1111/charset=UTF-8/";

    @RdfToPojo.Property(uri = VirtuosoVocabulary.USERNAME)
    private String username = "dba";

    @RdfToPojo.Property(uri = VirtuosoVocabulary.PASSWORD)
    private String password = "dba";

    @RdfToPojo.Property(uri = VirtuosoVocabulary.CLEAR_GRAPH)
    private boolean clearDestinationGraph = false;

    @RdfToPojo.Property(uri = VirtuosoVocabulary.LOAD_DIRECTORY_PATH)
    private String loadDirectoryPath = "";

    @RdfToPojo.Property(uri = VirtuosoVocabulary.LOAD_FILE_NAME)
    private String loadFileName = "";

    @RdfToPojo.Property(uri = VirtuosoVocabulary.TARGET_GRAPH)
    private String targetGraph = "";

    @RdfToPojo.Property(uri = VirtuosoVocabulary.STATUS_UPDATE_INTERVAL)
    private int statusUpdateInterval = 10;

    @RdfToPojo.Property(uri = VirtuosoVocabulary.CLEAR_LOAD_GRAPH)
    private boolean clearLoadList = true;

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
}
