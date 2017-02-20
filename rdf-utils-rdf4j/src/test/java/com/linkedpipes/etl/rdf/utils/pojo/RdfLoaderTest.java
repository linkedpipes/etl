package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.RdfBuilder;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class RdfLoaderTest {

    static class TestObjectValue implements RdfLoader.Loadable<String> {

        private String value;

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            if ("http://value".equals(predicate)) {
                value = object;
            }
            return null;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    static class TestObjectReference implements RdfLoader.Loadable<String> {

        private String value;

        private TestObjectValue ref;

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            switch (predicate) {
                case "http://value":
                    value = object;
                    break;
                case "http://ref":
                    ref = new TestObjectValue();
                    return ref;
            }
            return null;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public TestObjectValue getRef() {
            return ref;
        }

        public void setRef(
                TestObjectValue ref) {
            this.ref = ref;
        }
    }

    static class TestObjectList implements RdfLoader.Loadable<String> {

        private List<TestObjectValue> list = new LinkedList<>();

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            switch (predicate) {
                case "http://list":
                    TestObjectValue newValue = new TestObjectValue();
                    list.add(newValue);
                    return newValue;
            }
            return null;
        }

        public List<TestObjectValue> getList() {
            return list;
        }

        public void setList(
                List<TestObjectValue> list) {
            this.list = list;
        }
    }

    private static class Descriptor implements RdfLoader.Descriptor {

        private final Class<?> type;

        public Descriptor(Class<?> type) {
            this.type = type;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public Field getField(String predicate) {
            predicate = predicate.replace("http://", "");
            try {
                return type.getDeclaredField(predicate);
            } catch (NoSuchFieldException ex) {
                return null;
            }
        }
    }

    @Test
    public void loadTestValue() throws RdfUtilsException {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        final TestObjectValue testObject = new TestObjectValue();
        //
        builder.entity("http://A")
                .string("http://value", "0")
                .string("http://value_1", "1");
        builder.commit();
        //
        RdfLoader.load(source, testObject, "http://A", "http://graph",
                String.class);
        //
        Assert.assertEquals("0", testObject.getValue());
    }

    @Test
    public void loadTestReference() throws RdfUtilsException {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        final TestObjectReference testObject = new TestObjectReference();
        //
        builder.entity("http://A")
                .string("http://value", "0")
                .entity("http://ref", "http://B")
                .string("http://value", "1");
        builder.commit();
        //
        RdfLoader.load(source, testObject, "http://A", "http://graph",
                String.class);
        //
        Assert.assertEquals("0", testObject.getValue());
        Assert.assertNotNull(testObject.getRef());
        Assert.assertEquals("1", testObject.getRef().getValue());
    }

    @Test
    public void loadTestList() throws RdfUtilsException {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        final TestObjectList testObject = new TestObjectList();
        //
        builder.entity("http://A")
                .entity("http://list", "http://B")
                .string("http://value", "1")
                .close()
                .entity("http://list", "http://C")
                .string("http://value", "2");
        builder.commit();
        //
        RdfLoader.load(source, testObject, "http://A", "http://graph",
                String.class);
        //
        Assert.assertEquals(2, testObject.getList().size());
        Assert.assertEquals("1", testObject.getList().get(0).getValue());
        Assert.assertEquals("2", testObject.getList().get(1).getValue());
    }

    @Test
    public void loadTestValueReflection() throws RdfUtilsException {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        final TestObjectValue testObject = new TestObjectValue();

        //
        builder.entity("http://A")
                .string("http://value", "0")
                .string("http://value_1", "1");
        builder.commit();
        //
        RdfLoader.loadByReflection(source, (clazz) -> new Descriptor(clazz),
                testObject, "http://A", "http://graph");
        //
        Assert.assertEquals("0", testObject.getValue());
    }

    @Test
    public void loadTestReferenceReflection() throws RdfUtilsException {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        final TestObjectReference testObject = new TestObjectReference();
        //
        builder.entity("http://A")
                .string("http://value", "0")
                .entity("http://ref", "http://B")
                .string("http://value", "1");
        builder.commit();
        //
        RdfLoader.loadByReflection(source, (clazz) -> new Descriptor(clazz),
                testObject, "http://A", "http://graph");
        //
        Assert.assertEquals("0", testObject.getValue());
        Assert.assertNotNull(testObject.getRef());
        Assert.assertEquals("1", testObject.getRef().getValue());
    }

    @Test
    public void loadTestListReflection() throws RdfUtilsException {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        final TestObjectList testObject = new TestObjectList();
        //
        builder.entity("http://A")
                .entity("http://list", "http://B")
                .string("http://value", "1")
                .close()
                .entity("http://list", "http://C")
                .string("http://value", "2");
        builder.commit();
        //
        RdfLoader.loadByReflection(source, (clazz) -> new Descriptor(clazz),
                testObject, "http://A", "http://graph");
        //
        Assert.assertEquals(2, testObject.getList().size());
        Assert.assertEquals("1", testObject.getList().get(0).getValue());
        Assert.assertEquals("2", testObject.getList().get(1).getValue());
    }

}
