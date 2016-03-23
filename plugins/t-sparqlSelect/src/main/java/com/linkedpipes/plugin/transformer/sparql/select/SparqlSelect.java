package com.linkedpipes.plugin.transformer.sparql.select;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openrdf.model.URI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public final class SparqlSelect implements SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlSelect.class);

    @DataProcessingUnit.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @DataProcessingUnit.OutputPort(id = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @DataProcessingUnit.Configuration
    public SparqlSelectConfiguration configuration;

    @Override
    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException {
        // For each graph.
        final URI inputGraph = inputRdf.getGraph();
        final File outputFile = outputFiles.createFile(configuration.getFileName());
        LOG.info("{} -> {}", inputGraph, outputFile);
        final SPARQLResultsCSVWriterFactory writerFactory = new SPARQLResultsCSVWriterFactory();
        // Create output file and write the result.
        inputRdf.execute((connection) -> {
            try (final OutputStream outputStream = new FileOutputStream(outputFile)) {
                final TupleQueryResultWriter resultWriter = writerFactory.getWriter(outputStream);
                final TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, configuration.getQuery());
                query.evaluate(resultWriter);
            } catch (IOException ex) {
                throw new DataProcessingUnit.ExecutionFailed("Exception.", ex);
            }
        });
    }

}
