package com.linkedpipes.etl.rdf.utils;

import com.linkedpipes.etl.rdf.utils.model.ClosableRdfSource;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.*;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class RdfUtilsTest {

    public static class Label implements LangString {

        private String value;

        private String lang;

        public Label() {
        }

        public Label(String value) {
            this.value = value;
        }

        @Override
        public void setValue(String value, String lang) {
            this.value = value;
            this.lang = lang;
        }
    }

    public static class SubTestEntity implements Loadable {

        public String value;

        @Override
        public void resource(String resource) throws LoaderException {
            // No action.
        }

        @Override
        public Loadable load(String predicate, BackendRdfValue object) {
            if ("http://value".equals(predicate)) {
                value = object.asString();
            }
            return null;
        }
    }

    public static class TestEntity implements Loadable {

        public String value;

        public Label label;

        public List<Label> labelList = new LinkedList<>();

        public SubTestEntity ref;

        @Override
        public void resource(String resource) throws LoaderException {
            // No action.
        }

        @Override
        public Loadable load(String predicate, BackendRdfValue object) {
            switch (predicate) {
                case "http://value":
                    value = object.asString();
                    break;
                case "http://label":
                    value = object.asString();
                    break;
                case "http://labelList":
                    labelList.add(new Label(object.asString()));
                    break;
                case "http://ref":
                    ref = new SubTestEntity();
                    return ref;
            }
            return null;
        }

    }

    private static class TestDescriptor implements Descriptor {

        private final Class<?> type;

        public TestDescriptor(Class<?> type) {
            this.type = type;
        }

        @Override
        public String getObjectType() {
            return "http://T";
        }

        @Override
        public Field getFieldForResource() {
            return null;
        }

        @Override
        public Field getFieldForPredicate(String predicate) {
            predicate = predicate.replace("http://", "");
            try {
                return type.getDeclaredField(predicate);
            } catch (NoSuchFieldException ex) {
                return null;
            }
        }

    }

    @Test
    public void loadTest() throws RdfUtilsException {
        ClosableRdfSource source = Rdf4jSource.createInMemory();
        RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        TestEntity entity = new TestEntity();
        builder.entity("http://e")
                .string("http://value", "1")
                .iri(RDF.TYPE, "http://T")
                .string("http://labelList", "lbl-en", "en")
                .string("http://labelList", "lbl")
                .entity("http://ref", "http://f")
                .string("http://value", "3");
        builder.commit();
        RdfUtils.load(source, "http://e", "http://graph", entity);

        Assert.assertEquals("1", entity.value);
        Assert.assertNotNull(entity.ref);
        Assert.assertEquals("3", entity.ref.value);
        Assert.assertEquals(2, entity.labelList.size());
        Assert.assertEquals("lbl-en", entity.labelList.get(0).value);
        Assert.assertEquals("lbl", entity.labelList.get(1).value);

        source.close();
    }

    @Test
    public void loadByReflectionTest() throws RdfUtilsException {
        ClosableRdfSource source = Rdf4jSource.createInMemory();
        RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        TestEntity entity = new TestEntity();
        DescriptorFactory descriptorFactory =
                (clazz) -> new TestDescriptor(clazz);
        builder.entity("http://e")
                .string("http://value", "1")
                .iri(RDF.TYPE, "http://T")
                .string("http://labelList", "lbl-en", "en")
                .string("http://labelList", "lbl")
                .entity("http://ref", "http://f")
                .string("http://value", "3");
        builder.commit();

        RdfUtils.loadByType(source, "http://graph",
                entity, descriptorFactory);

        Assert.assertEquals("1", entity.value);
        Assert.assertNotNull(entity.ref);
        Assert.assertEquals("3", entity.ref.value);
        Assert.assertEquals(2, entity.labelList.size());
        Assert.assertEquals("lbl-en", entity.labelList.get(0).value);
        Assert.assertEquals("lbl", entity.labelList.get(1).value);
        Assert.assertEquals("en", entity.labelList.get(0).lang);
        Assert.assertNull(entity.labelList.get(1).lang);

        source.close();
    }

}
