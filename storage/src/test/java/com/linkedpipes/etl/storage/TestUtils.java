package com.linkedpipes.etl.storage;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.eclipse.rdf4j.model.Statement;

import java.io.File;
import java.net.URL;
import java.util.Collection;

public class TestUtils {

    private static final ClassLoader loader =
            Thread.currentThread().getContextClassLoader();

    public static File file(String name) {
        return fileFromResource(name);
    }

    public static File fileFromResource(String fileName) {
        URL url = loader.getResource(fileName);
        if (url == null) {
            throw new RuntimeException(
                    "Required resource '" + fileName + "' is missing.");
        }
        return new File(url.getPath());
    }

    public static Collection<Statement> rdfFromResource(String fileName)
            throws RdfUtils.RdfException {
        File file = fileFromResource(fileName);
        return RdfUtils.read(file);
    }

    public static StatementsSelector statements(String resourceName)
            throws RdfUtils.RdfException {
        return Statements.wrap(
                TestUtils.rdfFromResource(resourceName)).selector();
    }

}
