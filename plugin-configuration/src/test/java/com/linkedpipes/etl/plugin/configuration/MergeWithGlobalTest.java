package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MergeWithGlobalTest {

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void mergeGlobalControlIafF() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("mergeWithGlobal.trig");
        var actual = facade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inheritAndForce"),
                        TestUtils.selectGraph(data, "http://force")
                ),
                TestUtils.selectGraph(data, "http://description"),
                "http://base",
                valueFactory.createIRI(
                        "http://expected/parent_inheritAndForce_force"
                )
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inheritAndForce_force");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void mergeGlobalControlIIafF() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("mergeWithGlobal.trig");
        var actual = facade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inherit"),
                        TestUtils.selectGraph(data, "http://inheritAndForce"),
                        TestUtils.selectGraph(data, "http://force")
                ),
                TestUtils.selectGraph(data, "http://description"),
                "http://base",
                valueFactory.createIRI(
                        "http://expected/parent_inherit_inheritAndForce_force"
                )
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inherit_inheritAndForce_force");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void mergeGlobalControlFIaF() throws Exception {
        ConfigurationFacade facade = new ConfigurationFacade();
        var data = TestUtils.statementsFromResource("mergeWithGlobal.trig");
        var actual = facade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://force"),
                        TestUtils.selectGraph(data, "http://inheritAndForce")
                ),
                TestUtils.selectGraph(data, "http://description"),
                "http://base",
                valueFactory.createIRI(
                        "http://expected/parent_force_inheritAndForce")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_force_inheritAndForce");
        TestUtils.assertIsomorphic(actual, expected);
    }


}
