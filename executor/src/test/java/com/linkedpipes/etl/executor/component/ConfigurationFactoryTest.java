package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import com.linkedpipes.etl.rdf.utils.*;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationFactoryTest {

    /**
     * Test object used to load all properties.
     */
    private static class TestObject implements RdfLoader.Loadable<String> {

        private Map<String, List<String>> values = new HashMap();

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            if (!values.containsKey(predicate)) {
                values.put(predicate, new ArrayList<>());
            }
            values.get(predicate).add(object);
            return null;
        }
    }

    private static class ReferenceTestObject
            implements RdfLoader.Loadable<String> {

        private TestObject reference;

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            if (predicate.equals("http://value/reference")) {
                reference = new TestObject();
                return reference;
            }
            return null;
        }
    }

    @Test
    public void mergeThreeConfigurations() throws Exception {
        final RdfSource source = Rdf4jSource.createInMemory();
        // Pipeline
        final RdfBuilder ppl = new RdfBuilder(
                source.getTripleWriter("http://pipeline"));
        ppl.entity("http://ppl")
                .iri(RDF.TYPE, LP_PIPELINE.PIPELINE)
                .entity(LP_PIPELINE.HAS_COMPONENT, "http://cmp")
                .iri(RDF.TYPE, LP_PIPELINE.COMPONENT)
                .integer(LP_EXEC.HAS_ORDER, 0)
                .entity(LP_EXEC.HAS_CONFIGURATION, "http://c/1")
                .integer(LP_EXEC.HAS_ORDER, 1)
                .iri(LP_PIPELINE.HAS_CONFIGURATION_GRAPH, "http://config/1")
                .close()
                .entity(LP_EXEC.HAS_CONFIGURATION, "http://c/2")
                .integer(LP_EXEC.HAS_ORDER, 2)
                .iri(LP_PIPELINE.HAS_CONFIGURATION_GRAPH, "http://config/2")
                .close()
                .entity(LP_EXEC.HAS_CONFIGURATION, "http://c/3")
                .integer(LP_EXEC.HAS_ORDER, 3)
                .iri(LP_PIPELINE.HAS_CONFIGURATION_GRAPH, "http://config/3")
                .close();
        // Configuration description.
        ppl.entity("http://cmp").entity(
                LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION, "http://des")
                .iri(RDF.TYPE, LP_OBJECTS.DESCRIPTION)
                .iri(LP_OBJECTS.HAS_DESCRIBE, "http://config/type")
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/1")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/1")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/1")
                .close()
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/2")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/2")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/2")
                .close()
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/3")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/3")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/3")
                .close();
        ppl.commit();

        final RdfBuilder c_1 = new RdfBuilder(
                source.getTripleWriter("http://config/1"));
        c_1.entity("http://resource")
                .iri(RDF.TYPE, "http://config/type")
                .string("http://value/1", "1_1")
                .string("http://control/1", LP_OBJECTS.NONE)
                .string("http://value/2", "1_2")
                .string("http://control/2", LP_OBJECTS.NONE)
                .string("http://value/3", "1_3")
                .string("http://control/3", LP_OBJECTS.NONE);
        c_1.commit();
        final RdfBuilder c_2 = new RdfBuilder(
                source.getTripleWriter("http://config/2"));
        c_2.entity("http://resource")
                .iri(RDF.TYPE, "http://config/type")
                .string("http://value/1", "2_1")
                .string("http://control/1", LP_OBJECTS.INHERIT)
                .string("http://value/2", "2_2")
                .string("http://control/2", LP_OBJECTS.NONE)
                .string("http://value/3", "2_3_0")
                .string("http://value/3", "2_3_1")
                .string("http://control/3", LP_OBJECTS.FORCE);
        c_2.commit();
        final RdfBuilder c_3 = new RdfBuilder(
                source.getTripleWriter("http://config/3"));
        c_3.entity("http://resource/3")
                .iri(RDF.TYPE, "http://config/type")
                .string("http://value/1", "3_1")
                .string("http://control/1", LP_OBJECTS.INHERIT)
                .string("http://value/2", "3_2")
                .string("http://control/2", LP_OBJECTS.NONE)
                .string("http://value/3", "3_3")
                .string("http://control/3", LP_OBJECTS.FORCE);
        c_3.commit();
        //
        final PipelineModel model = new PipelineModel(
                "http://ppl", "http://pipeline");
        RdfUtils.load(source, model, "http://ppl", "http://pipeline",
                String.class);
        //
        final Pipeline pipeline = Mockito.mock(Pipeline.class);
        Mockito.when(pipeline.getModel()).thenReturn(model);
        Mockito.when(pipeline.getSource()).thenReturn(source);
        final RdfSource target = Rdf4jSource.createInMemory();
        // Merge configurations.
        final Configuration factory = new Configuration();
        factory.prepareConfiguration("http://cmp/config/resource",
                model.getComponent("http://cmp"), null, null,
                target.getTypedTripleWriter("http://cmp/config/final"),
                pipeline);
        // Load all to the test object.
        final TestObject testObject = new TestObject();
        RdfUtils.load(target, testObject, "http://cmp/config/resource",
                "http://cmp/config/final", String.class);
        // Check values.
        Assert.assertEquals("1_1",
                testObject.values.get("http://value/1").get(0));
        Assert.assertEquals("3_2",
                testObject.values.get("http://value/2").get(0));
        Assert.assertEquals(2, testObject.values.get("http://value/3").size());
        Assert.assertTrue(
                testObject.values.get("http://value/3").contains("2_3_0"));
        Assert.assertTrue(
                testObject.values.get("http://value/3").contains("2_3_1"));
        //
        source.shutdown();
        target.shutdown();
    }

    @Test
    public void copyOneOfTwoReferencedObject() throws Exception {
        final RdfSource source = Rdf4jSource.createInMemory();
        // Pipeline
        final RdfBuilder ppl = new RdfBuilder(
                source.getTripleWriter("http://pipeline"));
        ppl.entity("http://ppl")
                .iri(RDF.TYPE, LP_PIPELINE.PIPELINE)
                .entity(LP_PIPELINE.HAS_COMPONENT, "http://cmp")
                .iri(RDF.TYPE, LP_PIPELINE.COMPONENT)
                .integer(LP_EXEC.HAS_ORDER, 0)
                .entity(LP_EXEC.HAS_CONFIGURATION, "http://c/1")
                .integer(LP_EXEC.HAS_ORDER, 1)
                .iri(LP_PIPELINE.HAS_CONFIGURATION_GRAPH, "http://config/1")
                .close()
                .entity(LP_EXEC.HAS_CONFIGURATION, "http://c/2")
                .integer(LP_EXEC.HAS_ORDER, 2)
                .iri(LP_PIPELINE.HAS_CONFIGURATION_GRAPH, "http://config/2")
                .close();
        // Configuration description for top level object.
        ppl.entity("http://cmp").entity(
                LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION,
                "http://desRef")
                .iri(RDF.TYPE, LP_OBJECTS.DESCRIPTION)
                .iri(LP_OBJECTS.HAS_DESCRIBE, "http://config/typeReference")
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/r/1")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/reference")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/reference")
                .close();
        ppl.commit();
        // Configuration description for  a reference.
        ppl.entity("http://des")
                .iri(RDF.TYPE, LP_OBJECTS.DESCRIPTION)
                .iri(LP_OBJECTS.HAS_DESCRIBE, "http://config/type")
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/1")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/1")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/1")
                .close()
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/2")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/2")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/2")
                .close()
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/3")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/3")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/3")
                .close();
        ppl.commit();
        //
        final RdfBuilder c_1 = new RdfBuilder(
                source.getTripleWriter("http://config/1"));
        c_1.entity("http://resource")
                .iri(RDF.TYPE, "http://config/typeReference")
                .iri("http://control/reference", LP_OBJECTS.NONE)
                .entity("http://value/reference", "http://resource/ref")
                .iri(RDF.TYPE, "http://config/type")
                .string("http://value/1", "1_1")
                .string("http://control/1", LP_OBJECTS.NONE)
                .string("http://value/2", "1_2")
                .string("http://control/2", LP_OBJECTS.NONE)
                .string("http://value/3", "1_3")
                .string("http://control/3", LP_OBJECTS.NONE);
        c_1.commit();
        final RdfBuilder c_2 = new RdfBuilder(
                source.getTripleWriter("http://config/2"));
        c_2.entity("http://resource")
                .iri(RDF.TYPE, "http://config/typeReference")
                .iri("http://control/reference", LP_OBJECTS.INHERIT)
                .entity("http://value/reference", "http://resource/ref")
                .iri(RDF.TYPE, "http://config/type")
                .string("http://value/1", "2_1")
                .string("http://control/1", LP_OBJECTS.INHERIT)
                .string("http://value/2", "2_2")
                .string("http://control/2", LP_OBJECTS.NONE)
                .string("http://value/3", "2_3_0")
                .string("http://value/3", "2_3_1")
                .string("http://control/3", LP_OBJECTS.FORCE);
        c_2.commit();
        //
        final PipelineModel model = new PipelineModel(
                "http://ppl", "http://pipeline");
        RdfUtils.load(source, model, "http://ppl", "http://pipeline",
                String.class);
        //
        final Pipeline pipeline = Mockito.mock(Pipeline.class);
        Mockito.when(pipeline.getModel()).thenReturn(model);
        Mockito.when(pipeline.getSource()).thenReturn(source);
        final RdfSource target = Rdf4jSource.createInMemory();
        // Merge configurations.
        final Configuration factory = new Configuration();
        factory.prepareConfiguration("http://cmp/config/resource",
                model.getComponent("http://cmp"), null, null,
                target.getTypedTripleWriter("http://cmp/config/final"),
                pipeline);
        // Load all to the test object.
        final ReferenceTestObject testObject = new ReferenceTestObject();
        RdfUtils.load(target, testObject, "http://cmp/config/resource",
                "http://cmp/config/final", String.class);
        // Check values.
        Assert.assertNotNull(testObject.reference);
        Assert.assertEquals("1_1",
                testObject.reference.values.get("http://value/1").get(0));
        Assert.assertEquals("1_2",
                testObject.reference.values.get("http://value/2").get(0));
        Assert.assertEquals("1_3",
                testObject.reference.values.get("http://value/3").get(0));

        //
        source.shutdown();
        target.shutdown();
    }

    @Test
    public void mergeTwoReferencedObject() throws Exception {
        final RdfSource source = Rdf4jSource.createInMemory();
        // Pipeline
        final RdfBuilder ppl = new RdfBuilder(
                source.getTripleWriter("http://pipeline"));
        ppl.entity("http://ppl")
                .iri(RDF.TYPE, LP_PIPELINE.PIPELINE)
                .entity(LP_PIPELINE.HAS_COMPONENT, "http://cmp")
                .iri(RDF.TYPE, LP_PIPELINE.COMPONENT)
                .integer(LP_EXEC.HAS_ORDER, 0)
                .entity(LP_EXEC.HAS_CONFIGURATION, "http://c/1")
                .integer(LP_EXEC.HAS_ORDER, 1)
                .iri(LP_PIPELINE.HAS_CONFIGURATION_GRAPH, "http://config/1")
                .close()
                .entity(LP_EXEC.HAS_CONFIGURATION, "http://c/2")
                .integer(LP_EXEC.HAS_ORDER, 2)
                .iri(LP_PIPELINE.HAS_CONFIGURATION_GRAPH, "http://config/2")
                .close();
        // Configuration description for top level object.
        ppl.entity("http://cmp").entity(
                LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION,
                "http://desRef")
                .iri(RDF.TYPE, LP_OBJECTS.DESCRIPTION)
                .iri(LP_OBJECTS.HAS_DESCRIBE, "http://config/typeReference")
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/r/1")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/reference")
                .bool(LP_OBJECTS.IS_COMPLEX, true)
                .close();
        ppl.commit();
        // Configuration description for  a reference.
        ppl.entity("http://des")
                .iri(RDF.TYPE, LP_OBJECTS.DESCRIPTION)
                .iri(LP_OBJECTS.HAS_DESCRIBE, "http://config/type")
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/1")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/1")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/1")
                .close()
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/2")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/2")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/2")
                .close()
                .entity(LP_OBJECTS.HAS_MEMBER, "http://member/3")
                .iri(LP_OBJECTS.HAS_PROPERTY, "http://value/3")
                .iri(LP_OBJECTS.HAS_CONTROL, "http://control/3")
                .close();
        ppl.commit();
        //
        final RdfBuilder c_1 = new RdfBuilder(
                source.getTripleWriter("http://config/1"));
        c_1.entity("http://resource")
                .iri(RDF.TYPE, "http://config/typeReference")
                .entity("http://value/reference", "http://resource/ref")
                .iri(RDF.TYPE, "http://config/type")
                .string("http://value/1", "1_1")
                .string("http://control/1", LP_OBJECTS.NONE)
                .string("http://value/2", "1_2")
                .string("http://control/2", LP_OBJECTS.NONE)
                .string("http://value/3", "1_3")
                .string("http://control/3", LP_OBJECTS.FORCE);
        c_1.commit();
        final RdfBuilder c_2 = new RdfBuilder(
                source.getTripleWriter("http://config/2"));
        c_2.entity("http://resource")
                .iri(RDF.TYPE, "http://config/typeReference")
                .entity("http://value/reference", "http://resource/ref")
                .iri(RDF.TYPE, "http://config/type")
                .string("http://value/1", "2_1")
                .string("http://control/1", LP_OBJECTS.INHERIT)
                .string("http://value/2", "2_2")
                .string("http://control/2", LP_OBJECTS.NONE)
                .string("http://value/3", "2_3")
                .string("http://control/3", LP_OBJECTS.NONE);
        c_2.commit();
        //
        final PipelineModel model = new PipelineModel(
                "http://ppl", "http://pipeline");
        RdfUtils.load(source, model, "http://ppl", "http://pipeline",
                String.class);
        //
        final Pipeline pipeline = Mockito.mock(Pipeline.class);
        Mockito.when(pipeline.getModel()).thenReturn(model);
        Mockito.when(pipeline.getSource()).thenReturn(source);
        final RdfSource target = Rdf4jSource.createInMemory();
        // Merge configurations.
        final Configuration factory = new Configuration();
        factory.prepareConfiguration("http://cmp/config/resource",
                model.getComponent("http://cmp"), null, null,
                target.getTypedTripleWriter("http://cmp/config/final"),
                pipeline);
        // Load all to the test object.
        final ReferenceTestObject testObject = new ReferenceTestObject();
        RdfUtils.load(target, testObject, "http://cmp/config/resource",
                "http://cmp/config/final", String.class);
        // Check values.
        Assert.assertNotNull(testObject.reference);
        Assert.assertEquals("1_1",
                testObject.reference.values.get("http://value/1").get(0));
        Assert.assertEquals("2_2",
                testObject.reference.values.get("http://value/2").get(0));
        Assert.assertEquals("1_3",
                testObject.reference.values.get("http://value/3").get(0));
        //
        source.shutdown();
        target.shutdown();
    }

}
