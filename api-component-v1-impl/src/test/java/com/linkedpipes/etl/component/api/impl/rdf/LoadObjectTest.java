package com.linkedpipes.etl.component.api.impl.rdf;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;

/**
 * Test suite for {@link LoadObject} class.
 *
 * @author Petr Škoda
 */
public class LoadObjectTest {

    @RdfToPojo.Type(uri = "http://localhost/ontology/ClassOne")
    public static class TestClassOne {

        @RdfToPojo.Property(uri = "http://localhost/ontology/value")
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    @RdfToPojo.Value
    public static class TestClassTwo {

        @RdfToPojo.Value
        private String value;

        @RdfToPojo.Lang
        private String language;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

    }

    @RdfToPojo.Type(uri = "http://localhost/ontology/ClassThree")
    public static class TestClassThree {

        @RdfToPojo.Property(uri = "http://localhost/ontology/one")
        private TestClassOne one;

        @RdfToPojo.Property(uri = "http://localhost/ontology/two")
        private TestClassTwo two;

        @RdfToPojo.Property(uri = "http://localhost/ontology/list")
        private List<TestClassOne> list = new LinkedList<>();

        @RdfToPojo.Property(uri = "http://localhost/ontology/listTwo")
        private List<TestClassTwo> listTwo = new LinkedList<>();

        public TestClassOne getOne() {
            return one;
        }

        public void setOne(TestClassOne one) {
            this.one = one;
        }

        public TestClassTwo getTwo() {
            return two;
        }

        public void setTwo(TestClassTwo two) {
            this.two = two;
        }

        public List<TestClassOne> getList() {
            return list;
        }

        public void setList(List<TestClassOne> list) {
            this.list = list;
        }

        public List<TestClassTwo> getListTwo() {
            return listTwo;
        }

        public void setListTwo(List<TestClassTwo> listTwo) {
            this.listTwo = listTwo;
        }

    }

    @RdfToPojo.Type(uri = "http://localhost/ontology/ClassFour")
    public static class TestClassFour {

        @RdfToPojo.Property(uri = "http://localhost/ontology/value")
        private List<String> value = new LinkedList<>();

        public List<String> getValue() {
            return value;
        }

        public void setValue(List<String> value) {
            this.value = value;
        }

    }

    @Test
    public void loadDescriptionTest() throws Loader.CanNotDeserializeObject {
        final RdfDataSource data = new RdfDataSource();
        final ValueFactory vf = SimpleValueFactory.getInstance();

        //
        data.add("http://localhost/one", RDF.TYPE,
                vf.createIRI("http://localhost/ontology/ClassOne"));

        data.add("http://localhost/one", "http://localhost/ontology/value",
                vf.createLiteral("hodnota"));

        //
        data.add("http://localhost/item_0", RDF.TYPE,
                vf.createIRI("http://localhost/ontology/ClassOne"));

        data.add("http://localhost/item_0", "http://localhost/ontology/value",
                vf.createLiteral("0"));

        //
        data.add("http://localhost/item_1", RDF.TYPE,
                vf.createIRI("http://localhost/ontology/ClassOne"));

        data.add("http://localhost/item_1", "http://localhost/ontology/value",
                vf.createLiteral("1"));

        //
        data.add("http://localhost/three", RDF.TYPE,
                vf.createIRI("http://localhost/ontology/ClassThree"));

        data.add("http://localhost/three", "http://localhost/ontology/one",
                vf.createIRI("http://localhost/one"));

        data.add("http://localhost/three", "http://localhost/ontology/two",
                vf.createLiteral("anglictina", "en"));

        data.add("http://localhost/three", "http://localhost/ontology/list",
                vf.createIRI("http://localhost/item_0"));

        data.add("http://localhost/three", "http://localhost/ontology/list",
                vf.createIRI("http://localhost/item_1"));

        data.add("http://localhost/three", "http://localhost/ontology/listTwo",
                vf.createLiteral("anglictina", "en"));

        data.add("http://localhost/three", "http://localhost/ontology/listTwo",
                vf.createLiteral("cestina", "cs"));

        //
        final TestClassThree object = (TestClassThree) LoadObject.loadNew(
                TestClassThree.class, "http://localhost/three", null, data,
                loadAllOptions());

        Assert.assertNotNull(object.one);
        Assert.assertEquals("hodnota", object.one.value);

        Assert.assertNotNull(object.two);
        Assert.assertEquals("anglictina", object.two.value);
        Assert.assertEquals("en", object.two.language);

        Assert.assertNotNull(object.list);
        Assert.assertEquals(2, object.list.size());

        Assert.assertNotNull(object.listTwo);
        Assert.assertEquals(2, object.listTwo.size());
    }

    @Test
    public void loadLinkListTest() throws Loader.CanNotDeserializeObject {
        final RdfDataSource data = new RdfDataSource();
        final ValueFactory vf = SimpleValueFactory.getInstance();

        //
        data.add("http://localhost/four", RDF.TYPE,
                vf.createIRI("http://localhost/ontology/ClassFour"));

        data.add("http://localhost/four", "http://localhost/ontology/value",
                vf.createIRI("http://localhost/resources/1"));

        data.add("http://localhost/four", "http://localhost/ontology/value",
                vf.createIRI("http://localhost/resources/2"));

        //
        final TestClassFour object = (TestClassFour) LoadObject.loadNew(
                TestClassFour.class, "http://localhost/four", null, data,
                loadAllOptions());

        Assert.assertNotNull(object.value);
        Assert.assertEquals(2, object.value.size());

    }

    private static RdfReader.MergeOptionsFactory loadAllOptions() {
        return (resourceIri, graph) -> (predicate -> true);
    }

}
