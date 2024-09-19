package com.linkedpipes.etl.executor.component.configuration;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.executor.rdf.entity.EntityReference;
import com.linkedpipes.etl.rdf.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.RdfBuilder;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfTriple;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubstituteEnvironmentTest {

    @Test
    public void simpleSubstitution() throws ExecutorException {
        Map<String, String> env = new HashMap<>();
        env.put("LP_ETL_HOST", "lp");
        env.put("LP_ETL_PORT", "8080");
        //
        Assertions.assertEquals("lp:8080", SubstituteEnvironment.substitute(
                env, "{LP_ETL_HOST}:{LP_ETL_PORT}"));
        Assertions.assertEquals("x-lp:8080", SubstituteEnvironment.substitute(
                env, "x-{LP_ETL_HOST}:{LP_ETL_PORT}"));
    }

    @Test
    public void preserveStatements() throws ExecutorException, RdfUtilsException {
        Map<String, String> env = new HashMap<>();
        env.put("LP_ETL_HOST", "lp");
        env.put("LP_ETL_PORT", "8080");

        final var configurationClass = "http://localhost/Configuration";
        final var property = "http://localhost/1";
        final var graph = "http://localhost/graph";

        var source = Rdf4jSource.createInMemory();
        var builder = RdfBuilder.create(source, "http://localhost/graph");

        // Configuration entity.
        builder.entity("http://localhost/entity")
                .iri(RDF.TYPE, LP_OBJECTS.DESCRIPTION)
                .iri(LP_OBJECTS.HAS_DESCRIBE, configurationClass)
                .iri(LP_OBJECTS.HAS_MEMBER, property);
        builder.entity(property)
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://localhost/predicate")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://localhost/predicateControl");

        // Data entity.
        builder.entity("http://localhost/entity")
                .iri(RDF.TYPE, configurationClass)
                .iri("http://localhost/predicate", "http://localhost/1")
                .iri("http://localhost/predicate", "http://localhost/2");

        builder.commit();

        var actual = SubstituteEnvironment.substitute(
                env,
                source,
                new EntityReference("http://localhost/entity",graph,null),
                configurationClass);

        List<Statement> sourceList = new ArrayList<>();
        source.statements(null, graph, sourceList::add);

        List<RdfTriple> targetList = new ArrayList<>();
        actual.getSource().triples(null, targetList::add);

        // We should get the same number of triples.
        Assertions.assertEquals(sourceList.size(), targetList.size());
    }

}
