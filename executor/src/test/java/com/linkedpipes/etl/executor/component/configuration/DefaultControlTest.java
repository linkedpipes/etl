package com.linkedpipes.etl.executor.component.configuration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.rdf.utils.RdfBuilder;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.entity.EntityMergeType;
import com.linkedpipes.etl.rdf.utils.entity.EntityReference;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class DefaultControlTest {

    @Test
    public void initFromTwoSources() throws RdfUtilsException {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfSource otherSource = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        builder.entity("http://des").iri(RDF.TYPE, LP_OBJECTS.DESCRIPTION)
                .iri(LP_OBJECTS.HAS_DESCRIBE, "http://type")
                .entity(LP_OBJECTS.HAS_MEMBER, "http://des/1")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/1")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/1")
                .close()
                .entity(LP_OBJECTS.HAS_MEMBER, "http://des/2")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/2")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/2")
                .close();
        builder.commit();
        final RdfBuilder cnf1 = RdfBuilder.create(source, "http://config/1");
        cnf1.entity("http://config")
                .iri(RDF.TYPE, "http://type")
                .string("http://value/1", "1_1")
                .string("http://control/1", LP_OBJECTS.NONE)
                .string("http://value/2", "1_2")
                .string("http://control/2", LP_OBJECTS.FORCE);
        cnf1.commit();
        final RdfBuilder cnf2 = RdfBuilder.create(otherSource, "http://config/2");
        cnf2.entity("http://config")
                .iri(RDF.TYPE, "http://type")
                .string("http://value/1", "2_1")
                .string("http://control/1", LP_OBJECTS.NONE)
                .string("http://value/2", "2_1")
                .string("http://control/2", LP_OBJECTS.FORCE);
        cnf2.commit();
        final DefaultControl control = new DefaultControl();
        control.loadDefinition(source, "http://graph", "http://type");

        final List<EntityMerger.Reference> refs = new LinkedList<>();
        refs.add(new EntityMerger.Reference("http://config",
                "http://config/1", source));
        refs.add(new EntityMerger.Reference("http://config",
                "http://config/2", otherSource));
        control.init(refs);

        control.onReference("http://config", "http://config/1");
        Assert.assertEquals(EntityMerger.MergeType.SKIP,
                control.onProperty("http://value/1"));
        Assert.assertEquals(EntityMerger.MergeType.LOAD,
                control.onProperty("http://value/2"));

        control.onReference("http://config", "http://config/2");
        Assert.assertEquals(EntityMerger.MergeType.LOAD,
                control.onProperty("http://value/1"));
        Assert.assertEquals(EntityMerger.MergeType.SKIP,
                control.onProperty("http://value/2"));

        source.shutdown();
        otherSource.shutdown();
    }

}
