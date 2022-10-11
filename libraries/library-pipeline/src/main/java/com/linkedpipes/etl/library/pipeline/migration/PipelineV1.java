package com.linkedpipes.etl.library.pipeline.migration;

import com.linkedpipes.etl.library.pipeline.adapter.RawPipeline;
import com.linkedpipes.etl.library.pipeline.adapter.RawPipelineComponent;
import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.HashMap;
import java.util.Map;

/**
 * Some components shared configuration types. This cause issues
 * when additional properties were added to one of the configurations.
 *
 * <p>The type is used to match the description. This cause that wrong
 * description may be used to merge the configuration. This result
 * in removal of some properties.
 *
 * <p>This bug depends on the order of components.
 *
 * <p>As a solution the configuration of the components is changed to use
 * different vocabularies.
 *
 * <p>Another thing is that a reference to a configuration description is
 * added to definition of reference templates.
 */
public class PipelineV1 {

    private record Mapping(String source, String target) {

    }

    private static final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    private static final Map<String, Mapping> MAPPING;

    static {
        MAPPING = new HashMap<>();

        String prefix = "http://etl.linkedpipes.com/resources/components/";
        String suffix = "/0.0.0";

        MAPPING.put(prefix + "l-sparqlEndpointChunked" + suffix,
                new Mapping("l-sparqlEndpoint", "l-sparqlEndpointChunked"));
        MAPPING.put(prefix + "t-filesToRdfChunked" + suffix,
                new Mapping("t-filesToRdf", "t-filesToRdfChunked"));
        MAPPING.put(prefix + "t-sparqlConstructChunked" + suffix,
                new Mapping("t-sparqlConstruct", "t-sparqlConstructChunked"));
        MAPPING.put(prefix + "t-filesToRdfGraph" + suffix,
                new Mapping("t-filesToRdf", "t-filesToRdfGraph"));
        MAPPING.put(prefix + "t-filesToRdfChunked" + suffix,
                new Mapping("t-filesToRdf", "t-filesToRdfChunked"));
        MAPPING.put(prefix + "t-mustacheChunked" + suffix,
                new Mapping("t-mustache", "t-mustacheChunked"));
        MAPPING.put(prefix + "t-sparqlUpdateChunked" + suffix,
                new Mapping("t-sparqlUpdate", "t-sparqlUpdateChunked"));
        MAPPING.put(prefix + "t-tabularChunked" + suffix,
                new Mapping("t-tabular", "t-tabularChunked"));
    }

    private final Map<Resource, Resource> templateToPlugin;

    public PipelineV1(Map<Resource, Resource> templateToPlugin) {
        this.templateToPlugin = templateToPlugin;
    }

    public void migrateToV2(RawPipeline pipeline) throws PipelineMigrationFailed {
        for (RawPipelineComponent component : pipeline.components) {
            migrateComponent(component);
        }
    }

    private void migrateComponent(RawPipelineComponent component)
            throws PipelineMigrationFailed {
        Resource plugin = getPlugin(component.template);
        if (!shouldUpdate(plugin)) {
            return;
        }
        if (!hasConfiguration(component)) {
            return;
        }
        component.configuration = updateConfiguration(
                component.configuration, plugin);
    }

    private Resource getPlugin(Resource template) throws PipelineMigrationFailed {
        Resource result = templateToPlugin.get(template);
        if (result == null) {
            throw new PipelineMigrationFailed(
                    "Missing plugin for template: " + template);
        }
        return result;
    }

    public static boolean shouldUpdate(Resource template) {
        return MAPPING.containsKey(template.stringValue());
    }

    public static boolean hasConfiguration(RawPipelineComponent component) {
        return !component.configuration.isEmpty();
    }

    public static Statements updateConfiguration(
            Statements statements,
            Resource coreTemplate) {
        Mapping mapping = MAPPING.get(coreTemplate);
        if (mapping == null) {
            return statements;
        }
        return Statements.wrap(statements.stream().map((statement) -> {
            if (statement.getPredicate().equals(RDF.TYPE)) {
                return valueFactory.createStatement(
                        statement.getSubject(),
                        statement.getPredicate(),
                        updateIRI((IRI) statement.getObject(), mapping),
                        statement.getContext()
                );
            }
            return valueFactory.createStatement(
                    statement.getSubject(),
                    updateIRI(statement.getPredicate(), mapping),
                    statement.getObject(),
                    statement.getContext());
        }).toList());
    }

    private static IRI updateIRI(IRI source, Mapping mapping) {
        return valueFactory.createIRI(source.stringValue()
                .replace(mapping.source, mapping.target));
    }

}
