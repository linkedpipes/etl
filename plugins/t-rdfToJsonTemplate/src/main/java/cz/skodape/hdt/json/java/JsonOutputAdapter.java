package cz.skodape.hdt.json.java;

import com.fasterxml.jackson.databind.JsonNode;
import cz.skodape.hdt.model.OutputConfiguration;
import cz.skodape.hdt.model.TransformationFileAdapter;

import java.io.IOException;

public class JsonOutputAdapter
        implements TransformationFileAdapter.OutputConfigurationAdapter {

    @Override
    public OutputConfiguration readJson(JsonNode root) throws IOException {
        if (!root.has("type")) {
            return null;
        }
        if (!"JsonOutput".equals(root.get("type").asText())) {
            return null;
        }
        JsonOutputConfiguration result = new JsonOutputConfiguration();
        result.datatype = asType(root.get("dataType").asText());
        return result;
    }

    public JsonOutputConfiguration.Type asType(String string)
            throws IOException {
        switch (string) {
            case "string":
                return JsonOutputConfiguration.Type.String;
            case "number":
                return JsonOutputConfiguration.Type.Number;
            case "boolean":
                return JsonOutputConfiguration.Type.Boolean;
            default:
                throw new IOException("Invalid type '" + string + "'.");
        }
    }

}
