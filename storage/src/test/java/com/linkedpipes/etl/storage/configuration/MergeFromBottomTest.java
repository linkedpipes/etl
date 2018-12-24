package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class MergeFromBottomTest {

    @Test
    public void finalizeAfterMerge() {
        ValueFactory vf = SimpleValueFactory.getInstance();
        IRI iri = vf.createIRI("http://localhost/instance");

        Statements statements = Statements.arrayList();
        statements.setDefaultGraph("http://localhost/graph");
        statements.addIri(iri, "http://localhost/first", LP_OBJECTS.INHERIT);
        statements.addIri(iri, "http://localhost/second",
                LP_OBJECTS.INHERIT_AND_FORCE);
        statements.addIri(iri, "http://localhost/third", LP_OBJECTS.NONE);
        statements.addInt(iri, "http://localhost/value", 10);

        MergeFromBottom worker = new MergeFromBottom();
        Collection<Statement> actual = worker.finalize(statements);

        Statements expected = Statements.arrayList();
        expected.setDefaultGraph("http://localhost/graph");
        expected.addIri(iri, "http://localhost/second",
                LP_OBJECTS.INHERIT_AND_FORCE);
        expected.addIri(iri, "http://localhost/third", LP_OBJECTS.NONE);
        expected.addInt(iri, "http://localhost/value", 10);

        Rdf4jUtils.rdfEqual(expected, actual);
        Assert.assertTrue(Models.isomorphic(expected, actual));
    }

}
