package com.linkedpipes.etl.rdf.utils;

import com.linkedpipes.etl.rdf.rdf4j.Rdf4jUtils;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class Rdf4jUtilsTest {

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void tripleMatch() {
        List<Statement> actual = new LinkedList<>();
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(RDF.TYPE),
                valueFactory.createIRI("http://localhost/ontolog/Type")
        ));
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(SKOS.PREF_LABEL),
                valueFactory.createLiteral("Resource 01")
        ));
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/02"),
                valueFactory.createIRI(SKOS.PREF_LABEL),
                valueFactory.createLiteral("Resource 02")
        ));
        Assertions.assertTrue(Rdf4jUtils.rdfEqual(
                "rdf4jtestutilstest/triple.ttl", actual));
    }

    @Test
    public void tripleContains() {
        List<Statement> actual = new LinkedList<>();
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(RDF.TYPE),
                valueFactory.createIRI("http://localhost/ontolog/Type")
        ));
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(SKOS.PREF_LABEL),
                valueFactory.createLiteral("Resource 01")
        ));
        Assertions.assertTrue(Rdf4jUtils.rdfContains(
                "rdf4jtestutilstest/triple.ttl", actual));
    }

    @Test
    public void tripleMissing() {
        List<Statement> actual = new LinkedList<>();
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(RDF.TYPE),
                valueFactory.createIRI("http://localhost/ontolog/Type")
        ));
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/02"),
                valueFactory.createIRI(SKOS.PREF_LABEL),
                valueFactory.createLiteral("Resource 02")
        ));
        Assertions.assertFalse(Rdf4jUtils.rdfEqual(
                "rdf4jtestutilstest/triple.ttl", actual));
    }

    @Test
    public void quadsMatch() {
        List<Statement> actual = new LinkedList<>();
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(RDF.TYPE),
                valueFactory.createIRI("http://localhost/ontolog/Type"),
                valueFactory.createIRI("http://localhost/graph/01")
        ));
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(SKOS.PREF_LABEL),
                valueFactory.createLiteral("Resource 01"),
                valueFactory.createIRI("http://localhost/graph/01")
        ));
        Assertions.assertTrue(Rdf4jUtils.rdfEqual(
                "rdf4jtestutilstest/quad.trig", actual));
    }

    @Test
    public void quadsContains() {
        List<Statement> actual = new LinkedList<>();
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(RDF.TYPE),
                valueFactory.createIRI("http://localhost/ontolog/Type"),
                valueFactory.createIRI("http://localhost/graph/01")
        ));
        Assertions.assertTrue(Rdf4jUtils.rdfContains(
                "rdf4jtestutilstest/quad.trig", actual));
    }

    @Test
    public void quadsMissing() {
        List<Statement> actual = new LinkedList<>();
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(RDF.TYPE),
                valueFactory.createIRI("http://localhost/ontolog/Type"),
                valueFactory.createIRI("http://localhost/graph/01")
        ));
        Assertions.assertFalse(Rdf4jUtils.rdfEqual(
                "rdf4jtestutilstest/quad.trig", actual));
    }

    @Test
    public void quadsInvalidGraph() {
        List<Statement> actual = new LinkedList<>();
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(RDF.TYPE),
                valueFactory.createIRI("http://localhost/ontolog/Type"),
                valueFactory.createIRI("http://localhost/graph/01")
        ));
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(SKOS.PREF_LABEL),
                valueFactory.createLiteral("Resource 01"),
                valueFactory.createIRI("http://localhost/graph/02")
        ));
        Assertions.assertFalse(Rdf4jUtils.rdfEqual(
                "rdf4jtestutilstest/quad.trig", actual));
    }

    @Test
    public void quadsMissingGraph() {
        List<Statement> actual = new LinkedList<>();
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(RDF.TYPE),
                valueFactory.createIRI("http://localhost/ontolog/Type"),
                valueFactory.createIRI("http://localhost/graph/01")
        ));
        actual.add(valueFactory.createStatement(
                valueFactory.createIRI("http://localhost/resource/01"),
                valueFactory.createIRI(SKOS.PREF_LABEL),
                valueFactory.createLiteral("Resource 01")
        ));
        Assertions.assertFalse(Rdf4jUtils.rdfEqual(
                "rdf4jtestutilstest/quad.trig", actual));
    }

}
