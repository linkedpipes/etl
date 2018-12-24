package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
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

public class MergeHierarchyTest {

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private Statements data = Statements.arrayList();

    private Description description;

    @Before
    public void initialize() throws Exception {
        data.addAll(TestUtils.file("configuration/mergeHierarchy.trig"));
        description = Description.fromStatements(
                data.selectByGraph("http://description"));
    }

    @Test
    public void merge() throws Exception {
        List<Statements> configurations = Arrays.asList(
                data.selectByGraph("http://level-0"),
                data.selectByGraph("http://level-1"),
                data.selectByGraph("http://level-2")
        );

        MergeHierarchy worker = new MergeHierarchy();
        Statements actual = worker.merge(
                configurations,
                description,
                "http://base",
                valueFactory.createIRI("http://expected")
        );

        Statements expected = data.selectByGraph("http://expected");
        Rdf4jUtils.rdfEqual(expected, actual);
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

    @Test
    public void mergeFromBottom() {
        MergeFromBottom worker = new MergeFromBottom();

        Statements firstMerge = worker.merge(
                data.selectByGraph("http://level-1"),
                data.selectByGraph("http://level-2"),
                description,
                "http://base",
                valueFactory.createIRI("http://expected")
        );

        Statements secondMerge = worker.merge(
                data.selectByGraph("http://level-0"),
                firstMerge,
                description,
                "http://base",
                valueFactory.createIRI("http://expected")
        );

        Statements actual = worker.finalize(secondMerge);

        Statements expected = data.selectByGraph("http://expected");
        Rdf4jUtils.rdfEqual(actual, expected);
        Assert.assertTrue(Models.isomorphic(actual, expected));
    }

}
