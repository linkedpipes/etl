package com.linkedpipes.etl.library.template.configuration;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.TestUtils;
import com.linkedpipes.etl.library.template.plugin.adapter.rdf.RdfToConfigurationDescription;
import com.linkedpipes.etl.library.template.plugin.model.ConfigurationDescription;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class ConfigurationUtilsTest {

    private final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    @Test
    public void createNewFromJarFile() throws Exception {
        var data = TestUtils.statementsFromResource(
                "configuration/createNew.trig");
        var actual = ConfigurationFacade.createNewFromJarFile(
                TestUtils.selectGraph(data, "http://input"),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/jar")
        );
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
        var data = TestUtils.statementsFromResource(
                "configuration/createNew.trig");
        var actual = ConfigurationFacade.createNewFromTemplate(
                TestUtils.selectGraph(data, "http://input"),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/template")
        );
        var expected = TestUtils.selectGraph(data, "http://expected/template");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge000() throws Exception {
        var data = TestUtils.statementsFromResource(
                "configuration/merge-000.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://level-0"),
                        TestUtils.selectGraph(data, "http://level-1"),
                        TestUtils.selectGraph(data, "http://level-2")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void merge001() throws Exception {
        var data = TestUtils.statementsFromResource(
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
                VALUE_FACTORY.createIRI(
                        "http://expected")
        );
        var expected = TestUtils.selectGraph(
                data, "http://loexpected");
        TestUtils.assertIsomorphic(actual, expected);
    }


    @Test
    public void mergeTwoLevelInherit() throws Exception {
        var data = TestUtils.statementsFromResource(
                "configuration/mergeTwoLevels.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inherit")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/parent_inherit")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inherit");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void mergeTwoLevelInheritAndForce() throws Exception {
        var data = TestUtils.statementsFromResource(
                "configuration/mergeTwoLevels.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inheritAndForce")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/parent_inheritAndForce")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inheritAndForce");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void mergeTwoLevelNoControl() throws Exception {
        var data = TestUtils.statementsFromResource(
                "configuration/mergeTwoLevels.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://noControl")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/parent_noControl")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_noControl");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void mergeTwoLevelNone() throws Exception {
        var data = TestUtils.statementsFromResource(
                "configuration/mergeTwoLevels.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://none")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI("http://expected/parent_none")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_none");
        TestUtils.assertIsomorphic(actual, expected);
    }


    @Test
    public void mergeGlobalControlIafF() throws Exception {
        var data = TestUtils.statementsFromResource(
                "configuration/mergeWithGlobal.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://inheritAndForce"),
                        TestUtils.selectGraph(data, "http://force")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI(
                        "http://expected/parent_inheritAndForce_force"
                )
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inheritAndForce_force");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void mergeGlobalControlIIafF() throws Exception {
        var data = TestUtils.statementsFromResource(
                "configuration/mergeWithGlobal.trig");
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
                )
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_inherit_inheritAndForce_force");
        TestUtils.assertIsomorphic(actual, expected);
    }

    @Test
    public void mergeGlobalControlFIaF() throws Exception {
        var data = TestUtils.statementsFromResource(
                "configuration/mergeWithGlobal.trig");
        var actual = ConfigurationFacade.merge(
                Arrays.asList(
                        TestUtils.selectGraph(data, "http://parent"),
                        TestUtils.selectGraph(data, "http://force"),
                        TestUtils.selectGraph(data, "http://inheritAndForce")
                ),
                loadDescription(data, "http://description"),
                "http://base",
                VALUE_FACTORY.createIRI(
                        "http://expected/parent_force_inheritAndForce")
        );
        var expected = TestUtils.selectGraph(
                data, "http://expected/parent_force_inheritAndForce");
        TestUtils.assertIsomorphic(actual, expected);
    }

}
