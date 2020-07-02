package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

public class CreateNewTest {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void createNewFromJarFile() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("createNew.trig");
        var actual = facade.createNewFromJarFile(
                TestUtils.selectGraph(data, "http://input"),
                TestUtils.selectGraph(data, "http://description"),
                "http://base",
                valueFactory.createIRI("http://expected/jar")
        );
        var expected = TestUtils.selectGraph(data, "http://expected/jar");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void createNewFromTemplate() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("createNew.trig");
        var actual = facade.createNewFromTemplate(
                TestUtils.selectGraph(data, "http://input"),
                TestUtils.selectGraph(data, "http://description"),
                "http://base",
                valueFactory.createIRI("http://expected/template")
        );
        var expected = TestUtils.selectGraph(data, "http://expected/template");
        TestUtils.assertIsomorphic(actual, expected);
    }

}
