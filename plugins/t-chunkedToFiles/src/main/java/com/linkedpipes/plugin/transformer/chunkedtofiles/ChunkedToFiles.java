package com.linkedpipes.plugin.transformer.chunkedtofiles;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Optional;

public class ChunkedToFiles implements Component, SequentialExecution {

    private static final String FILE_ENCODE = "UTF-8";

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputChunked")
    public ChunkedTriples inputChunked;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public ChunkedToFilesConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private Integer outputCounter = 0;

    private RDFFormat outputFormat;

    @Override
    public void execute() throws LpException {
        initializeFromConfiguration();
        convertChunks();
    }

    private void initializeFromConfiguration() throws LpException {
        Optional<RDFFormat> format = Rio.getParserFormatForMIMEType(
                configuration.getFileType());
        if (!format.isPresent()) {
            throw exceptionFactory.failure(
                    "Can't determine output file type: {}",
                    configuration.getFileType());
        }
        outputFormat = format.get();
    }

    private void convertChunks() throws LpException {
        progressReport.start(inputChunked.size());
        for (ChunkedTriples.Chunk chunk : inputChunked) {
            convert(chunk);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void convert(ChunkedTriples.Chunk chunk) throws LpException {
        File outputFile = createOutputFile();

        try (FileOutputStream outStream = new FileOutputStream(outputFile);
             OutputStreamWriter outWriter = new OutputStreamWriter(
                     outStream, Charset.forName(FILE_ENCODE))) {
            writeChunk(chunk, outWriter);
        } catch (IOException | RuntimeException ex) {
            throw exceptionFactory.failure("Can't write data.", ex);
        }
    }

    private void writeChunk(ChunkedTriples.Chunk chunk,
            OutputStreamWriter streamWriter) throws LpException {
        RDFWriter rdfWriter = createWriter(streamWriter);
        rdfWriter.startRDF();
        chunk.toCollection().forEach((st) -> rdfWriter.handleStatement(st));
        rdfWriter.endRDF();
    }

    private RDFWriter createWriter(OutputStreamWriter streamWriter) {
        if (outputFormat.supportsContexts()) {
            return Rio.createWriter(outputFormat, streamWriter);
        } else {
            RDFWriter writer = Rio.createWriter(outputFormat, streamWriter);
            writer = new ChangeContext(writer, getOutputGraph());
            return writer;
        }
    }

    private IRI getOutputGraph() {
        String graph = configuration.getGraphUri();
        if (graph == null || graph.isEmpty()) {
            return null;
        }
        return SimpleValueFactory.getInstance().createIRI(graph);
    }

    private File createOutputFile() throws LpException {
        String fileName = ++outputCounter + "." +
                outputFormat.getDefaultFileExtension();
        return outputFiles.createFile(fileName);
    }

}
