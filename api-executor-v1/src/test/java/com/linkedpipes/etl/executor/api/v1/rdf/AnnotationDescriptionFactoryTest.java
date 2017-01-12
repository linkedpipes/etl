package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;
import org.junit.Assert;
import org.junit.Test;

public class AnnotationDescriptionFactoryTest {

    @RdfToPojo.Type(iri = "http://type")
    public static class TestObject {

        @RdfToPojo.Property(iri = "http://label")
        private String label;

    }

    @RdfToPojo.Type(iri = "http://inheritance")
    public static class TestInheritance extends TestObject {

        @RdfToPojo.Property(iri = "http://name")
        private String name;

    }

    @Test
    public void simpleScan() {
        final RdfLoader.DescriptorFactory factory =
                new AnnotationDescriptionFactory();
        final RdfLoader.Descriptor desc =
                factory.create(TestObject.class);
        //
        Assert.assertEquals("http://type", desc.getType());
        Assert.assertNotNull(desc.getField("http://label"));
    }

    @Test
    public void inheritanceScan() {
        final RdfLoader.DescriptorFactory factory =
                new AnnotationDescriptionFactory();
        final RdfLoader.Descriptor desc =
                factory.create(TestInheritance.class);
        //
        Assert.assertEquals("http://inheritance", desc.getType());
        Assert.assertNotNull(desc.getField("http://name"));
        Assert.assertNotNull(desc.getField("http://label"));
    }

}
