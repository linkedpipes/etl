package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.rdf4j.Statements;
import com.linkedpipes.etl.storage.TestUtils;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MergeTwoLevelsTest {

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private MergeHierarchy worker = new MergeHierarchy();

    private Statements data = Statements.ArrayList();

    private Description description;

    @Before
    public void initialize() throws Exception {
        data.addAll(TestUtils.file("configuration/mergeTwoLevels.trig"));
        this.description = Description.fromStatements(
                data.selectByGraph("http://description"));
    }

    @Test
    public void mergeTwoLevelInherit() throws Exception {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://inherit")
        );

        Statements actual = this.worker.merge(
                configurations,
                this.description,
                "http://base",
                this.valueFactory.createIRI("http://expected/parent_inherit")
        );
        Statements expected = this.data.selectByGraph(
                "http://expected/parent_inherit");
        Assert.assertTrue(Models.isomorphic(expected, actual));
    }

    @Test
    public void mergeTwoLevelInheritFromBottom() {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://inherit")
        );

        Statements actual = merge(configurations,
                "http://expected/parent_inherit_bottom");

        Statements expected = this.data.selectByGraph(
                "http://expected/parent_inherit_bottom");
        Assert.assertTrue(Models.isomorphic(expected, actual));
    }

    private Statements merge(List<Statements> configurations, String graph) {
        MergeFromBottom worker = new MergeFromBottom();
        Statements actual = configurations.get(configurations.size() - 1);
        for (int index = configurations.size() - 2; index >= 0; --index) {
            Statements parent = configurations.get(index);
            actual = worker.merge(
                    parent,
                    actual,
                    this.description,
                    "http://base",
                    this.valueFactory.createIRI(graph)
            );
        }
        return worker.finalize(actual);
    }

    @Test
    public void mergeTwoLevelInheritAndForce() throws Exception {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://inheritAndForce")
        );

        Statements actual = this.worker.merge(
                configurations,
                this.description,
                "http://base",
                this.valueFactory.createIRI(
                        "http://expected/parent_inheritAndForce")
        );
        Statements expected = this.data.selectByGraph(
                "http://expected/parent_inheritAndForce");
        Assert.assertTrue(Models.isomorphic(expected, actual));
    }

    @Test
    public void mergeTwoLevelInheritAndForceFromBottom() {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://inheritAndForce")
        );

        Statements actual = merge(
                configurations,
                "http://expected/parent_inheritAndForce_bottom");

        Statements expected = this.data.selectByGraph(
                "http://expected/parent_inheritAndForce_bottom");
        Assert.assertTrue(Models.isomorphic(expected, actual));
    }

    @Test
    public void mergeTwoLevelNoControl() throws Exception {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://noControl")
        );

        Statements actual = this.worker.merge(
                configurations,
                this.description,
                "http://base",
                this.valueFactory.createIRI(
                        "http://expected/parent_noControl")
        );
        Statements expected = this.data.selectByGraph(
                "http://expected/parent_noControl");
        Assert.assertTrue(Models.isomorphic(expected, actual));
    }

    @Test
    public void mergeTwoLevelNoControlFromBottom() {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://noControl")
        );

        Statements actual = merge(
                configurations, "http://expected/parent_noControl_bottom");

        Statements expected = this.data.selectByGraph(
                "http://expected/parent_noControl_bottom");
        Assert.assertTrue(Models.isomorphic(expected, actual));
    }

    @Test
    public void mergeTwoLevelNone() throws Exception {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://none")
        );

        Statements actual = this.worker.merge(
                configurations,
                this.description,
                "http://base",
                this.valueFactory.createIRI(
                        "http://expected/parent_none")
        );
        Statements expected = this.data.selectByGraph(
                "http://expected/parent_none");
        Assert.assertTrue(Models.isomorphic(expected, actual));
    }

    @Test
    public void mergeFromBottomNoneFromBottom() {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://none")
        );

        Statements actual = merge(configurations,
                "http://expected/parent_none_bottom");

        Statements expected = this.data.selectByGraph(
                "http://expected/parent_none_bottom");
        Assert.assertTrue(Models.isomorphic(expected, actual));
    }
}
