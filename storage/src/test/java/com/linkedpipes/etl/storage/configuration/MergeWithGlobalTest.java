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

public class MergeWithGlobalTest {

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private MergeHierarchy worker = new MergeHierarchy();

    private Statements data = Statements.arrayList();

    private Description description;

    @Before
    public void initialize() throws Exception {
        data.addAll(TestUtils.file("configuration/mergeWithGlobal.trig"));
        this.description = Description.fromStatements(
                data.selectByGraph("http://description"));
    }

    @Test
    public void mergeGlobalControlIafF() throws Exception {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://inheritAndForce"),
                this.data.selectByGraph("http://force")
        );

        Statements actual = this.worker.merge(
                configurations,
                this.description,
                "http://base",
                this.valueFactory.createIRI(
                        "http://expected/parent_inheritAndForce_force")
        );

        Statements expected = this.data.selectByGraph(
                "http://expected/parent_inheritAndForce_force");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeGlobalControlIafFFromBottom() {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://inheritAndForce"),
                this.data.selectByGraph("http://force")
        );

        Statements actual = merge(
                configurations,
                "http://expected/parent_inheritAndForce_force");

        Statements expected = this.data.selectByGraph(
                "http://expected/parent_inheritAndForce_force");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    private Statements merge(List<Statements> configurations, String graph) {
        MergeFromBottom worker = new MergeFromBottom();
        Statements actual = configurations.get(configurations.size() - 1);
        for (int index = configurations.size() - 2; index >= 0; --index) {
            actual = worker.merge(
                    configurations.get(index),
                    actual,
                    this.description,
                    "http://base",
                    this.valueFactory.createIRI(graph)
            );
        }
        return worker.finalize(actual);
    }

    @Test
    public void mergeGlobalControlIIafF() throws Exception {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://inherit"),
                this.data.selectByGraph("http://inheritAndForce"),
                this.data.selectByGraph("http://force")
        );

        Statements actual = this.worker.merge(
                configurations,
                this.description,
                "http://base",
                this.valueFactory.createIRI(
                        "http://expected/parent_inherit_inheritAndForce_force")
        );

        Statements expected = this.data.selectByGraph(
                "http://expected/parent_inherit_inheritAndForce_force");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeGlobalControlIIafFFromBottom() {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://inherit"),
                this.data.selectByGraph("http://inheritAndForce"),
                this.data.selectByGraph("http://force")
        );

        Statements actual = merge(
                configurations,
                "http://expected/parent_inherit_inheritAndForce_force");

        Statements expected = this.data.selectByGraph(
                "http://expected/parent_inherit_inheritAndForce_force");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeGlobalControlFIaF() throws Exception {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://force"),
                this.data.selectByGraph("http://inheritAndForce")
        );

        Statements actual = this.worker.merge(
                configurations,
                this.description,
                "http://base",
                this.valueFactory.createIRI(
                        "http://expected/parent_force_inheritAndForce")
        );

        Statements expected = this.data.selectByGraph(
                "http://expected/parent_force_inheritAndForce");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeGlobalControlFIaFFromBottom() {
        List<Statements> configurations = Arrays.asList(
                this.data.selectByGraph("http://parent"),
                this.data.selectByGraph("http://force"),
                this.data.selectByGraph("http://inheritAndForce")
        );

        Statements actual = merge(
                configurations,
                "http://expected/parent_force_inheritAndForce");

        Statements expected = this.data.selectByGraph(
                "http://expected/parent_force_inheritAndForce");
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

}
