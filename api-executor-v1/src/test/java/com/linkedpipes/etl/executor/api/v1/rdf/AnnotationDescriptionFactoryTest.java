package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.rdf.utils.pojo.Descriptor;
import com.linkedpipes.etl.rdf.utils.pojo.DescriptorFactory;
import com.linkedpipes.etl.rdf.utils.pojo.LoaderException;
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

        @RdfToPojo.Resource
        private String iri;


    }

    @Test
    public void simpleScan() throws LoaderException {
        DescriptorFactory factory = new AnnotationDescriptionFactory();
        Descriptor desc = factory.create(TestObject.class);

        Assert.assertEquals("http://type", desc.getObjectType());
        Assert.assertNotNull(desc.getFieldForPredicate("http://label"));
        Assert.assertNull(desc.getFieldForResource());
    }

    @Test
    public void inheritanceScan() throws LoaderException {
        DescriptorFactory factory = new AnnotationDescriptionFactory();
        Descriptor desc = factory.create(TestInheritance.class);

        Assert.assertEquals("http://inheritance", desc.getObjectType());
        Assert.assertNotNull(desc.getFieldForPredicate("http://name"));
        Assert.assertNotNull(desc.getFieldForPredicate("http://label"));
        Assert.assertNotNull(desc.getFieldForResource());
    }

}
