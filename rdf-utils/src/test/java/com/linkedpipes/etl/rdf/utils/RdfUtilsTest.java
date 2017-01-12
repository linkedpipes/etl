package com.linkedpipes.etl.rdf.utils;

import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class RdfUtilsTest {

    public static class Label implements RdfLoader.LangString {

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

    public static class SubTestEntity implements RdfLoader.Loadable<String> {

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

    public static class TestEntity implements RdfLoader.Loadable<String> {

        private String value;

        private Label label;

        private List<Label> labelList = new LinkedList<>();

        private SubTestEntity ref;

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            switch (predicate) {
                case "http://value":
                    value = object;
                    break;
                case "http://label":
                    value = object;
                    break;
                case "http://labelList":
                    labelList.add(new Label(object));
                    break;
                case "http://ref":
                    ref = new SubTestEntity();
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

        public Label getLabel() {
            return label;
        }

        public void setLabel(Label label) {
            this.label = label;
        }

        public List<Label> getLabelList() {
            return labelList;
        }

        public void setLabelList(
                List<Label> labelList) {
            this.labelList = labelList;
        }

        public SubTestEntity getRef() {
            return ref;
        }

        public void setRef(SubTestEntity ref) {
            this.ref = ref;
        }
    }

    private static class Descriptor implements RdfLoader.Descriptor {

        private final Class<?> type;

        public Descriptor(Class<?> type) {
            this.type = type;
        }

        @Override
        public String getType() {
            return "http://T";
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
    public void loadTest() throws RdfUtilsException {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        final TestEntity entity = new TestEntity();
        //
        builder.entity("http://e")
                .string("http://value", "1")
                .iri(RDF.TYPE, "http://T")
                .string("http://labelList", "lbl-en", "en")
                .string("http://labelList", "lbl")
                .entity("http://ref", "http://f")
                .string("http://value", "3");
        builder.commit();
        //
        RdfUtils.load(source, entity, "http://e", "http://graph", String.class);
        //
        Assert.assertEquals("1", entity.value);
        Assert.assertNotNull(entity.ref);
        Assert.assertEquals("3", entity.ref.value);
        Assert.assertEquals(2, entity.labelList.size());
        Assert.assertEquals("lbl-en", entity.labelList.get(0).value);
        Assert.assertEquals("lbl", entity.labelList.get(1).value);
        //
        source.shutdown();
    }

    @Test
    public void loadByReflectionTest() throws RdfUtilsException {
        final RdfSource source = Rdf4jSource.createInMemory();
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        final TestEntity entity = new TestEntity();
        final RdfLoader.DescriptorFactory descriptorFactory =
                (clazz) -> new Descriptor(clazz);
        //
        builder.entity("http://e")
                .string("http://value", "1")
                .iri(RDF.TYPE, "http://T")
                .string("http://labelList", "lbl-en", "en")
                .string("http://labelList", "lbl")
                .entity("http://ref", "http://f")
                .string("http://value", "3");
        builder.commit();
        //
        RdfUtils.loadTypedByReflection(source, entity, "http://graph",
                descriptorFactory);
        //
        Assert.assertEquals("1", entity.value);
        Assert.assertNotNull(entity.ref);
        Assert.assertEquals("3", entity.ref.value);
        Assert.assertEquals(2, entity.labelList.size());
        Assert.assertEquals("lbl-en", entity.labelList.get(0).value);
        Assert.assertEquals("lbl", entity.labelList.get(1).value);
        Assert.assertEquals("en", entity.labelList.get(0).lang);
        Assert.assertNull(entity.labelList.get(1).lang);
        //
        source.shutdown();
    }

}
