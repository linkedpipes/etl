package cz.skodape.hdt.selector.identity;

import com.fasterxml.jackson.databind.JsonNode;
import cz.skodape.hdt.model.SelectorConfiguration;
import cz.skodape.hdt.model.TransformationFileAdapter;

public class IdentitySelectorAdapter
        implements TransformationFileAdapter.SelectorConfigurationAdapter {

    private static final String TYPE = "Identity";

    @Override
    public SelectorConfiguration readJson(JsonNode root) {
        String type = root.get("type").asText();
        if (TYPE.equals(type)) {
            return new IdentitySelectorConfiguration();
        }
        return null;
    }

}
