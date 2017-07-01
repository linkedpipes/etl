package com.linkedpipes.plugin.transformer.rdftowrappedjsonldchunked;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.io.*;
import java.util.Collection;

public class RdfToWrappedJsonLdChunked
        implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
    public ChunkedTriples inputRdf;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public RdfToWrappedJsonLdChunkedConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    private Integer fileCounter = 0;

    private TemplateWriter templateWriter;

    @Override
    public void execute() throws LpException {
        initializeTemplate();
        progressReport.start(inputRdf.size());
        for (ChunkedTriples.Chunk chunk : inputRdf) {
            File outputFile = createOutputFile();
            try {
                transformFile(chunk.toCollection(), outputFile);
            } catch (LpException ex) {
                throw exceptionFactory.failure("Can't transform chunk", ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void initializeTemplate() {
        this.templateWriter = new TemplateWriter();
        this.templateWriter.createTemplate(configuration.getTemplate());
    }

    private File createOutputFile() throws LpException {
        return outputFiles.createFile(++fileCounter + ".json");
    }

    private void transformFile(Collection<Statement> statements, File target)
            throws LpException {
        String id = getId(statements);
        templateWriter.setId(id);
        templateWriter.setStatements(statements);
        try (OutputStreamWriter writer = new FileWriter(target)) {
            templateWriter.writeToWriter(writer);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Failed to create file: {}",
                    target, ex);
        }
    }

    private String getId(Collection<Statement> statements) {
        String type = configuration.getMainResourceType();
        for (Statement statement : statements) {
            if (RDF.TYPE.equals(statement.getPredicate()) ||
                    statement.getObject().stringValue().equals(type)) {
                return statement.getSubject().stringValue();
            }
        }
        return null;
    }

}
