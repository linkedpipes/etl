package com.linkedpipes.plugin.transformer.sparql.select;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
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
import com.linkedpipes.etl.component.api.executable.SimpleExecution;
import com.linkedpipes.etl.component.api.Component;

/**
 *
 * @author Škoda Petr
 */
public final class SparqlSelect implements SimpleExecution {

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

    @Override
    public void execute(Component.Context context) throws NonRecoverableException {
        // For each graph.
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
                throw new Component.ExecutionFailed("Exception.", ex);
            }
        });
    }

}
