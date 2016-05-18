package com.linkedpipes.plugin.transformer.filesToStatements;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dpu.api.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.IOException;
import org.openrdf.model.IRI;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.dpu.api.Component;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 *
 * @author Škoda Petr
 */
public final class FilesToStatements implements SimpleExecution {

    @Component.OutputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "OutputRdf")
    public WritableGraphListDataUnit outputRdf;

    @Component.Configuration
    public FilesToStatementsConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute(Component.Context context) throws NonRecoverableException {
        final List<Statement> statements = new ArrayList<>(64);
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final IRI predicate = valueFactory.createIRI(
                configuration.getPredicate());
        //
        progressReport.start(inputFiles.size() + 1);
        for (FilesDataUnit.Entry file : inputFiles) {
            // Read file content.
            final String content;
            try {
                content = FileUtils.readFileToString(file.toFile());
            } catch (IOException ex) {
                throw new ExecutionFailed("Can't read file: {}", file, ex);
            }
            // Add to output.
            final IRI outputGraph = outputRdf.createGraph();
            statements.add(valueFactory.createStatement(
                    valueFactory.createBNode(),
                    predicate,
                    valueFactory.createLiteral(content),
                    outputGraph));
            // Add to the repository.
            if (statements.size() > 63) {
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
    private void addStatements(List<Statement> statements)
            throws SesameDataUnit.RepositoryActionFailed {
        outputRdf.execute((connection) -> {
            connection.add(statements);
        });
        statements.clear();
    }

}
