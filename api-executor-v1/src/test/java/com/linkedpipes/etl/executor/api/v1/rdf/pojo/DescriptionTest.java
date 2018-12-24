package com.linkedpipes.etl.executor.api.v1.rdf.pojo;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;
import org.junit.Assert;
import org.junit.Test;

public class DescriptionTest {

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
    public void simpleScan()  {
        Descriptor desc = new Descriptor(TestObject.class);

        Assert.assertEquals("http://type", desc.getObjectType());
        Assert.assertNotNull(desc.getFieldForPredicate("http://label"));
        Assert.assertNull(desc.getFieldForResource());
    }

    @Test
    public void inheritanceScan() {
        Descriptor desc = new Descriptor(TestInheritance.class);

        Assert.assertEquals("http://inheritance", desc.getObjectType());
        Assert.assertNotNull(desc.getFieldForPredicate("http://name"));
        Assert.assertNotNull(desc.getFieldForPredicate("http://label"));
        Assert.assertNotNull(desc.getFieldForResource());
    }

}
