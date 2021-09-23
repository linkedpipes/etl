package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ReflectionLoaderTest {

    public static class TestClass {

        public String iri;

        public String value;

    }

    @Test
    public void loadEntity() throws Exception {
        Descriptor  descriptor = Mockito.mock(Descriptor.class);
        Mockito.when(descriptor.getFieldForPredicate("http://value"))
                .thenReturn(TestClass.class.getDeclaredField("value"));
        Mockito.when(descriptor.getFieldForResource())
                .thenReturn(TestClass.class.getDeclaredField("iri"));

        DescriptorFactory descriptorFactory =
                Mockito.mock(DescriptorFactory.class);
        Mockito.when(descriptorFactory.create(Mockito.any()))
                .thenReturn(descriptor);

        TestClass instance = new TestClass();

        ReflectionLoader loader = new ReflectionLoader(
                descriptorFactory, instance);
        loader.initialize();
        loader.resource("http://localhost");

        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asString()).thenReturn("value");
        loader.load("http://value", value);

        Assertions.assertEquals("http://localhost", instance.iri);
        Assertions.assertEquals("value", instance.value);
    }

}
