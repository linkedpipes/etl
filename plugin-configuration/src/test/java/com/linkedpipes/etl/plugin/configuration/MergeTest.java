package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MergeTest {

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void merge000() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("merge-000.trig");
        var actual = facade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://level-0"),
                        TestUtils.selectGraph(data, "http://level-1"),
                        TestUtils.selectGraph(data, "http://level-2")
                ),
                TestUtils.selectGraph(data, "http://description"),
                "http://base",
                valueFactory.createIRI("http://expected")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge001() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("merge-001.trig");
        var description = TestUtils.selectGraph(
                data,
                "http://linkedpipes.com/resources/components/"
                        + "e-textHolder/0.0.0/configuration/desc");
        var configurations = Arrays.asList(
                TestUtils.selectGraph(data,
                        "http://linkedpipes.com/resources/components/"
                                + "e-textHolder/0.0.0/configuration"),
                TestUtils.selectGraph(data,
                        "http://localhost:8080/resources/components/"
                                + "1476168977169/configuration"),
                TestUtils.selectGraph(data,
                        "http://localhost:8080/resources/components/"
                                + "1476115743178/configuration"),
                TestUtils.selectGraph(data,
                        "http://localhost/pipeline/3405c1ee/configuration")
        );
        var actual = facade.merge(
                configurations,
                description,
                "http://localhost/pipeline/3405c1ee/configuration",
                valueFactory.createIRI(
                        "http://expected")
        );
        var expected = TestUtils.selectGraph(
                data, "http://loexpected");
        TestUtils.assertIsomorphic(actual, expected);
    }
}
