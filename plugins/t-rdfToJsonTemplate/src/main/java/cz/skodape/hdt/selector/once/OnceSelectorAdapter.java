package cz.skodape.hdt.selector.once;

import com.fasterxml.jackson.databind.JsonNode;
import cz.skodape.hdt.model.SelectorConfiguration;
import cz.skodape.hdt.model.TransformationFileAdapter;

public class OnceSelectorAdapter
        implements TransformationFileAdapter.SelectorConfigurationAdapter {

    private static final String TYPE = "Once";

    @Override
    public SelectorConfiguration readJson(JsonNode root) {
        String type = root.get("type").asText();
        if (TYPE.equals(type)) {
            return new OnceSelectorConfiguration();
        }
        return null;
    }
}
