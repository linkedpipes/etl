package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import org.junit.Assert;
import org.junit.Test;
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

        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asString()).thenReturn("value");
        loader.load("http://value", value);

        Assert.assertEquals("http://localhost", instance.iri);
        Assert.assertEquals("value", instance.value);
    }

}
