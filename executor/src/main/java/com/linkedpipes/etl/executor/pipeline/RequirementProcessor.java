package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.executor.rdf.PojoLoader;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.Repositories;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Find requirements in pipeline definition and handle them.
 *
 * @author Škoda Petr
 */
class RequirementProcessor {

    public static class ProcessingFailed extends Exception {

        public ProcessingFailed(Throwable cause) {
            super(cause);
        }

    }

    private static class TempDirectory implements PojoLoader.Loadable {

        private String targetProperty;

        @Override
        public PojoLoader.Loadable load(String predicate, Value object)
                throws LpException {
            switch (predicate) {
                case LINKEDPIPES.REQUIREMENTS.HAS_TARGET_PROPERTY:
                    targetProperty = object.stringValue();
                    return null;
                default:
                    return null;
            }
        }

    }

    private static class InputDirectory implements PojoLoader.Loadable {

        private String targetProperty;

        @Override
        public PojoLoader.Loadable load(String predicate, Value object)
                throws LpException {
            switch (predicate) {
                case LINKEDPIPES.REQUIREMENTS.HAS_TARGET_PROPERTY:
                    targetProperty = object.stringValue();
                    return null;
                default:
                    return null;
            }
        }

    }

    private RequirementProcessor() {
    }

    public static void handle(PipelineDefinition definition,
            ResourceManager resourceManager) throws ProcessingFailed {

        final String query = "SELECT ?source ?requirement ?type ?value"
                + " WHERE {\n"
                + "  ?source <"
                + LINKEDPIPES.HAS_REQUIREMENT + "> ?requirement .\n"
                + "  ?requirement a <"
                + LINKEDPIPES.REQUIREMENTS.REQUIREMENT + "> ;"
                + "    a ?type. \n"
                + "  OPTIONAL {\n"
                + "    ?requirement <"
                + LINKEDPIPES.REQUIREMENTS.HAS_SOURCE_PROPERTY + "> ?uri. \n"
                + "    ?source ?uri ?value. \n"
                + "  }\n"
                + "}";

        final List<Map<String, String>> queryResult
                = definition.executeSelect(query);
        //
        for (Map<String, String> item : queryResult) {
            switch (item.get("type")) {
                case LINKEDPIPES.REQUIREMENTS.TEMP_DIRECTORY:
                    handleTempDirectory(
                            definition,
                            resourceManager,
                            item.get("requirement"),
                            item.get("source"));
                    break;
                case LINKEDPIPES.REQUIREMENTS.INPUT_DIRECTORY:
                    handleInputDirectory(
                            definition,
                            resourceManager,
                            item.get("requirement"),
                            item.get("source"));
                    break;
                default:
                    break;
            }
        }

    }

    private static void handleTempDirectory(PipelineDefinition definition,
            ResourceManager resourceManager, String requirement, String source)
            throws ProcessingFailed {
        // Read requirements.
        final TempDirectory tempDirectory = new TempDirectory();
        try {
            PojoLoader.load(
                    definition.getRepository(),
                    requirement,
                    definition.getDefinitionGraph(),
                    tempDirectory);
        } catch (LpException ex) {
            throw new ProcessingFailed(ex);
        }
        // Add triple with path to the temp directory.
        final File workingDir = resourceManager.getWorkingDirectory("temp");
        try {
            Repositories.consume(definition.getRepository(), (connection) -> {
                final ValueFactory vf = SimpleValueFactory.getInstance();
                connection.add(vf.createStatement(
                        vf.createIRI(source),
                        vf.createIRI(tempDirectory.targetProperty),
                        vf.createIRI(workingDir.toURI().toString())),
                        vf.createIRI(definition.getDefinitionGraph()));
            });
        } catch (RepositoryException ex) {
            throw new ProcessingFailed(ex);
        }
    }

    private static void handleInputDirectory(PipelineDefinition definition,
            ResourceManager resourceManager, String requirement, String source)
            throws ProcessingFailed {
        // Read requirements.
        final InputDirectory inputDirectory = new InputDirectory();
        try {
            PojoLoader.load(
                    definition.getRepository(),
                    requirement,
                    definition.getDefinitionGraph(),
                    inputDirectory);
        } catch (LpException ex) {
            throw new ProcessingFailed(ex);
        }
        // Add triple with path to the temp directory.
        final File inputDir = resourceManager.getInputDirectory();
        try {
            Repositories.consume(definition.getRepository(), (connection) -> {
                final ValueFactory vf = SimpleValueFactory.getInstance();
                connection.add(vf.createStatement(
                        vf.createIRI(source),
                        vf.createIRI(inputDirectory.targetProperty),
                        vf.createIRI(inputDir.toURI().toString())),
                        vf.createIRI(definition.getDefinitionGraph()));
            });
        } catch (RepositoryException ex) {
            throw new ProcessingFailed(ex);
        }
    }

}
