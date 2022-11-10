package com.linkedpipes.etl.library.template.configuration;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.TestUtils;
import com.linkedpipes.etl.library.template.configuration.adapter.RdfToConfigurationDescription;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class ConfigurationFacadeTest {

    private final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    @Test
    public void createNewFromJarFile() throws Exception {
        var data = TestUtils.statements(
                "configuration/create-new.trig");
        var actual = ConfigurationFacade.createNewFromJarFile(
                TestUtils.selectGraph(data, "http://input"),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/jar"));
        var expected = TestUtils.selectGraph(data, "http://expected/jar");
        TestUtils.assertIsomorphic(actual, expected);
    }

    private ConfigurationDescription loadDescription(
            List<Statement> statements, String graph) {
        List<Statement> relevantStatements =
                TestUtils.selectGraph(statements, graph);
        var candidates =
                RdfToConfigurationDescription.asConfigurationDescriptions(
                        Statements.wrap(relevantStatements).selector());
        Assertions.assertEquals(1, candidates.size());
        return candidates.get(0);
    }

    @Test
    public void createNewFromTemplate() throws Exception {
        var data = TestUtils.statements(
                "configuration/create-new.trig");
        var actual = ConfigurationFacade.createNewFromTemplate(
                TestUtils.selectGraph(data, "http://input"),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/template"));
        var expected = TestUtils.selectGraph(data, "http://expected/template");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge000() throws Exception {
        var data = TestUtils.statements(
                "configuration/merge-000.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://level-0"),
                        TestUtils.selectGraph(data, "http://level-1"),
                        TestUtils.selectGraph(data, "http://level-2")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected"));
        var expected = TestUtils.selectGraph(
                data, "http://expected");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge001() throws Exception {
        var data = TestUtils.statements(
                "configuration/merge-001.trig");
        var description = loadDescription(
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
        var actual = ConfigurationFacade.merge(
                configurations,
                description,
                "http://localhost/pipeline/3405c1ee/configuration",
                VALUE_FACTORY.createIRI("http://expected"));
        var expected = TestUtils.selectGraph(
                data, "http://loexpected");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge002Inherit() throws Exception {
        var data = TestUtils.statements(
                "configuration/merge-002.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inherit")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/parent_inherit"));
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inherit");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge002InheritAndForce() throws Exception {

        var data = TestUtils.statements(
                "configuration/merge-002.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inheritAndForce")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI(
                        "http://expected/parent_inheritAndForce"));
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inheritAndForce");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge002NoControl() throws Exception {
        var data = TestUtils.statements(
                "configuration/merge-002.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://noControl")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/parent_noControl"));
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_noControl");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge002None() throws Exception {

        var data = TestUtils.statements(
                "configuration/merge-002.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://none")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/parent_none"));
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_none");
        TestUtils.assertIsomorphic(actual, expected);
    }


    @Test
    public void mergeGlobalControlIafF() throws Exception {

        var data = TestUtils.statements(
                "configuration/merge-003.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inheritAndForce"),
                        TestUtils.selectGraph(data, "http://force")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI(
                        "http://expected/parent_inheritAndForce_force"));
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inheritAndForce_force");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge003IIafF() throws Exception {

        var data = TestUtils.statements(
                "configuration/merge-003.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inherit"),
                        TestUtils.selectGraph(data, "http://inheritAndForce"),
                        TestUtils.selectGraph(data, "http://force")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI(
                        "http://expected/parent_inherit_inheritAndForce_force"
                ));
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inherit_inheritAndForce_force");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge003FIaF() throws Exception {

        var data = TestUtils.statements(
                "configuration/merge-003.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://force"),
                        TestUtils.selectGraph(data, "http://inheritAndForce")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI(
                        "http://expected/parent_force_inheritAndForce"));
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_force_inheritAndForce");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void localizeConfiguration001() throws Exception {
        var data = TestUtils.statements(
                "configuration/localize-001.trig");
        var actual = ConfigurationFacade.localizeConfiguration(
                loadDescription(data, "http://description"),
                Statements.wrap(TestUtils.selectGraph(data, "http://input"))
                        .selector(),
                VALUE_FACTORY.createIRI("http://localhost/expected"));
        var expected =
                Statements.wrap(TestUtils.selectGraph(data, "http://expected"))
                        .withoutGraph().asList();
        TestUtils.assertIsomorphic(actual.asList(), expected);
    }

    @Test
    public void removePrivate() throws Exception {
        var data = TestUtils.statements(
                "configuration/remove-private.trig");
        var actual = ConfigurationFacade.removePrivateStatements(
                Statements.wrap(TestUtils.selectGraph(data, "http://input"))
                        .withoutGraph().selector(),
                loadDescription(data, "http://description"));
        var expected =
                Statements.wrap(TestUtils.selectGraph(data, "http://expected"))
                        .withoutGraph().asList();
        TestUtils.assertIsomorphic(expected, actual);
    }

}
