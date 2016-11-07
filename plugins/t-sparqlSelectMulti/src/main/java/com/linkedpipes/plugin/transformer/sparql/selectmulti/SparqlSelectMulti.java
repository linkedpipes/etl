package com.linkedpipes.plugin.transformer.sparql.selectmulti;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.IRI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Škoda Petr
 */
public final class SparqlSelectMulti implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlSelectMulti.class);

    /**
     * Used to store configuration of the component.
     */
    private static final class Configuration {

        private final String iri;

        private final String query;

        private final String fileName;

        Configuration(String iri, String query, String fileName) {
            this.iri = iri;
            this.query = query;
            this.fileName = fileName;
        }

    }

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        final List<Configuration> configurations = new LinkedList<>();
        // Load configurations.
        configurationRdf.execute((connection) -> {
            final TupleQuery query = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, getConfigurationQuery());
            final TupleQueryResult results = query.evaluate();
            while (results.hasNext()) {
                final BindingSet binding = results.next();
                configurations.add(new Configuration(
                        binding.getValue("s").stringValue(),
                        binding.getValue("query").stringValue(),
                        binding.getValue("fileName").stringValue()));
            }
        });
        // Transform.
        for (Configuration configuration : configurations) {
            if (configuration.fileName == null
                    || configuration.fileName.isEmpty()) {
                throw exceptionFactory.failure("Missing property: {} on {}",
                        SparqlSelectMultiVocabulary.HAS_FILE_NAME,
                        configuration.iri);
            }
            transform(configuration.query, configuration.fileName);
        }
    }

    private void transform(String queryString, String outputFileName)
            throws LpException {
        final IRI inputGraph = inputRdf.getGraph();
        final File outputFile = outputFiles.createFile(outputFileName).toFile();
        LOG.info("\n{}\n    -> {}", queryString, outputFileName);
        final SPARQLResultsCSVWriterFactory writerFactory = new SPARQLResultsCSVWriterFactory();
        // Create output file and write the result.
        inputRdf.execute((connection) -> {
            try (final OutputStream outputStream
                    = new FileOutputStream(outputFile)) {
                final TupleQueryResultWriter resultWriter
                        = writerFactory.getWriter(outputStream);
                final TupleQuery query = connection.prepareTupleQuery(
                        QueryLanguage.SPARQL, queryString);
                final SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(inputGraph);
                // We need to add this else we can not use
                // GRAPH ?g in query.
                dataset.addNamedGraph(inputGraph);
                query.setDataset(dataset);
                query.evaluate(resultWriter);
            } catch (IOException ex) {
                throw exceptionFactory.failure("Exception.", ex);
            }
        });
    }

    private static String getConfigurationQuery() {
        return "SELECT DISTINCT ?s ?query ?fileName WHERE {\n"
                + "  ?s a <" + SparqlSelectMultiVocabulary.CONFIG
                + "> ;\n"
                + "    <" + SparqlSelectMultiVocabulary.HAS_FILE_NAME
                + "> ?fileName ;\n"
                + "    <" + SparqlSelectMultiVocabulary.HAS_QUERY
                + "> ?query .\n"
                + "}";
    }

}
