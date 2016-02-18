package com.linkedpipes.plugin.transformer.xslt;

import com.linkedpipes.etl.dataunit.system.api.SystemDataUnitException;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;
import java.io.File;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public final class Xslt implements SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Xslt.class);

    @DataProcessingUnit.InputPort(id = "FilesInput")
    public FilesDataUnit inputFiles;

    @DataProcessingUnit.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @DataProcessingUnit.Configuration
    public XsltConfiguration configuration;

    @DataProcessingUnit.Extension
    public ProgressReport progressReport;

    @Override
    public void execute(DataProcessingUnit.Context context)
            throws DataProcessingUnit.ExecutionFailed, SystemDataUnitException {
        final Processor processor = new Processor(false);
        processor.registerExtensionFunction(UUIDGenerator.getInstance());

        final XsltCompiler compiler = processor.newXsltCompiler();
        final XsltExecutable executable;
        try {
            executable = compiler.compile(new StreamSource(new StringReader(configuration.getXsltTemplate())));
        } catch (SaxonApiException ex) {
            throw new DataProcessingUnit.ExecutionFailed("Can't compile template.", ex);
        }

        for (FilesDataUnit.Entry entry : inputFiles) {
            final File inputFile = entry.getPath();
            final File outputFile = outputFiles.createFile(replaceExtension(entry.getFileName(),
                    configuration.getNewExtension()));

            if (context.canceled()) {
                throw new DataProcessingUnit.ExecutionCancelled();
            }
            // Transform.
            final XsltTransformer transformer = executable.load();
            final Serializer output = new Serializer(outputFile);
            try {
                transformer.setSource(new StreamSource(inputFile));
                transformer.setDestination(output);
                transformer.transform();
            } catch (SaxonApiException ex) {
                throw new DataProcessingUnit.ExecutionFailed("Can't transform file.", ex);
            } finally {
                // Clear document cache.
                try {
                    output.close();
                } catch (SaxonApiException ex) {
                    LOG.warn("Can't close output.", ex);
                }
                transformer.getUnderlyingController().clearDocumentPool();
                try {
                    transformer.close();
                } catch (SaxonApiException ex) {
                    LOG.warn("Can't close transformer.", ex);
                }
            }
            //
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    /**
     * If given extension is null return original file name. Else if given file has no extension (not dot in the name)
     * extension is added. Else if there is an extension (there is a dot) the string after dot is replaced
     * with given extension.
     *
     * @param fileName
     * @param extension
     * @return
     */
    private static String replaceExtension(String fileName, String extension) {
        if (extension == null) {
            return fileName;
        }
        final int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return fileName + "." + extension;
        } else {
            return fileName.substring(0, dotIndex + 1) + extension;
        }
    }

}
