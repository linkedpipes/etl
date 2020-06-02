package com.linkedpipes.etl.executor.api.v1.rdf.pojo;

import com.linkedpipes.etl.executor.api.v1.rdf.LanguageString;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class FieldLoaderTest {

    public enum TestEnum {
        VALUE_A,
        VALUE_B
    }

    public static class TestLangString extends LanguageString {

    }

    public static class TestClass {

        public int intValue;

        public String stringValue;

        private boolean booleanValue;

        private TestClass reference;

        public List<TestClass> list = new LinkedList<>();

        public String[] array;

        public List<Integer> intList = new LinkedList<>();

        public TestEnum enumValue;

        public TestLangString langString;

        public Date dateValue;

        public List<List<String>> nestedCollection;

        public List<TestEnum> enumCollection = new LinkedList<>();

        public List<String[]> arrayCollection;

        public List<TestLangString> langStringCollection = new LinkedList<>();

        public String getStringValue() {
            return stringValue;
        }

        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public TestClass getReference() {
            return reference;
        }

        public void setReference(
                TestClass reference) {
            this.reference = reference;
        }
    }

    @Test
    public void loadInteger() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("intValue");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asLong()).thenReturn(12L);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assert.assertEquals(12, instance.intValue);
    }

    @Test
    public void loadString() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("stringValue");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asString()).thenReturn("value");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assert.assertEquals("value", instance.stringValue);
    }

    @Test
    public void loadBoolean() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("booleanValue");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assert.assertEquals(true, instance.booleanValue);
    }

    @Test
    public void loadReference() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("reference");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asString()).thenReturn("http://localhost");

        Assert.assertNull(instance.reference);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assert.assertNotNull(instance.reference);
    }

    @Test
    public void loadNewReference() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("reference");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asString()).thenReturn("http://localhost");

        FieldLoader loader = new FieldLoader();
        Object firstReference = loader.set(instance, field, value, true);
        Object secondReference = loader.set(instance, field, value, false);

        Assert.assertNotEquals(firstReference, secondReference);
    }

    @Test
    public void extendReference() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("reference");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asString()).thenReturn("http://localhost");

        FieldLoader loader = new FieldLoader();
        Object firstReference = loader.set(instance, field, value, true);
        Object secondReference = loader.set(instance, field, value, true);

        Assert.assertEquals(firstReference, secondReference);
    }

    @Test
    public void loadCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("list");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);
        loader.set(instance, field, value, true);

        Assert.assertEquals(2, instance.list.size());
    }

    @Test(expected = RdfException.class)
    public void loadNullCollection() throws Exception {
        TestClass instance = new TestClass();
        instance.list = null;
        Field field = TestClass.class.getDeclaredField("list");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);
    }

    @Test
    public void loadClearLoadCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("list");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);
        loader.set(instance, field, value, false);

        Assert.assertEquals(1, instance.list.size());
    }

    @Test(expected = RdfException.class)
    public void loadArray() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("array");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);
    }

    @Test
    public void loadPrimitiveCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("intList");
        RdfValue value = Mockito.mock(RdfValue.class);
        FieldLoader loader = new FieldLoader();

        Mockito.when(value.asLong()).thenReturn(11L);
        loader.set(instance, field, value, true);

        Mockito.when(value.asLong()).thenReturn(12L);
        loader.set(instance, field, value, true);

        Assert.assertEquals(2, instance.intList.size());
        Assert.assertTrue(11 == instance.intList.get(0));
        Assert.assertTrue(12 == instance.intList.get(1));
    }

    @Test
    public void loadEnum() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("enumValue");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asString()).thenReturn("VALUE_A");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assert.assertEquals(TestEnum.VALUE_A, instance.enumValue);
    }

    @Test
    public void loadLangString() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("langString");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asString()).thenReturn("value");
        Mockito.when(value.getLanguage()).thenReturn("cs");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assert.assertNotNull(instance.langString);
        Assert.assertEquals("value", instance.langString.getValue());
        Assert.assertEquals("cs", instance.langString.getLanguage());
    }

    @Test
    public void loadDate() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("dateValue");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asString()).thenReturn("2010-02-20");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(instance.dateValue);

        Assert.assertEquals(2010, calendar.get(Calendar.YEAR));
        Assert.assertEquals(1, calendar.get(Calendar.MONTH));
        Assert.assertEquals(20, calendar.get(Calendar.DAY_OF_MONTH));
    }


    @Test(expected = RdfException.class)
    public void loadNestedCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("nestedCollection");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);
    }

    @Test
    public void loadEnumCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("enumCollection");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asString()).thenReturn("VALUE_A");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assert.assertEquals(1, instance.enumCollection.size());
        Assert.assertEquals(TestEnum.VALUE_A, instance.enumCollection.get(0));
    }

    @Test(expected = RdfException.class)
    public void loadArrayCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("arrayCollection");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);
    }

    @Test
    public void loadLangStringCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("langStringCollection");
        RdfValue value = Mockito.mock(RdfValue.class);
        Mockito.when(value.asString()).thenReturn("value");
        Mockito.when(value.getLanguage()).thenReturn("cs");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assert.assertEquals(1, instance.langStringCollection.size());
        TestLangString langString = instance.langStringCollection.get(0);
        Assert.assertEquals("value", langString.getValue());
        Assert.assertEquals("cs", langString.getLanguage());
    }

}
