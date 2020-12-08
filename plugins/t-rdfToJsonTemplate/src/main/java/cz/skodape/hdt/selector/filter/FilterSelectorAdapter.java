package cz.skodape.hdt.selector.filter;

import com.fasterxml.jackson.databind.JsonNode;
import cz.skodape.hdt.model.SelectorConfiguration;
import cz.skodape.hdt.model.TransformationFileAdapter;
import cz.skodape.hdt.selector.path.PathSelectorAdapter;

import java.io.IOException;

public class FilterSelectorAdapter
        implements TransformationFileAdapter.SelectorConfigurationAdapter {

    @Override
    public SelectorConfiguration readJson(JsonNode root) throws IOException {
        String type = root.get("type").asText();
        if ("Filter".equals(type)) {
            return readConfiguration(root);
        }
        return null;
    }

    public FilterSelectorConfiguration readConfiguration(
            JsonNode root) throws IOException {
        FilterSelectorConfiguration result = new FilterSelectorConfiguration();
        PathSelectorAdapter pathAdapter = new PathSelectorAdapter();
        result.path = pathAdapter.readConfiguration(root);
        result.value = root.get("value").textValue();
        result.condition = readCondition(root.get("condition"));
        return result;
    }

    private FilterSelectorConfiguration.ConditionType readCondition(
            JsonNode node) throws IOException {
        String type = node.textValue();
        switch (type) {
            case "Contain":
                return FilterSelectorConfiguration.ConditionType.Contain;
            case "Equal":
                return FilterSelectorConfiguration.ConditionType.Equal;
            default:
                throw new IOException(
                        "Unsupported condition type '" + type + "'.");
        }
    }

}
