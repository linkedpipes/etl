package com.linkedpipes.etl.rdf.utils.entity;

import com.linkedpipes.etl.rdf.utils.RdfBuilder;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.eclipse.rdf4j.model.Value;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

public class EntityMergerTest {

    /**
     * Helper class used to verify results of the merge.
     */
    private static class TestClass implements RdfLoader.Loadable<String> {

        private String value1;

        private String value2;

        private TestClass reference;

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            switch (predicate) {
                case "http://value/1":
                    value1 = object;
                    break;
                case "http://value/2":
                    value2 = object;
                    break;
                case "http://value/reference":
                    reference = new TestClass();
                    return reference;
            }
            return null;
        }
    }

    /**
     * Helper class for testing.
     */
    private static class StaticDescriptor implements            EntityControl {

        private Map<String, EntityMergeType> strategy = new HashMap<>();

        private String currentObject;

        @Override
        public void init(List<EntityReference> references)
                throws RdfUtilsException {
            // No operation here.
        }

        @Override
        public void onReference(String resource, String graph) {
            currentObject = resource;
        }

        @Override
        public EntityMergeType onProperty(String property) {
            return strategy.getOrDefault(currentObject + "|" + property,
                    EntityMergeType.SKIP);
        }
    }

    @Test
    public void mergeFlatObjectsWithReference() throws Exception {
        final RdfSource<Value> source = Rdf4jSource.createInMemory();
        //
        final RdfBuilder A = RdfBuilder.create(source, "http://graph/A");
        A.entity("http://res/A")
                .iri(RDF.TYPE, "http://config/type")
                .string("http://value/1", "a")
                .string("http://value/2", "a")
                .entity("http://value/reference", "http://res/A/1")
                .string("http://value/1", "a");
        A.commit();
        //
        final RdfBuilder B = RdfBuilder.create(source, "http://graph/B");
        B.entity("http://res/B")
                .string("http://value/1", "b")
                .string("http://value/2", "b");
        B.commit();
        //
        final List<EntityReference> references = new ArrayList<>(3);
        references.add(new EntityReference(
                "http://res/A", "http://graph/A", source));
        references.add(new EntityReference(
                "http://res/B", "http://graph/B", source));
        // Mock merger.
        final StaticDescriptor descriptor = new StaticDescriptor();
        descriptor.strategy.put("http://res/A|http://value/1",
                EntityMergeType.SKIP);
        descriptor.strategy.put("http://res/A|http://value/2",
                EntityMergeType.LOAD);
        descriptor.strategy.put("http://res/A|http://value/reference",
                EntityMergeType.LOAD);
        descriptor.strategy.put("http://res/B|http://value/1",
                EntityMergeType.LOAD);
        descriptor.strategy.put("http://res/B|http://value/2",
                EntityMergeType.SKIP);
        final EntityControlFactory descriptorFactory =
                Mockito.mock(EntityControlFactory.class);
        Mockito.when(descriptorFactory.create(Mockito.anyString()))
                .thenReturn(descriptor);
        //
        final RdfSource target = Rdf4jSource.createInMemory();
        EntityMerger.merge(references, descriptorFactory,
                "http://res/target",
                target.getTypedTripleWriter("http://graph/target"),
                Value.class);
        // Test output by loading into an object.
        final TestClass testObject = new TestClass();
        RdfLoader.load(target, testObject, "http://res/target",
                "http://graph/target", String.class);
        Assert.assertEquals("b", testObject.value1);
        Assert.assertEquals("a", testObject.value2);
        Assert.assertNotNull(testObject.reference);
        Assert.assertEquals("a", testObject.reference.value1);
        //
        target.shutdown();
        source.shutdown();
    }

    @Test
    public void mergeRecursive() throws Exception {
        final RdfSource<Value> source = Rdf4jSource.createInMemory();
        //
        final RdfBuilder A = RdfBuilder.create(source, "http://graph/A");
        A.entity("http://res/A")
                .iri(RDF.TYPE, "http://config/type")
                .entity("http://value/reference", "http://res/A/1")
                .iri(RDF.TYPE, "http://config/type")
                .string("http://value/1", "a")
                .string("http://value/2", "a");
        A.commit();
        //
        final RdfBuilder B = RdfBuilder.create(source, "http://graph/B");
        B.entity("http://res/B")
                .entity("http://value/reference", "http://res/B/1")
                .string("http://value/1", "b")
                .string("http://value/2", "b");
        B.commit();
        //
        final List<EntityReference> references = new ArrayList<>(3);
        references.add(new EntityReference(
                "http://res/A", "http://graph/A", source));
        references.add(new EntityReference(
                "http://res/B", "http://graph/B", source));
        // Mock merger.
        final StaticDescriptor descriptor = new StaticDescriptor();
        descriptor.strategy.put("http://res/A|http://value/reference",
                EntityMergeType.MERGE);
        descriptor.strategy.put("http://res/A/1|http://value/1",
                EntityMergeType.LOAD);
        descriptor.strategy.put("http://res/A/1|http://value/2",
                EntityMergeType.SKIP);
        descriptor.strategy.put("http://res/B|http://value/reference",
                EntityMergeType.MERGE);
        descriptor.strategy.put("http://res/B/1|http://value/1",
                EntityMergeType.SKIP);
        descriptor.strategy.put("http://res/B/1|http://value/2",
                EntityMergeType.LOAD);
        //
        final EntityControlFactory descriptorFactory =
                Mockito.mock(EntityControlFactory.class);
        Mockito.when(descriptorFactory.create(Mockito.anyString()))
                .thenReturn(descriptor);
        //
        final RdfSource target = Rdf4jSource.createInMemory();
        EntityMerger.merge(references, descriptorFactory,
                "http://res/target",
                target.getTypedTripleWriter("http://graph/target"),
                Value.class);
        // Test output by loading into an object.
        final TestClass testObject = new TestClass();
        RdfLoader.load(target, testObject, "http://res/target",
                "http://graph/target", String.class);
        Assert.assertNotNull(testObject.reference);
        Assert.assertEquals("a", testObject.reference.value1);
        Assert.assertEquals("b", testObject.reference.value2);
        //
        target.shutdown();
        source.shutdown();
    }

    @Test
    public void emptyReferenceList() throws Exception {
        try {
            EntityMerger.merge(Collections.EMPTY_LIST,
                    null, null, null, Value.class);
            Assert.fail();
        } catch (RdfUtilsException ex) {
            // OK
        } catch (Exception ex) {
            Assert.fail();
        }
    }

    @Test
    public void missingDescription() throws Exception {
        final RdfSource<Value> source = Rdf4jSource.createInMemory();
        //
        final RdfBuilder A = RdfBuilder.create(source, "http://graph/A");
        A.entity("http://res/A")
                .iri(RDF.TYPE, "http://config/type")
                .string("http://value/1", "a");
        A.commit();
        //
        final RdfBuilder B = RdfBuilder.create(source, "http://graph/B");
        B.entity("http://res/B")
                .string("http://value/1", "b");
        B.commit();
        //
        final List<EntityReference> references = new ArrayList<>(3);
        references.add(new EntityReference(
                "http://res/A", "http://graph/A", source));
        references.add(new EntityReference(
                "http://res/B", "http://graph/B", source));
        final EntityControlFactory descriptorFactory =
                Mockito.mock(EntityControlFactory.class);
        //
        final RdfSource target = Rdf4jSource.createInMemory();
        try {
            EntityMerger.merge(references, descriptorFactory,
                    "http://res/target",
                    target.getTypedTripleWriter("http://graph/target"),
                    Value.class);
            Assert.fail();
        } catch (RdfUtilsException ex) {
            // OK
        }
        //
        target.shutdown();
        source.shutdown();
    }

}
