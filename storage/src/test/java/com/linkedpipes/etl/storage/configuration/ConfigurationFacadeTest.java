package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
import com.linkedpipes.etl.storage.TestUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ConfigurationFacadeTest {

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void createNew() throws Exception {
        Collection<Statement> actual = ConfigurationFacade.createNewConfiguration(
                TestUtils.rdfFromResource("configuration/parentConfig.ttl"),
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base",
                valueFactory.createIRI("http://graph"),
                false
        );
        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/createNewExpected.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    /**
     * Inherit all is used with JarFiles configurations, where the inherit
     * property can be missing.
     */
    @Test
    public void createNewInheritAll() throws Exception {
        Collection<Statement> actual = ConfigurationFacade.createNewConfiguration(
                TestUtils.rdfFromResource("configuration/jarConfig.ttl"),
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base",
                valueFactory.createIRI("http://graph"),
                true
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/createNewInheritAllExpected.trig");
        Rdf4jUtils.rdfEqual(actual, expected);
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeTwoLevelInherit() throws Exception {
        List<Collection<Statement>> configurations = Arrays.asList(
                TestUtils.rdfFromResource("configuration/parentConfig.ttl"),
                TestUtils.rdfFromResource("configuration/childConfigInherit.ttl")
        );

        Collection<Statement> actual = ConfigurationFacade.merge(
                configurations,
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeTwoLevelInheritExpected.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeTwoLevelInheritAndForce() throws Exception {
        List<Collection<Statement>> configurations = Arrays.asList(
                TestUtils.rdfFromResource("configuration/parentConfig.ttl"),
                TestUtils.rdfFromResource("configuration/childConfigInheritAndForce.ttl")
        );

        Collection<Statement> actual = ConfigurationFacade.merge(
                configurations,
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeTwoLevelInheritAndForceExpected.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeTwoLevelNoControl() throws Exception {
        List<Collection<Statement>> configurations = Arrays.asList(
                TestUtils.rdfFromResource("configuration/parentConfig.ttl"),
                TestUtils.rdfFromResource("configuration/childConfigNoControl.ttl")
        );

        Collection<Statement> actual = ConfigurationFacade.merge(
                configurations,
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeTwoLevelNoControlExpected.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeTwoLevelNone() throws Exception {
        List<Collection<Statement>> configurations = Arrays.asList(
                TestUtils.rdfFromResource("configuration/parentConfig.ttl"),
                TestUtils.rdfFromResource("configuration/childConfigNone.ttl")
        );

        Collection<Statement> actual = ConfigurationFacade.merge(
                configurations,
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeTwoLevelNoneExpected.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeLevel() throws Exception {
        List<Collection<Statement>> configurations = Arrays.asList(
                TestUtils.rdfFromResource("configuration/level-0.ttl"),
                TestUtils.rdfFromResource("configuration/level-1.ttl"),
                Collections.emptyList(), // Add empty configuration.
                TestUtils.rdfFromResource("configuration/level-2.ttl")
        );

        Collection<Statement> actual = ConfigurationFacade.merge(
                configurations,
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/levelExpected.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeFromBottomInherit() throws Exception {
        Collection<Statement> actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/parentConfig.ttl"),
                TestUtils.rdfFromResource("configuration/childConfigInherit.ttl"),
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeFromBottomInheritExpected.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeFromBottomInheritAndForce() throws Exception {
        Collection<Statement> actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/parentConfig.ttl"),
                TestUtils.rdfFromResource("configuration/childConfigInheritAndForce.ttl"),
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeFromBottomInheritAndForceExpected.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeFromBottomNoControl() throws Exception {
        Collection<Statement> actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/parentConfig.ttl"),
                TestUtils.rdfFromResource("configuration/childConfigNoControl.ttl"),
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeFromBottomNoControlExpected.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeFromBottomNone() throws Exception {
        Collection<Statement> actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/parentConfig.ttl"),
                TestUtils.rdfFromResource("configuration/childConfigNone.ttl"),
                TestUtils.rdfFromResource("configuration/desc.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeFromBottomNoneExpected.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeFromBottomLevel() throws Exception {
        Collection<Statement> description =
                TestUtils.rdfFromResource("configuration/desc.ttl");
        Collection<Statement> actual;

        actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/level-1.ttl"),
                TestUtils.rdfFromResource("configuration/level-2.ttl"),
                description,
                "http://base", valueFactory.createIRI("http://graph")
        );

        actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/level-0.ttl"),
                actual,
                description,
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/levelExpected.trig");
        Rdf4jUtils.rdfEqual(actual, expected);
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeGlobalControlIafF() throws Exception {
        List<Collection<Statement>> configurations = Arrays.asList(
                TestUtils.rdfFromResource("configuration/globalControl.ttl"),
                TestUtils.rdfFromResource("configuration/globalControlInheritAndForce.ttl"),
                TestUtils.rdfFromResource("configuration/globalControlForce.ttl")
        );

        Collection<Statement> actual = ConfigurationFacade.merge(
                configurations,
                TestUtils.rdfFromResource("configuration/descWithComponentControl.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeGlobalControlIafF.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeGlobalControlFIaF() throws Exception {
        List<Collection<Statement>> configurations = Arrays.asList(
                TestUtils.rdfFromResource("configuration/globalControl.ttl"),
                TestUtils.rdfFromResource("configuration/globalControlForce.ttl"),
                TestUtils.rdfFromResource("configuration/globalControlInheritAndForce.ttl")
        );

        Collection<Statement> actual = ConfigurationFacade.merge(
                configurations,
                TestUtils.rdfFromResource("configuration/descWithComponentControl.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeGlobalControlFIaF.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeGlobalControlIIafF() throws Exception {
        List<Collection<Statement>> configurations = Arrays.asList(
                TestUtils.rdfFromResource("configuration/globalControl.ttl"),
                TestUtils.rdfFromResource("configuration/globalControlInherit.ttl"),
                TestUtils.rdfFromResource("configuration/globalControlInheritAndForce.ttl"),
                TestUtils.rdfFromResource("configuration/globalControlForce.ttl")
        );

        Collection<Statement> actual = ConfigurationFacade.merge(
                configurations,
                TestUtils.rdfFromResource("configuration/descWithComponentControl.ttl"),
                "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeGlobalControlIIafF.trig");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeFromBottomGlobalControlIafF() throws Exception {
        Collection<Statement> description =
                TestUtils.rdfFromResource("configuration/descWithComponentControl.ttl");
        Collection<Statement> actual;

        actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/globalControlInheritAndForce.ttl"),
                TestUtils.rdfFromResource("configuration/globalControlForce.ttl"),
                description, "http://base", valueFactory.createIRI("http://graph")
        );

        actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/globalControl.ttl"),
                actual,
                description, "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeGlobalControlIafF.trig");
        Rdf4jUtils.rdfEqual(actual, expected);
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeFromBottomGlobalControlFIaF() throws Exception {
        Collection<Statement> description =
                TestUtils.rdfFromResource("configuration/descWithComponentControl.ttl");
        Collection<Statement> actual;

        actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/globalControlForce.ttl"),
                TestUtils.rdfFromResource("configuration/globalControlInheritAndForce.ttl"),
                description, "http://base", valueFactory.createIRI("http://graph")
        );

        actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/globalControl.ttl"),
                actual,
                description, "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeGlobalControlFIaF.trig");
        Rdf4jUtils.rdfEqual(actual, expected);
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeFromBottomGlobalControlIIafF() throws Exception {
        Collection<Statement> description =
                TestUtils.rdfFromResource("configuration/descWithComponentControl.ttl");
        Collection<Statement> actual;

        actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/globalControlInheritAndForce.ttl"),
                TestUtils.rdfFromResource("configuration/globalControlForce.ttl"),
                description, "http://base", valueFactory.createIRI("http://graph")
        );

        actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/globalControlInherit.ttl"),
                actual,
                description, "http://base", valueFactory.createIRI("http://graph")
        );

        actual = ConfigurationFacade.mergeFromBottom(
                TestUtils.rdfFromResource("configuration/globalControl.ttl"),
                actual,
                description, "http://base", valueFactory.createIRI("http://graph")
        );

        Collection<Statement> expected = TestUtils.rdfFromResource(
                "configuration/mergeGlobalControlIIafF.trig");
        Rdf4jUtils.rdfEqual(actual, expected);
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

}
