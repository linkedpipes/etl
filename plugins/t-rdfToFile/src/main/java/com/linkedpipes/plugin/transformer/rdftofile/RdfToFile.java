package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.util.Optional;

/**
 *
 * @author Škoda Petr
 */
public final class RdfToFile implements Component.Sequential {

    private static final String FILE_ENCODE = "UTF-8";

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.OutputPort(id = "OutputFile")
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
            throw exceptionFactory.failed("Invalid output file type: {}",
                    configuration.getFileName());
        }
        final File outputFile = outputFiles.createFile(
                configuration.getFileName()).toFile();
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
                progressReport.start((int) connection.size(inputRdf.getGraph()));
                connection.export(writer, inputRdf.getGraph());
                progressReport.done();
            } catch (IOException ex) {
                throw exceptionFactory.failed("Can't write data.", ex);
            }
        });
    }

}
