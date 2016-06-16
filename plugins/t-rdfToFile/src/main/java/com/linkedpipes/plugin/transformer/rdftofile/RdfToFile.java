package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.api.Component.ExecutionFailed;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import com.linkedpipes.etl.component.api.executable.SimpleExecution;
import com.linkedpipes.etl.component.api.Component;

/**
 *
 * @author Škoda Petr
 */
public final class RdfToFile implements SimpleExecution {

    private static final String FILE_ENCODE = "UTF-8";

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.OutputPort(id = "OutputFile")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public RdfToFileConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    private RDFFormat rdfFormat;

    @Override
    public void execute(Component.Context context) throws NonRecoverableException {
        rdfFormat = configuration.getFileFormat();
        final File outputFile = outputFiles.createFile(configuration.getFileName()).toFile();
        inputRdf.execute((connection) -> {
            try (FileOutputStream outStream = new FileOutputStream(outputFile);
                    OutputStreamWriter outWriter = new OutputStreamWriter(outStream, Charset.forName(FILE_ENCODE))) {
                // Based on data type utilize graph (context) renamer on not.
                RDFWriter writer = Rio.createWriter(rdfFormat, outWriter);
                if (rdfFormat.supportsContexts()) {
                    writer = new RdfWriterContextRenamer(writer,
                            connection.getValueFactory().createURI(configuration.getGraphUri()));
                }
                writer = new RdfWriterContext(writer, context, progressReport);
                progressReport.start((int) connection.size(inputRdf.getGraph()));
                connection.export(writer, inputRdf.getGraph());
                progressReport.done();
            } catch (IOException ex) {
                throw new ExecutionFailed("Can't write data.", ex);
            }
        });
    }

}
