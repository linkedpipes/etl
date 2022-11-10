package com.linkedpipes.etl.library.rdf;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class StatementsTest {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void emptyEquals() {
        var left = Statements.empty();
        var right = Statements.empty();
        Assertions.assertEquals(left, right);
    }

    @Test
    public void equalsWrap() {
        List<Statement> content = new ArrayList<>();
        content.add(valueFactory.createStatement(
                valueFactory.createBNode(),
                SKOS.PREF_LABEL,
                valueFactory.createLiteral("label")));
        var left = Statements.wrap(content);
        var right = Statements.wrap(content);
        Assertions.assertEquals(left, right);
    }

    @Test
    public void equalsContent() {
        var left = Statements.arrayList();
        left.add(valueFactory.createStatement(
                valueFactory.createIRI("http://example.com"),
                SKOS.PREF_LABEL,
                valueFactory.createLiteral("label")));
        var right = Statements.arrayList();
        right.add(valueFactory.createStatement(
                valueFactory.createIRI("http://example.com"),
                SKOS.PREF_LABEL,
                valueFactory.createLiteral("label")));
        Assertions.assertEquals(left, right);
    }

}
