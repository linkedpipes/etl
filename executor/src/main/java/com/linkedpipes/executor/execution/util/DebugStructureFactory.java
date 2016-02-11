package com.linkedpipes.executor.execution.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.commons.entities.executor.DebugStructure;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.executor.execution.contoller.ResourceManager;
import com.linkedpipes.executor.rdf.boundary.DefinitionStorage;
import com.linkedpipes.executor.rdf.boundary.MessageStorage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public final class DebugStructureFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DebugStructureFactory.class);

    private static final String QUERY_COMPONENTS = ""
            + "SELECT ?component ?port ?binding ?directory ?fragment WHERE {"
            + "  ?component a <" + LINKEDPIPES.COMPONENT + "> ;\n"
            + "    <" + LINKEDPIPES.HAS_PORT + "> ?port .\n"
            + "\n"
            + "  ?port a <" + LINKEDPIPES.PORT + "> ;\n"
            + "    <" + LINKEDPIPES.HAS_BINDING + "> ?binding ;\n"
            + "    <" + LINKEDPIPES.HAS_URI_FRAGMENT + "> ?fragment ;\n"
            + "    <" + LINKEDPIPES.HAS_DEBUG_DIRECTORY + "> ?directory .\n"
            + "}";

    private static String getTypeQuery(String entity) {
        return "SELECT ?type WHERE { <" + entity + "> a ?type }";
    }

    public static void createDebugOutput(String executionId, DefinitionStorage definition, MessageStorage messages,
            ResourceManager resourceManager) {
        final List<Map<String, String>> queryResult;
        try {
            queryResult = definition.executeSelect(QUERY_COMPONENTS);
        } catch (SparqlSelect.QueryException ex) {
            LOG.error("Can't prepare debug data.", ex);
            return;
        }
        // Create output.
        final DebugStructure debugStructure = new DebugStructure();
        debugStructure.setExecutionId(executionId);
        for (Map<String, String> item : queryResult) {
            // Get DataUnit.
            final DebugStructure.DataUnit dataUnit;
            if (debugStructure.getDataUnits().containsKey(item.get("fragment"))) {
                // This should not happen !
                LOG.error("Duplicit result detected for: {}", item.get("fragment"));
                dataUnit = debugStructure.getDataUnits().get(item.get("fragment"));
            } else {
                dataUnit = new DebugStructure.DataUnit();
                debugStructure.getDataUnits().put(item.get("fragment"), dataUnit);
            }
            // Add data to DataUnit.
            dataUnit.setDebugDirectory(item.get("directory"));
            dataUnit.setBinding(item.get("binding"));
            dataUnit.setComponentUri(item.get("component"));
            // Select types and add them.
            try {
                final List<Map<String, String>> types = definition.executeSelect(getTypeQuery(item.get("port")));
                for (Map<String, String> type : types) {
                    dataUnit.getTypes().add(type.get("type"));
                }
            } catch (SparqlSelect.QueryException ex) {
                LOG.error("Can't query for port types.", ex);
            }
        }
        // Save to hard drive.
        final ObjectMapper json = new ObjectMapper();
        if (resourceManager == null) {
            return;
        }
        final File statusFile = resourceManager.getDebugFile();
        try {
            json.writerWithDefaultPrettyPrinter().writeValue(statusFile, debugStructure);
        } catch (IOException ex) {
            LOG.error("Can't write debug file!", ex);
        }

    }

}
