package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import java.util.Collection;

class EmptyPipelineFactory {

    private static final ValueFactory VALUE_FACTORY =
            SimpleValueFactory.getInstance();

    private static IRI HAS_PROFILE;

    private static IRI PROFILE;

    private static IRI HAS_RDF_REPOSITORY_POLICY;

    private static IRI SINGLE_REPOSITORY;

    private static IRI HAS_RDF_REPOSITORY_TYPE;

    private static IRI NATIVE_STORE;

    static {
        HAS_PROFILE = VALUE_FACTORY.createIRI(LP_PIPELINE.HAS_PROFILE);
        PROFILE = VALUE_FACTORY.createIRI(LP_PIPELINE.PROFILE);
        HAS_RDF_REPOSITORY_POLICY = VALUE_FACTORY.createIRI(
                LP_PIPELINE.HAS_RDF_REPOSITORY_POLICY);
        SINGLE_REPOSITORY = VALUE_FACTORY.createIRI(
                LP_PIPELINE.SINGLE_REPOSITORY);
        HAS_RDF_REPOSITORY_TYPE = VALUE_FACTORY.createIRI(
                LP_PIPELINE.HAS_RDF_REPOSITORY_TYPE);
        NATIVE_STORE = VALUE_FACTORY.createIRI(LP_PIPELINE.NATIVE_STORE);
    }

    public static Collection<Statement> create(IRI iri) {
        String iriAsStr = iri.stringValue();
        Model model = new LinkedHashModel();
        model.add(iri, RDF.TYPE, Pipeline.TYPE, iri);
        Value version = VALUE_FACTORY.createLiteral(Pipeline.VERSION_NUMBER);
        model.add(iri, Pipeline.HAS_VERSION, version, iri);
        Value label = VALUE_FACTORY.createLiteral(iriAsStr);
        model.add(iri, SKOS.PREF_LABEL, label, iri);
        IRI profileIri = VALUE_FACTORY.createIRI(
                iriAsStr + "/profile/default");
        model.add(iri, HAS_PROFILE, profileIri, iri);

        model.add(profileIri, RDF.TYPE, PROFILE, iri);

        model.add(
                profileIri, HAS_RDF_REPOSITORY_POLICY, SINGLE_REPOSITORY, iri);
        model.add(profileIri, HAS_RDF_REPOSITORY_TYPE, NATIVE_STORE, iri);

        return model;
    }

}
