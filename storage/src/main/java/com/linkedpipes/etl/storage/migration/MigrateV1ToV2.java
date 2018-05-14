package com.linkedpipes.etl.storage.migration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import com.linkedpipes.etl.storage.rdf.StatementsCollection;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Some components shared configuration types. This cause issues
 * when additional properties were added to one of the configurations.
 *
 * The type is used to match the description. This cause that wrong
 * description may be used to merge the configuration. This result
 * in removal of some properties.
 *
 * This bug depends on the order of components.
 *
 * As a solution the configuration of the components is changed to use
 * different vocabularies.
 *
 * Another thing is that a reference to a configuration description is
 * added to definition of reference templates.
 */
public class MigrateV1ToV2 {

    private static class Mapping {

        private final String source;

        private final String target;

        public Mapping(String source, String target) {
            this.source = source;
            this.target = target;
        }
    }

    private static final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    private static final IRI COMPONENT;

    private static final IRI HAS_TEMPLATE;

    private static final IRI HAS_CONFIGURATION;

    private static final Map<String, Mapping> MAPPING;

    static {
        COMPONENT = valueFactory.createIRI(LP_PIPELINE.COMPONENT);
        HAS_TEMPLATE = valueFactory.createIRI(LP_PIPELINE.HAS_TEMPLATE);
        HAS_CONFIGURATION = valueFactory.createIRI(
                LP_PIPELINE.HAS_CONFIGURATION_GRAPH);
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

    private final TemplateFacade templateFacade;

    private StatementsCollection configurations;

    public MigrateV1ToV2(TemplateFacade templateFacade) {
        this.templateFacade = templateFacade;
    }

    public void pipeline(
            StatementsCollection configurations,
            RdfObjects pipeline) {
        this.configurations = configurations;
        for (RdfObjects.Entity component : pipeline.getTyped(COMPONENT)) {
            migrateComponent(component);
        }
    }

    private void migrateComponent(RdfObjects.Entity component) {
        String template = component.getReference(HAS_TEMPLATE)
                .getResource().stringValue();
        String coreTemplate = getCoreTemplate(template);
        if (shouldUpdate(coreTemplate)) {
            String configuration = component.getReference(HAS_CONFIGURATION)
                    .getResource().stringValue();
            updateConfigurations(configuration, coreTemplate);
        }
    }

    public static boolean shouldUpdate(String iri) {
        return MAPPING.containsKey(iri);
    }

    private String getCoreTemplate(String iri) {
        Template template = templateFacade.getTemplate(iri);
        return templateFacade.getRootTemplate(template).getIri();
    }

    private void updateConfigurations(String graph, String coreTemplate) {
        Collection<Statement> toUpdate = configurations.getStatements()
                .stream()
                .filter(st -> st.getContext().stringValue().equals(graph))
                .collect(Collectors.toList());
        configurations.remove(toUpdate);
        configurations.addAll(updateConfiguration(toUpdate, coreTemplate));
    }

    public static Collection<Statement> updateConfiguration(
            Collection<Statement> statements,
            String coreTemplate) {
        Mapping mapping = MAPPING.get(coreTemplate);
        if (mapping == null) {
            return statements;
        }
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

    private static IRI updateIRI(IRI source, Mapping mapping) {
        return valueFactory.createIRI(source.stringValue()
                .replace(mapping.source, mapping.target));
    }

}
