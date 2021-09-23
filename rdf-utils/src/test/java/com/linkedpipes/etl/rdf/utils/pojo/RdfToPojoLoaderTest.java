package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.model.SimpleStore;
import com.linkedpipes.etl.rdf.utils.model.SimpleTriple;
import com.linkedpipes.etl.rdf.utils.model.SimpleValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

public class RdfToPojoLoaderTest {

    public static class TestObject implements Loadable {

        public String resource;

        public String title;

        public TestObject object;

        @Override
        public void resource(String resource) {
            this.resource = resource;
        }

        @Override
        public Loadable load(String predicate, BackendRdfValue value) {
            switch (predicate) {
                case "title":
                    title = value.asString();
                    break;
                case "object":
                    object = new TestObject();
                    return object;
                default:
                    break;
            }
            return null;
        }
    }

    @Test
    public void loadResource() throws RdfUtilsException {
        SimpleStore store = new SimpleStore(Arrays.asList(
                new SimpleTriple("resource", "title",
                        new SimpleValue("TestObjectTitle", false)),
                new SimpleTriple("resource", "object",
                        new SimpleValue("ref", true)),
                new SimpleTriple("ref", "title",
                        new SimpleValue("InnerObject", false))
        ));

        RdfToPojoLoader loader = new RdfToPojoLoader(store);
        TestObject testObject = new TestObject();
        loader.loadResource("resource", "", testObject);

        Assertions.assertEquals("resource", testObject.resource);
        Assertions.assertEquals("TestObjectTitle", testObject.title);
        Assertions.assertNotNull(testObject.object);
        Assertions.assertEquals("ref", testObject.object.resource);
        Assertions.assertEquals("InnerObject", testObject.object.title);
    }

    @Test
    public void loadByReflection()
            throws RdfUtilsException, NoSuchFieldException {
        SimpleStore store = new SimpleStore(Arrays.asList(
                new SimpleTriple("resource", "title",
                        new SimpleValue("TestObjectTitle", false)),
                new SimpleTriple("resource", "object",
                        new SimpleValue("ref", true)),
                new SimpleTriple("ref", "title",
                        new SimpleValue("InnerObject", false))
        ));

        Descriptor descriptor = Mockito.mock(Descriptor.class);
        Mockito.when(descriptor.getFieldForResource()).thenReturn(
                TestObject.class.getDeclaredField("resource")
        );
        Mockito.when(descriptor.getFieldForPredicate("title")).thenReturn(
                TestObject.class.getDeclaredField("title")
        );
        Mockito.when(descriptor.getFieldForPredicate("object")).thenReturn(
                TestObject.class.getDeclaredField("object")
        );

        DescriptorFactory descriptorFactory =
                Mockito.mock(DescriptorFactory.class);
        Mockito.when(descriptorFactory.create(Mockito.any()))
                .thenReturn(descriptor);

        RdfToPojoLoader loader = new RdfToPojoLoader(store);
        TestObject testObject = new TestObject();
        loader.loadResourceByReflection("resource", "", testObject,
                descriptorFactory);

        Assertions.assertEquals("resource", testObject.resource);
        Assertions.assertEquals("TestObjectTitle", testObject.title);
        Assertions.assertNotNull(testObject.object);
        Assertions.assertEquals("ref", testObject.object.resource);
        Assertions.assertEquals("InnerObject", testObject.object.title);
    }

}
