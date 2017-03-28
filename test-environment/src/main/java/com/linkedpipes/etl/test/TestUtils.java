package com.linkedpipes.etl.test;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.util.RDFInserter;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;

public class TestUtils {

    private TestUtils() {
    }

    public static void load(WritableSingleGraphDataUnit dataUnit, File file,
            RDFFormat format) throws Exception {
        final RDFParser rdfParser = Rio.createParser(format);
        Repositories.consume(dataUnit.getRepository(), (connection) -> {
            final RDFInserter inserter = new RDFInserter(connection);
            inserter.enforceContext(dataUnit.getWriteGraph());
            rdfParser.setRDFHandler(inserter);
            try (InputStream input = new FileInputStream(file)) {
                rdfParser.parse(input, "http://localhost/");
            } catch (IOException ex) {
                throw new RuntimeException("Can't import data.", ex);
            }
        });
    }

    /**
     * Store content of given data unit into a file.
     *
     * @param dataUnit
     * @param file
     * @param format
     */
    public static void save(SingleGraphDataUnit dataUnit, File file,
            RDFFormat format) throws Exception {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            final RDFWriter writer = Rio.createWriter(format, outputStream);
            Repositories.consume(dataUnit.getRepository(), (connection) -> {
                connection.export(writer, dataUnit.getReadGraph());
            });
        }
    }

    /**
     * Store content of given repository into a file.
     *
     * @param repository
     * @param file
     * @param format
     */
    public static void save(Repository repository, File file,
            RDFFormat format) throws Exception {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            final RDFWriter writer = Rio.createWriter(format, outputStream);
            Repositories.consume(repository, (connection) -> {
                connection.export(writer);
            });
        }
    }

    /**
     * @param fileName
     * @return Path to file in test resources.
     */
    public static File fileFromResource(String fileName) {
        final URL url = Thread.currentThread().getContextClassLoader().
                getResource(fileName);
        if (url == null) {
            throw new RuntimeException("Required resource '"
                    + fileName + "' is missing.");
        }
        return new File(url.getPath());
    }

    /**
     * @return Randomly named directory in temp folder.
     */
    public static File getTempDirectory() throws IOException {
        return Files.createTempDirectory("lp-test-dpu-").toFile();
    }

}
