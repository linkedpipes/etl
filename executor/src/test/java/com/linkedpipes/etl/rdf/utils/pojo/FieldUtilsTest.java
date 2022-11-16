package com.linkedpipes.etl.rdf.utils.pojo;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class FieldUtilsTest {

    public static class TestClass {

        public String publicValue;

        protected String protectedValue;

        protected String inaccessible;

        public String getProtectedValue() {
            return protectedValue;
        }

        public void setProtectedValue(String protectedValue) {
            this.protectedValue = protectedValue;
        }
    }

    @Test
    public void setPublic() throws Exception {
        TestClass entity = new TestClass();
        Field field = TestClass.class.getDeclaredField("publicValue");
        FieldUtils.setValue(entity, field, "string");
    }

    @Test
    public void setByGetter() throws Exception {
        TestClass entity = new TestClass();
        Field field = TestClass.class.getDeclaredField("protectedValue");
        FieldUtils.setValue(entity, field, "string");
    }

    @Test
    public void setFailed() throws Exception {
        TestClass entity = new TestClass();
        Field field = TestClass.class.getDeclaredField("inaccessible");
        Assertions.assertThrows(LoaderException.class, () -> {
            FieldUtils.setValue(entity, field, "string");
        });
    }

    @Test
    public void getPublic() throws Exception {
        TestClass entity = new TestClass();
        entity.publicValue = "value";
        Field field = TestClass.class.getDeclaredField("publicValue");
        String actualValue = (String)FieldUtils.getValue(entity, field);
        Assertions.assertEquals(entity.publicValue, actualValue);
    }

    @Test
    public void getByGetter() throws Exception {
        TestClass entity = new TestClass();
        entity.setProtectedValue("value");
        Field field = TestClass.class.getDeclaredField("protectedValue");
        FieldUtils.getValue(entity, field);
        String actualValue = (String)FieldUtils.getValue(entity, field);
        Assertions.assertEquals(entity.getProtectedValue(), actualValue);
    }

    @Test
    public void getFailed() throws Exception {
        TestClass entity = new TestClass();
        Field field = TestClass.class.getDeclaredField("inaccessible");
        Assertions.assertThrows(LoaderException.class, () -> {
            FieldUtils.getValue(entity, field);
        });
    }

}
