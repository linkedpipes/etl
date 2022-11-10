package com.linkedpipes.etl.storage;

import com.linkedpipes.etl.library.rdf.Statements;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TestUtils {

    private static final ClassLoader loader =
            Thread.currentThread().getContextClassLoader();

    public static File file(String fileName) {
        URL url = loader.getResource(fileName);
        if (url == null) {
            throw new RuntimeException(
                    "Required resource '" + fileName + "' is missing.");
        }
        return new File(url.getPath());
    }

    public static Statements statements(String fileName) throws IOException {
        File file = file(fileName);
        Statements result = Statements.arrayList();
        result.file().addAll(file);
        return result;
    }

}
