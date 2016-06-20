package com.linkedpipes.plugin.transformer.sparql.select;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openrdf.model.IRI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;

/**
 *
 * @author Škoda Petr
 */
public final class SparqlSelect implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlSelect.class);

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public SparqlSelectConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getFileName() == null
                || configuration.getFileName().isEmpty()) {
            throw exceptionFactory.invalidConfigurationProperty(
                    SparqlSelectVocabulary.HAS_FILE_NAME, "");
        }
        if (configuration.getQuery()== null
                || configuration.getQuery().isEmpty()) {
            throw exceptionFactory.invalidConfigurationProperty(
                    SparqlSelectVocabulary.HAS_QUERY, "");
        }
        //
        final IRI inputGraph = inputRdf.getGraph();
        final File outputFile = outputFiles.createFile(configuration.getFileName()).toFile();
        LOG.info("{} -> {}", inputGraph, outputFile);
        final SPARQLResultsCSVWriterFactory writerFactory = new SPARQLResultsCSVWriterFactory();
        // Create output file and write the result.
        inputRdf.execute((connection) -> {
            try (final OutputStream outputStream = new FileOutputStream(outputFile)) {
                final TupleQueryResultWriter resultWriter = writerFactory.getWriter(outputStream);
                final TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, configuration.getQuery());
                final SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(inputGraph);
                // We need to add this else we can not use
                // GRAPH ?g in query.
                dataset.addNamedGraph(inputGraph);
                query.setDataset(dataset);
                query.evaluate(resultWriter);
            } catch (IOException ex) {
                throw exceptionFactory.failed("Exception.", ex);
            }
        });
    }

}
