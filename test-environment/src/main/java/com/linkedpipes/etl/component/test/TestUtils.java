package com.linkedpipes.etl.component.test;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.repository.util.Repositories;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

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
            inserter.enforceContext(dataUnit.getGraph());
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
    public static void store(SingleGraphDataUnit dataUnit, File file,
            RDFFormat format) throws Exception {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            final RDFWriter writer = Rio.createWriter(format, outputStream);
            Repositories.consume(dataUnit.getRepository(), (connection) -> {
                connection.export(writer, dataUnit.getGraph());
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
    public static void store(Repository repository, File file,
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

    /**
     * Print content of given data unit to the standard output.
     *
     * @param dataUnit
     */
    public static void printContent(WritableGraphListDataUnit dataUnit)
            throws LpException {
        for (IRI graph : dataUnit.getGraphs()) {
            System.out.println(": " + graph.toString());
            final RepositoryConnection connection
                    = dataUnit.getRepository().getConnection();
            final RepositoryResult<Statement> result
                    = connection.getStatements(null, null, null, false);
            while (result.hasNext()) {
                final Statement st = result.next();
                System.out.println("\t" + st.getSubject().stringValue()
                        + "\t" + st.getPredicate().stringValue()
                        + "\t" + st.getObject().stringValue());

            }
        }
    }

    ;

}
