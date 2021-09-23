package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

    public static class TestLangString implements LangString {

        String value;

        String language;

        @Override
        public void setValue(String value, String language) {
            this.value = value;
            this.language = language;
        }
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
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asLong()).thenReturn(12L);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assertions.assertEquals(12, instance.intValue);
    }

    @Test
    public void loadString() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("stringValue");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asString()).thenReturn("value");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assertions.assertEquals("value", instance.stringValue);
    }

    @Test
    public void loadBoolean() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("booleanValue");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assertions.assertEquals(true, instance.booleanValue);
    }

    @Test
    public void loadReference() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("reference");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asString()).thenReturn("http://localhost");

        Assertions.assertNull(instance.reference);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assertions.assertNotNull(instance.reference);
    }

    @Test
    public void loadNewReference() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("reference");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asString()).thenReturn("http://localhost");

        FieldLoader loader = new FieldLoader();
        Object firstReference = loader.set(instance, field, value, true);
        Object secondReference = loader.set(instance, field, value, false);

        Assertions.assertNotEquals(firstReference, secondReference);
    }

    @Test
    public void extendReference() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("reference");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asString()).thenReturn("http://localhost");

        FieldLoader loader = new FieldLoader();
        Object firstReference = loader.set(instance, field, value, true);
        Object secondReference = loader.set(instance, field, value, true);

        Assertions.assertEquals(firstReference, secondReference);
    }

    @Test
    public void loadCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("list");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);
        loader.set(instance, field, value, true);

        Assertions.assertEquals(2, instance.list.size());
    }

    @Test
    public void loadNullCollection() throws Exception {
        TestClass instance = new TestClass();
        instance.list = null;
        Field field = TestClass.class.getDeclaredField("list");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        Assertions.assertThrows(LoaderException.class, () -> {
            loader.set(instance, field, value, true);
        });
    }

    @Test
    public void loadClearLoadCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("list");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);
        loader.set(instance, field, value, false);

        Assertions.assertEquals(1, instance.list.size());
    }

    @Test
    public void loadArray() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("array");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        Assertions.assertThrows(LoaderException.class, () -> {
            loader.set(instance, field, value, true);
        });
    }

    @Test
    public void loadPrimitiveCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("intList");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        FieldLoader loader = new FieldLoader();

        Mockito.when(value.asLong()).thenReturn(11L);
        loader.set(instance, field, value, true);

        Mockito.when(value.asLong()).thenReturn(12L);
        loader.set(instance, field, value, true);

        Assertions.assertEquals(2, instance.intList.size());
        Assertions.assertTrue(11 == instance.intList.get(0));
        Assertions.assertTrue(12 == instance.intList.get(1));
    }

    @Test
    public void loadEnum() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("enumValue");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asString()).thenReturn("VALUE_A");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assertions.assertEquals(TestEnum.VALUE_A, instance.enumValue);
    }

    @Test
    public void loadLangString() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("langString");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asString()).thenReturn("value");
        Mockito.when(value.getLanguage()).thenReturn("cs");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assertions.assertNotNull(instance.langString);
        Assertions.assertEquals("value", instance.langString.value);
        Assertions.assertEquals("cs", instance.langString.language);
    }

    @Test
    public void loadDate() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("dateValue");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asString()).thenReturn("2010-02-20");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(instance.dateValue);

        Assertions.assertEquals(2010, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(1, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(20, calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void loadNestedCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("nestedCollection");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        Assertions.assertThrows(LoaderException.class, () -> {
            loader.set(instance, field, value, true);
        });
    }

    @Test
    public void loadEnumCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("enumCollection");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asString()).thenReturn("VALUE_A");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assertions.assertEquals(1, instance.enumCollection.size());
        Assertions.assertEquals(TestEnum.VALUE_A, instance.enumCollection.get(0));
    }

    @Test
    public void loadArrayCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("arrayCollection");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asBoolean()).thenReturn(true);

        FieldLoader loader = new FieldLoader();
        Assertions.assertThrows(LoaderException.class, () -> {
            loader.set(instance, field, value, true);
        });
    }

    @Test
    public void loadLangStringCollection() throws Exception {
        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("langStringCollection");
        BackendRdfValue value = Mockito.mock(BackendRdfValue.class);
        Mockito.when(value.asString()).thenReturn("value");
        Mockito.when(value.getLanguage()).thenReturn("cs");

        FieldLoader loader = new FieldLoader();
        loader.set(instance, field, value, true);

        Assertions.assertEquals(1, instance.langStringCollection.size());
        TestLangString langString = instance.langStringCollection.get(0);
        Assertions.assertEquals("value", langString.value);
        Assertions.assertEquals("cs", langString.language);
    }

}
