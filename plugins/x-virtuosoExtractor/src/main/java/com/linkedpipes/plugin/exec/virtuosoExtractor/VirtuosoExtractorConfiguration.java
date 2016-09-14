package com.linkedpipes.plugin.exec.virtuosoExtractor;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = VirtuosoExtractorVocabulary.CONFIG_CLASS)
public class VirtuosoExtractorConfiguration {

    @RdfToPojo.Property(uri = VirtuosoExtractorVocabulary.VIRTUOSO_URI)
    private String virtuosoUrl =
            "jdbc:virtuosoExtractor://localhost:1111/charset=UTF-8/";

    @RdfToPojo.Property(uri = VirtuosoExtractorVocabulary.USERNAME)
    private String username = "dba";

    @RdfToPojo.Property(uri = VirtuosoExtractorVocabulary.PASSWORD)
    private String password = "dba";

    @RdfToPojo.Property(uri = VirtuosoExtractorVocabulary.OUTPUT_PATH)
    private String outputPath = "";

    @RdfToPojo.Property(uri = VirtuosoExtractorVocabulary.GRAPH)
    private String graph = "";

    public VirtuosoExtractorConfiguration() {
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

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }
}
