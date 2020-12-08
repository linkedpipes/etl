package cz.skodape.hdt.json.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import cz.skodape.hdt.model.SourceConfiguration;
import cz.skodape.hdt.model.TransformationFileAdapter;

public class JacksonSourceAdapter
        implements TransformationFileAdapter.SourceConfigurationAdapter {

    @Override
    public SourceConfiguration readJson(JsonNode root) {
        return null;
    }

}
