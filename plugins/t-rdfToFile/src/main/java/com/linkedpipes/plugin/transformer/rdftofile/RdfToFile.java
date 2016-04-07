package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit.ExecutionFailed;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 *
 * @author Škoda Petr
 */
public final class RdfToFile implements SequentialExecution {

    private static final String FILE_ENCODE = "UTF-8";

    @DataProcessingUnit.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @DataProcessingUnit.OutputPort(id = "OutputFile")
    public WritableFilesDataUnit outputFiles;

    @DataProcessingUnit.Configuration
    public RdfToFileConfiguration configuration;

    @DataProcessingUnit.Extension
    public ProgressReport progressReport;

    private RDFFormat rdfFormat;

    @Override
    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException {
        rdfFormat = configuration.getFileFormat();
        final File outputFile = outputFiles.createFile(configuration.getFileName());
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
                // TODO We can !optionaly! find out data size here, based on configutaion.
                progressReport.start((int) connection.size(inputRdf.getGraph()));
                // Export.
                connection.export(writer, inputRdf.getGraph());
            } catch (IOException ex) {
                throw new ExecutionFailed("Can't write data.", ex);
            }
        });
        progressReport.done();
    }

}
