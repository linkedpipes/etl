package com.linkedpipes.etl.library.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class FileNameTest {

    @Test
    public void simpleConversion() {
        String expected = "http://localhost:8080/name?key=value";
        String fileName = FileName.asFileName(expected);
        File file = new File("./" + fileName);
        Assertions.assertEquals(expected, FileName.asIri(file));
    }

}
