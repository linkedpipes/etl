package com.linkedpipes.etl.dataunit.core;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.RdfBuilder;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void loadWithSources() throws Exception {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        builder.entity("http://component/a")
                .iri(RDF.TYPE, LP_PIPELINE.COMPONENT)
                .entity(LP_PIPELINE.HAS_DATA_UNIT, "http://dataunit/a")
                .iri(RDF.TYPE, "http://type")
                .string(LP_PIPELINE.HAS_BINDING, "binding/a");
        builder.entity("http://component/b")
                .iri(RDF.TYPE, LP_PIPELINE.COMPONENT)
                .entity(LP_PIPELINE.HAS_DATA_UNIT, "http://dataunit/b")
                .iri(RDF.TYPE, LP_PIPELINE.OUTPUT)
                .string(LP_PIPELINE.HAS_BINDING, "binding/b");
        builder.entity("http://component/c")
                .iri(RDF.TYPE, LP_PIPELINE.COMPONENT)
                .entity(LP_PIPELINE.HAS_DATA_UNIT, "http://dataunit/c")
                .iri(RDF.TYPE, LP_PIPELINE.OUTPUT)
                .string(LP_PIPELINE.HAS_BINDING, "binding/c");
        builder.entity("http://component/d")
                .iri(RDF.TYPE, LP_PIPELINE.COMPONENT)
                .entity(LP_PIPELINE.HAS_DATA_UNIT, "http://dataunit/d")
                .iri(RDF.TYPE, LP_PIPELINE.OUTPUT)
                .string(LP_PIPELINE.HAS_BINDING, "binding/d");
        //
        builder.entity("http://connection/a")
                .iri(RDF.TYPE, LP_PIPELINE.CONNECTION)
                .iri(LP_PIPELINE.HAS_SOURCE_COMPONENT, "http://component/b")
                .string(LP_PIPELINE.HAS_SOURCE_BINDING, "binding/b")
                .iri(LP_PIPELINE.HAS_TARGET_COMPONENT, "http://component/a")
                .string(LP_PIPELINE.HAS_TARGET_BINDING, "binding/a");
        builder.entity("http://connection/b")
                .iri(RDF.TYPE, LP_PIPELINE.CONNECTION)
                .iri(LP_PIPELINE.HAS_SOURCE_COMPONENT, "http://component/c")
                .string(LP_PIPELINE.HAS_SOURCE_BINDING, "binding/c")
                .iri(LP_PIPELINE.HAS_TARGET_COMPONENT, "http://component/a")
                .string(LP_PIPELINE.HAS_TARGET_BINDING, "binding/a");
        builder.commit();
        //
        final BaseConfiguration config =
                new BaseConfiguration("http://dataunit/a", "http://graph");
        RdfLoader.load(source, config,
                "http://dataunit/a", "http://graph", String.class);
        config.loadSources(source);
        //
        Assert.assertEquals("binding/a", config.getBinding());
        Assert.assertEquals(1, config.getTypes().size());
        Assert.assertEquals("http://type", config.getTypes().get(0));
        Assert.assertEquals(2, config.getSources().size());
        Assert.assertTrue(config.getSources().contains("http://dataunit/b"));
        Assert.assertTrue(config.getSources().contains("http://dataunit/c"));
        //
        source.shutdown();
    }

}
