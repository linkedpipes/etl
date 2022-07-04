package com.linkedpipes.plugin.transformer.jsontojsonld;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JsonToJsonLd implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public JsonToJsonLdConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    private AddContextStream.Configuration streamConfiguration;

    @Override
    public void execute() throws LpException {
        prepareStreamConfiguration();
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry inputEntry : inputFiles) {
            processEntry(inputEntry);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void prepareStreamConfiguration() {
        final AddContextStream.Configuration config
                = new AddContextStream.Configuration();
        config.useFileName = configuration.isFileReference();
        config.context = configuration.getContext();
        config.dataPredicate = configuration.getDataPredicate();
        config.encoding = configuration.getEncoding();
        config.fileNamePredicate = configuration.getFilePredicate();
        config.type = configuration.getType();
        streamConfiguration = config;
    }

    private void processEntry(FilesDataUnit.Entry entry) throws LpException {
        final File outputFile = outputFiles.createFile(
                entry.getFileName() + ".jsonld");
        try {
            updateFile(entry, outputFile);
        } catch (IOException ex) {
            throw new LpException("Can't update file: {}",
                    entry.getFileName(), ex);
        }
    }

    private void updateFile(FilesDataUnit.Entry inputEntry, File outputFile)
            throws IOException {
        File inputFile = inputEntry.toFile();
        try (InputStream inputStream = new AddContextStream(streamConfiguration,
                inputEntry.getFileName(), new FileInputStream(inputFile))) {
            FileUtils.copyInputStreamToFile(inputStream, outputFile);
        }
    }

}
