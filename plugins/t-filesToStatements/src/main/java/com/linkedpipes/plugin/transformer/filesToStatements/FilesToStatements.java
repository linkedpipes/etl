package com.linkedpipes.plugin.transformer.filesToStatements;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FilesToStatements implements Component, SequentialExecution {

    private static final int BUFFER_SIZE = 64;

    @Component.OutputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputRdf")
    public WritableGraphListDataUnit outputRdf;

    @Component.Configuration
    public FilesToStatementsConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        final List<Statement> statements = new ArrayList<>(BUFFER_SIZE + 1);
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final IRI predicate;
        try {
            predicate = valueFactory.createIRI(
                    configuration.getPredicate());
        } catch (Throwable t) {
            throw exceptionFactory.failure("Invalid predicate: {}",
                    FilesToStatementsVocabulary.PREDICATE, t);
        }
        //
        progressReport.start(inputFiles.size() + 1);
        for (FilesDataUnit.Entry file : inputFiles) {
            // Read file content.
            final String content;
            try {
                content = FileUtils.readFileToString(file.toFile());
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't read file: {}", file, ex);
            }
            // Add to output.
            final IRI outputGraph = outputRdf.createGraph();
            statements.add(valueFactory.createStatement(
                    valueFactory.createBNode(),
                    predicate,
                    valueFactory.createLiteral(content),
                    outputGraph));
            // Add to the repository.
            if (statements.size() >= BUFFER_SIZE) {
                addStatements(statements);
            }
            progressReport.entryProcessed();
        }
        // Add to the repository.
        addStatements(statements);
        progressReport.done();
    }

    /**
     * Add statements to output and clear given list.
     */
    private void addStatements(List<Statement> statements) throws LpException {
        outputRdf.execute((connection) -> {
            connection.add(statements);
        });
        statements.clear();
    }

}
