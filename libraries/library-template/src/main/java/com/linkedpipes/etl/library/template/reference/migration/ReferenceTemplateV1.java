package com.linkedpipes.etl.library.template.reference.migration;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ReferenceTemplateV1 {

    private record Mapping(String source, String target) {

    }

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

    public ReferenceTemplateV1(Map<Resource, Resource> templateToPlugin) {
        this.templateToPlugin = templateToPlugin;
    }

    /**
     * There used to be a configuration description stored, now there this
     * is not allowed for reference templates. It is thus not needed
     * to update it.
     * <p>
     * In addition, some templates also shared parts of configuration
     * vocabulary, especially chunked versions of the components.
     */
    public void migrateToV2(RawReferenceTemplate template)
            throws ReferenceMigrationFailed {
        if (template.template == null) {
            throw new ReferenceMigrationFailed(
                    "Missing template for '{}'.", template.resource);
        }
        Resource root = templateToPlugin.get(template.template);
        if (root == null) {
            throw new ReferenceMigrationFailed(
                    "Missing root template '{}' for '{}'.",
                    template.template, template.resource);
        }
        Mapping mapping = MAPPING.get(root.stringValue());
        if (mapping == null) {
            return;
        }
        template.configuration = Statements.wrap(
                updateConfiguration(template.configuration, mapping));
        template.version = 2;
    }

    protected Collection<Statement> updateConfiguration(
            Collection<Statement> statements, Mapping mapping) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        return statements.stream().map((statement) -> {
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
        }).collect(Collectors.toList());
    }

    protected IRI updateIRI(IRI source, Mapping mapping) {
        return SimpleValueFactory.getInstance().createIRI(
                source.stringValue().replace(
                        mapping.source(), mapping.target()));
    }

}
