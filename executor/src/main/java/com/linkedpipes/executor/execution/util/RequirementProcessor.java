package com.linkedpipes.executor.execution.util;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import java.io.File;
import java.util.Map;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.executor.execution.contoller.ResourceManager;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration;
import com.linkedpipes.executor.rdf.boundary.DefinitionStorage;
import com.linkedpipes.executor.rdf.boundary.RdfOperationFailed;
import com.linkedpipes.executor.rdf.boundary.WritableRdfJava;
import com.linkedpipes.etl.utils.core.entity.EntityLoader;

/**
 * Process requirement in pipeline definition and handle them.
 *
 * @author Škoda Petr
 */
public final class RequirementProcessor {

    public static class InvalidRequirement extends Exception {

        public InvalidRequirement(String message) {
            super(message);
        }

        public InvalidRequirement(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private static class TempDirectory implements EntityLoader.Loadable {

        private String targetProperty;

        @Override
        public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
            switch (predicate) {
                case LINKEDPIPES.REQUIREMENTS.HAS_TARGET_PROPERTY:
                    targetProperty = value;
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public void validate() throws EntityLoader.LoadingFailed {
            if (targetProperty == null) {
                throw new EntityLoader.LoadingFailed("All fields must be set!");
            }
        }

    }

    private static class DefinitionResource implements EntityLoader.Loadable {

        private String targetProperty;

        private String sourceProperty;

        @Override
        public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
            switch (predicate) {
                case LINKEDPIPES.REQUIREMENTS.HAS_TARGET_PROPERTY:
                    targetProperty = value;
                    return null;
                case LINKEDPIPES.REQUIREMENTS.HAS_SOURCE_PROPERTY:
                    sourceProperty = value;
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public void validate() throws EntityLoader.LoadingFailed {
            if (targetProperty == null || sourceProperty == null) {
                throw new EntityLoader.LoadingFailed("All fields must be set!");
            }
        }

    }

    /**
     * Query for all existing requirements.
     *
     * We can select single requirement multiple times, based on it's types.
     */
    private static final String QUERY_REQUIREMENTS = ""
            + "SELECT ?source ?requirement ?type ?value WHERE {\n"
            + "  ?source <" + LINKEDPIPES.HAS_REQUIREMENT + "> ?requirement .\n"
            + "  ?requirement a <" + LINKEDPIPES.REQUIREMENTS.REQUIREMENT + "> ;"
            + "    a ?type. \n"
            + "  OPTIONAL {\n"
            + "    ?requirement <" + LINKEDPIPES.REQUIREMENTS.HAS_SOURCE_PROPERTY + "> ?uri. \n"
            + "    ?source ?uri ?value. \n"
            + "  }\n"
            + "}";

    public static void handle(PipelineConfiguration pipeline, ResourceManager resourceManager,
            DefinitionStorage definitionDataUnit)
            throws SparqlSelect.QueryException, InvalidRequirement, RdfOperationFailed {
        for (Map<String, String> record : definitionDataUnit.executeSelect(QUERY_REQUIREMENTS)) {
            switch (record.get("type")) {
                case LINKEDPIPES.REQUIREMENTS.TEMP_DIRECTORY:
                    handleTempDirectory(resourceManager, definitionDataUnit, record.get("source"),
                            record.get("requirement"), definitionDataUnit.getDefinitionGraphUri());
                    break;
                case LINKEDPIPES.REQUIREMENTS.RESOLVE_DEFINITION_RESOURCE:
                    if (!record.containsKey("value")) {
                        throw new InvalidRequirement("Missing path to resolve!");
                    }
                    handleDefinitionResource(resourceManager, definitionDataUnit, record.get("source"),
                            record.get("requirement"), record.get("value"), definitionDataUnit.getDefinitionGraphUri());
                    break;
                default:
                    break;
            }
        }

    }

    protected static void handleTempDirectory(ResourceManager resourceManager,
            DefinitionStorage definitionDataUnit, String source, String requirementUri, String graph)
            throws InvalidRequirement, RdfOperationFailed {
        final TempDirectory tempDirectory = new TempDirectory();
        try {
            EntityLoader.load(definitionDataUnit, requirementUri, graph, tempDirectory);
        } catch (EntityLoader.LoadingFailed ex) {
            throw new InvalidRequirement("Requirement: " + requirementUri, ex);
        }
        final File workingDir = resourceManager.getWorkingDir("temp-");
        final WritableRdfJava writable = definitionDataUnit.asWritableRdfJava();
        writable.begin();
        writable.addUri(source, tempDirectory.targetProperty, workingDir.toURI().toString());
        writable.commit();
    }

    protected static void handleDefinitionResource(ResourceManager resourceManager,
            DefinitionStorage definitionDataUnit, String source, String requirementUri, String pathToResolve,
            String graph) throws SparqlSelect.QueryException, InvalidRequirement, RdfOperationFailed {
        final DefinitionResource definitionResource = new DefinitionResource();
        try {
            EntityLoader.load(definitionDataUnit, requirementUri, graph, definitionResource);
        } catch (EntityLoader.LoadingFailed ex) {
            throw new InvalidRequirement("Requirement: " + requirementUri, ex);
        }
        // Construct he full path.
        final File definitionRoot = resourceManager.getDefinitionDirectory();
        final File finalPath = new File(definitionRoot, pathToResolve);
        // Store into a definition file.
        final WritableRdfJava writable = definitionDataUnit.asWritableRdfJava();
        writable.begin();
        writable.addUri(source, definitionResource.targetProperty, finalPath.toURI().toString());
        writable.commit();
    }

}
