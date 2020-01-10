package com.linkedpipes.plugin.transformer.jsonldformat;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

import java.io.*;

public class JsonLdFormat implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public JsonLdFormatConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    private Object context;

    @Override
    public void execute() throws LpException {
        initializeContext();
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            File inputFIle = entry.toFile();
            File outputFile = outputFiles.createFile(entry.getFileName());
            try {
                transformFile(inputFIle, outputFile);
            } catch (LpException ex) {
                throw exceptionFactory.failure(
                        "Can't transform: {}", entry.getFileName(), ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void initializeContext() throws LpException {
        if (JsonLdFormatVocabulary.EXPANDED.equals(configuration.getFormat())) {
            // Expanded mode does not use context.
            return;
        }
        try {
            context = JsonUtils.fromString(configuration.getContext());
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't prepare context.", ex);
        }
    }

    private void transformFile(File source, File target) throws LpException {
        Object inputObject = readJsonLdFile(source);
        Object outputObject = transformJsonLd(inputObject);
        writeJsonLdFile(target, outputObject);
    }

    private Object readJsonLdFile(File file) throws LpException {
        try (InputStream input = new FileInputStream(file)) {
            return JsonUtils.fromInputStream(input);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't open input file: {}",
                    file, ex);
        }
    }

    private Object transformJsonLd(Object object) throws LpException {
        JsonLdOptions options = new JsonLdOptions();
        try {
            return formatJsonLd(object, options);
        } catch (JsonLdError ex) {
            throw exceptionFactory.failure("Can't transform object.", ex);
        }
    }

    private Object formatJsonLd(
            Object object, JsonLdOptions options)
            throws LpException, JsonLdError {
        switch (configuration.getFormat()) {
            case JsonLdFormatVocabulary.COMPACT:
                return JsonLdProcessor.compact(object, context, options);
            case JsonLdFormatVocabulary.FLAT:
                return JsonLdProcessor.flatten(object, context, options);
            case JsonLdFormatVocabulary.EXPANDED:
                return JsonLdProcessor.expand(object, options);
            case JsonLdFormatVocabulary.FRAME:
                return JsonLdProcessor.frame(object, getFrameJson(), options);
            default:
                throw exceptionFactory.failure("Invalid format type: '{}'",
                        configuration.getFormat());
        }
    }

    private Object getFrameJson() throws LpException {
        try {
            return JsonUtils.fromString(configuration.getFrame());
        } catch(IOException ex) {
            throw exceptionFactory.failure("Can't parse frame.", ex);
        }
    }

    private void writeJsonLdFile(File file, Object jsonLdObject)
            throws LpException {
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            JsonUtils.write(writer, jsonLdObject);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't write file: {}", file, ex);
        }
    }

}
