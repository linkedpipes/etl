package com.linkedpipes.etl.rdf.utils;

import org.junit.Assert;
import org.junit.Test;

public class RdfUtilsExceptionTest {

    @Test
    public void passParameters() {
        RdfUtilsException exception = new RdfUtilsException("text: {}", 1);
        Assert.assertEquals("text: 1", exception.getMessage());
    }

    @Test
    public void passParametersAndException() {
        Exception root = new Exception();
        RdfUtilsException exception =
                new RdfUtilsException("text: {}", 1, root);
        Assert.assertEquals("text: 1", exception.getMessage());
        Assert.assertEquals(root, exception.getCause());
    }

}
