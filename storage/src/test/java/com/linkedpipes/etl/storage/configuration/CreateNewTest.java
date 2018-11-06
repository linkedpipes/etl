package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.rdf4j.Statements;
import com.linkedpipes.etl.storage.TestUtils;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CreateNewTest {

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private CreateNewConfiguration worker = new CreateNewConfiguration();

    private Statements data = Statements.arrayList();

    private Description description;

    @Before
    public void initialize() throws Exception {
        data.addAll(TestUtils.file("configuration/createNew.trig"));
        this.description = Description.fromStatements(
                data.selectByGraph("http://description"));
    }

    @Test
    public void createNewFromJarFile() {
        Statements actual = worker.createNewFromJarFile(
                data.selectByGraph("http://input"),
                description,
                "http://base",
                valueFactory.createIRI("http://expected/jar")
        );
        Statements expected = data.selectByGraph(
                "http://expected/jar");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void createNewFromTemplate() {
        Statements actual = worker.createNewFromTemplate(
                data.selectByGraph("http://input"),
                description,
                "http://base",
                valueFactory.createIRI("http://expected/template")
        );
        Statements expected = data.selectByGraph(
                "http://expected/template");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

}
