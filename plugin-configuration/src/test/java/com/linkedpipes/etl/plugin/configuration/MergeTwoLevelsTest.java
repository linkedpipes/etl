package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MergeTwoLevelsTest {

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void mergeTwoLevelInherit() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("mergeTwoLevels.trig");
        var actual = facade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inherit")
                ),
                TestUtils.selectGraph(data, "http://description"),
                "http://base",
                valueFactory.createIRI("http://expected/parent_inherit")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inherit");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void mergeTwoLevelInheritAndForce() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("mergeTwoLevels.trig");
        var actual = facade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inheritAndForce")
                ),
                TestUtils.selectGraph(data, "http://description"),
                "http://base",
                valueFactory.createIRI("http://expected/parent_inheritAndForce")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inheritAndForce");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void mergeTwoLevelNoControl() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("mergeTwoLevels.trig");
        var actual = facade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://noControl")
                ),
                TestUtils.selectGraph(data, "http://description"),
                "http://base",
                valueFactory.createIRI("http://expected/parent_noControl")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_noControl");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void mergeTwoLevelNone() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("mergeTwoLevels.trig");
        var actual = facade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://none")
                ),
                TestUtils.selectGraph(data, "http://description"),
                "http://base",
                valueFactory.createIRI("http://expected/parent_none")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_none");
        TestUtils.assertIsomorphic(actual, expected);
    }

}
