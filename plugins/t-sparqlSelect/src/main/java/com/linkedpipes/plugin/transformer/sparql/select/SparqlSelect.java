package com.linkedpipes.plugin.transformer.sparql.select;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class SparqlSelect implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(SparqlSelect.class);

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public SparqlSelectConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getFileName() == null
                || configuration.getFileName().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlSelectVocabulary.HAS_FILE_NAME, "");
        }
        if (configuration.getQuery() == null
                || configuration.getQuery().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlSelectVocabulary.HAS_QUERY, "");
        }
        //
        final IRI inputGraph = inputRdf.getReadGraph();
        final File outputFile =
                outputFiles.createFile(configuration.getFileName());
        LOG.info("{} -> {}", inputGraph, outputFile);
        final SPARQLResultsCSVWriterFactory writerFactory =
                new SPARQLResultsCSVWriterFactory();
        // Create output file and write the result.
        inputRdf.execute((connection) -> {
            try (final OutputStream outputStream = new FileOutputStream(
                    outputFile)) {
                final TupleQueryResultWriter resultWriter =
                        writerFactory.getWriter(outputStream);
                final TupleQuery query = connection
                        .prepareTupleQuery(QueryLanguage.SPARQL,
                                configuration.getQuery());
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

}
