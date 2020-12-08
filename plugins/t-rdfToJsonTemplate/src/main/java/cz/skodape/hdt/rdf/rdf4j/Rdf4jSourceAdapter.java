package cz.skodape.hdt.rdf.rdf4j;

import com.fasterxml.jackson.databind.JsonNode;
import cz.skodape.hdt.model.SourceConfiguration;
import cz.skodape.hdt.model.TransformationFileAdapter;

import java.io.File;

public class Rdf4jSourceAdapter
        implements TransformationFileAdapter.SourceConfigurationAdapter {

    @Override
    public SourceConfiguration readJson(JsonNode root) {
        String type = root.get("type").asText();
        if ("Rdf4jMemory".equals(type)) {
            return readRdf4jMemorySource(root);
        }
        if ("Rdf4jChunkedMemory".equals(type)) {
            return readRdf4jChunkedSource(root);
        }
        return null;
    }

    public SourceConfiguration readRdf4jMemorySource(JsonNode root) {
        Rdf4jMemorySourceConfiguration result =
                new Rdf4jMemorySourceConfiguration();
        if (root.has("file")) {
            result.file = new File(root.get("file").asText());
        }
        if (root.has("graphAware")) {
            result.graphAware = root.get("graphAware").asBoolean();
        }
        return result;
    }


    public SourceConfiguration readRdf4jChunkedSource(JsonNode root) {
        Rdf4jChunkedSourceConfiguration result =
                new Rdf4jChunkedSourceConfiguration();
        if (root.has("file")) {
            result.file = new File(root.get("file").asText());
        }
        return result;
    }

}
