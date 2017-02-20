package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Optional;

public final class RdfToFile implements Component, SequentialExecution {

    private static final String FILE_ENCODE = "UTF-8";

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.OutputPort(iri = "OutputFile")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public RdfToFileConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        Optional<RDFFormat> rdfFormat = Rio.getParserFormatForMIMEType(
                configuration.getFileType());
        if (!rdfFormat.isPresent()) {
            throw exceptionFactory.failure("Invalid output file type: {}",
                    configuration.getFileName());
        }
        final File outputFile = outputFiles.createFile(
                configuration.getFileName());
        inputRdf.execute((connection) -> {
            try (FileOutputStream outStream = new FileOutputStream(outputFile);
                 OutputStreamWriter outWriter = new OutputStreamWriter(
                         outStream, Charset.forName(FILE_ENCODE))) {
                // Based on data type utilize graph (context) renamer on not.
                RDFWriter writer = Rio.createWriter(rdfFormat.get(), outWriter);
                if (rdfFormat.get().supportsContexts()) {
                    writer = new RdfWriterContextRenamer(writer,
                            connection.getValueFactory().createIRI(
                                    configuration.getGraphUri()));
                }
                writer = new RdfWriterContext(writer, progressReport);
                progressReport
                        .start((int) connection.size(inputRdf.getReadGraph()));
                connection.export(writer, inputRdf.getReadGraph());
                progressReport.done();
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't write data.", ex);
            }
        });
    }

}
