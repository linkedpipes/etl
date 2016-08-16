package com.linkedpipes.plugin.transformer.filesToStatements;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import java.io.IOException;
import org.openrdf.model.IRI;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Read content of files and save it in form of literals.
 *
 * @author Škoda Petr
 */
public final class FilesToStatements implements Component.Sequential {

    private static final int BUFFER_SIZE = 64;

    @Component.OutputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "OutputRdf")
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
            throw exceptionFactory.invalidRdfProperty(
                    FilesToStatementsVocabulary.PREDICATE,
                    "Invalid predicate.", t);
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
