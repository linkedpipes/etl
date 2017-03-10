package com.linkedpipes.etl.rdf.utils;

import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jTestUtils;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class Rdf4jTestUtilsTest {

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
        Assert.assertTrue(Rdf4jTestUtils.rdfEqual(
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
        Assert.assertTrue(Rdf4jTestUtils.rdfContains(
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
        Assert.assertFalse(Rdf4jTestUtils.rdfEqual(
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
        Assert.assertTrue(Rdf4jTestUtils.rdfEqual(
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
        Assert.assertTrue(Rdf4jTestUtils.rdfContains(
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
        Assert.assertFalse(Rdf4jTestUtils.rdfEqual(
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
        Assert.assertFalse(Rdf4jTestUtils.rdfEqual(
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
        Assert.assertFalse(Rdf4jTestUtils.rdfEqual(
                "rdf4jtestutilstest/quad.trig", actual));
    }

}
