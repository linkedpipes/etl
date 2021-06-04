package com.linkedpipes.etl.executor.api.v1.rdf.pojo;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Assertions.assertEquals("http://type", desc.getObjectType());
        Assertions.assertNotNull(desc.getFieldForPredicate("http://label"));
        Assertions.assertNull(desc.getFieldForResource());
    }

    @Test
    public void inheritanceScan() {
        Descriptor desc = new Descriptor(TestInheritance.class);

        Assertions.assertEquals("http://inheritance", desc.getObjectType());
        Assertions.assertNotNull(desc.getFieldForPredicate("http://name"));
        Assertions.assertNotNull(desc.getFieldForPredicate("http://label"));
        Assertions.assertNotNull(desc.getFieldForResource());
    }

}
