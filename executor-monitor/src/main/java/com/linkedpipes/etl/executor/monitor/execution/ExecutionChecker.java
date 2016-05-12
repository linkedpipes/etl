package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade.ExecutionMismatch;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade.OperationFailed;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class apply defensive approach ie. if an exception
 * is thrown by any operation the given parameter object should
 * stay in consistent state.
 *
 * @author Petr Škoda
 */
class ExecutionChecker {

    /**
     * Used to report that file from which should the data be loaded is
     * missing.
     */
    static class MissingFile extends Exception {

        MissingFile(String message) {
            super(message);
        }

    }

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutionChecker.class);

    private ExecutionChecker() {

    }

    /**
     * Load execution data into given execution.
     *
     * @param execution
     */
    public static void updateFromDirectory(Execution execution)
            throws OperationFailed, ExecutionMismatch {
        //
        final File definitionFile
                = new File(execution.getDirectory(), "execution.jsonld");
        if (!definitionFile.exists()) {
            // No execution directory, the execution must be queued.
            loadQueued(execution);
        } else {
            try (InputStream input = new FileInputStream(definitionFile)) {
                checkExecution(execution, input);
            } catch (IOException ex) {
                throw new OperationFailed("Can't read definition file.", ex);
            }
        }
    }

    /**
     * Load execution data into execution from the stream.
     *
     * @param execution
     * @param stream
     */
    public static void checkExecution(Execution execution,
            InputStream stream) throws OperationFailed, ExecutionMismatch {
        final Date checkStart = new Date();
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final List<Statement> output = new ArrayList<>(8);
        Resource startEvent = null;
        Resource endEvent = null;
        int componentsToExecute = 0;
        int componentsFinished = 0;

        final List<Statement> executionStatements;
        try {
            executionStatements = loadStream(stream);
        } catch (IOException ex) {
            throw new OperationFailed("Can't load statements.", ex);
        }

        // Search and get execution IRI if it's missing.
        String executionIri = null;
        final List<Resource> components = new ArrayList<>(32);
        for (Statement statement : executionStatements) {
            if (RDF.TYPE.equals(statement.getPredicate())) {
                if (statement.getObject().stringValue().equals(
                        "http://etl.linkedpipes.com/ontology/Execution")) {
                    executionIri = statement.getSubject().stringValue();
                } else if (statement.getObject().stringValue().equals(
                        "http://linkedpipes.com/ontology/Component")) {
                    components.add(statement.getSubject());
                }
            }
        }

        if (execution.getIri() == null) {
            execution.setIri(executionIri);
        } else if (!execution.getIri().equals(executionIri)) {
            throw new ExecutionMismatch("Found: " + executionIri
                    + " expected:" + execution.getIri());
        }

        Date lastChange = null;
        final IRI executionResource = valueFactory.createIRI(execution.getIri());
        final IRI graph = createGraph(valueFactory, execution.getIri());
        for (Statement statement : executionStatements) {
            // Read status.
            if (statement.getPredicate().stringValue().equals(
                    "http://etl.linkedpipes.com/ontology/status")) {
                if (statement.getSubject().equals(executionResource)) {
                    output.add(valueFactory.createStatement(
                            statement.getSubject(),
                            statement.getPredicate(),
                            statement.getObject(),
                            graph));
                    continue;
                }
                if (components.contains(statement.getSubject())) {
                    switch (statement.getObject().stringValue()) {
                        case "http://etl.linkedpipes.com/resources/status/mapped":
                            break;
                        default:
                            ++componentsToExecute;
                            break;
                    }
                }
                continue;
            }

            if (RDF.TYPE.equals(statement.getPredicate())) {
                final String value = statement.getObject().stringValue();
                switch (value) {
                    case "http://linkedpipes.com/ontology/events/ExecutionBegin":
                        startEvent = statement.getSubject();
                        break;
                    case "http://linkedpipes.com/ontology/events/ExecutionEnd":
                        endEvent = statement.getSubject();
                        break;
                    case "http://linkedpipes.com/ontology/events/ComponentEnd":
                    case "http://linkedpipes.com/ontology/events/ComponentFailed":
                        ++componentsFinished;
                        break;
                    default:
                        break;
                }
            } else if (statement.getSubject().equals(executionResource)) {
                // For the pipeline itself we store the status and
                // reference to the pipeline object.
                if (statement.getPredicate().stringValue().equals("http://etl.linkedpipes.com/ontology/pipeline")) {
                    output.add(valueFactory.createStatement(
                            statement.getSubject(),
                            statement.getPredicate(),
                            statement.getObject(),
                            graph));
                } else if (statement.getPredicate().stringValue().equals("http://etl.linkedpipes.com/ontology/lastChange")) {
                    lastChange = ((Literal) statement.getObject()).calendarValue().toGregorianCalendar().getTime();
                }
            }
        }

        // Check.
        if (lastChange != null && execution.getLastChange() != null
                && lastChange.before(execution.getLastChange())) {
            // We have newer data already loaded.
            return;
        }

        // Search for events.
        Value start = null;
        Value end = null;
        for (Statement statement : executionStatements) {
            if (statement.getSubject().equals(startEvent)) {
                if (statement.getPredicate().stringValue().equals("http://linkedpipes.com/ontology/events/created")) {
                    start = statement.getObject();
                }
            } else if (statement.getSubject().equals(endEvent)) {
                if (statement.getPredicate().stringValue().equals("http://linkedpipes.com/ontology/events/created")) {
                    end = statement.getObject();
                }
            }
        }

        // Add custom.
        output.add(valueFactory.createStatement(
                valueFactory.createIRI(execution.getIri()),
                RDF.TYPE,
                valueFactory.createIRI("http://etl.linkedpipes.com/ontology/Execution"),
                graph));

        if (execution.getDirectory() != null) {
            output.add(valueFactory.createStatement(
                    executionResource,
                    valueFactory.createIRI("http://etl.linkedpipes.com/ontology/execution/size"),
                    valueFactory.createLiteral(FileUtils.sizeOfDirectory(execution.getDirectory())),
                    graph));
        }

        output.add(valueFactory.createStatement(
                executionResource,
                valueFactory.createIRI("http://etl.linkedpipes.com/ontology/execution/componentCount"),
                valueFactory.createLiteral(components.size()),
                graph));

        output.add(valueFactory.createStatement(
                executionResource,
                valueFactory.createIRI("http://etl.linkedpipes.com/ontology/execution/componentToExecute"),
                valueFactory.createLiteral(componentsToExecute),
                graph));

        output.add(valueFactory.createStatement(
                executionResource,
                valueFactory.createIRI("http://etl.linkedpipes.com/ontology/execution/componentFinished"),
                valueFactory.createLiteral(componentsFinished),
                graph));

        if (start != null) {
            output.add(valueFactory.createStatement(
                    executionResource,
                    valueFactory.createIRI("http://etl.linkedpipes.com/ontology/execution/start"),
                    start,
                    graph));
        }

        if (end != null) {
            output.add(valueFactory.createStatement(
                    executionResource,
                    valueFactory.createIRI("http://etl.linkedpipes.com/ontology/execution/end"),
                    end,
                    graph));
        }

        // Update debug data.
        execution.setDebugData(new DebugData(executionStatements, execution));

        // Update execution status on discovered informations.
        if (execution.getStatus() == null) {
            // We are loading new execution, so we have to assign a staus.
            if (startEvent == null) {
                execution.setStatus(Execution.StatusType.QUEUED);
            } else if (endEvent == null) {
                execution.setStatus(Execution.StatusType.RUNNING);
            } else {
                // We have start and end event.
                execution.setStatus(Execution.StatusType.FINISHED);
            }
        } else // We are updating the status.
         if (endEvent == null) {
                // Here we can only change from queued to running.
                if (execution.getStatus() == Execution.StatusType.QUEUED) {
                    execution.setStatus(Execution.StatusType.RUNNING);
                }
            } else {
                execution.setStatus(Execution.StatusType.FINISHED);
            }

        if (execution.getStatus() == Execution.StatusType.RUNNING) {
            execution.setExecutionStatementsFull(executionStatements);
        } else {
            execution.setExecutionStatementsFull(null);
        }

        // For now set change time to every reload.
        execution.setLastChange(checkStart);
        execution.setLastCheck(checkStart);

        updateGenerated(execution);
        execution.setExecutionStatements(output);
    }

    /**
     * Create and save the content for deleted executions into the given
     * execution.
     *
     * @param execution
     */
    public static void setToDeleted(Execution execution) {
        final Date checkStart = new Date();
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final IRI graph = createGraph(valueFactory, execution.getIri());
        final List<Statement> output = new ArrayList<>(1);

        execution.setStatus(Execution.StatusType.DELETED);

        output.add(valueFactory.createStatement(
                valueFactory.createIRI(execution.getIri()),
                RDF.TYPE,
                valueFactory.createIRI("http://etl.linkedpipes.com/ontology/Deleted"),
                graph));

        // For now set change time to every reload.
        execution.setLastChange(checkStart);
        execution.setLastCheck(checkStart);

        execution.setExecutionStatements(output);
    }

    /**
     * Create and save content for queued execution.
     *
     * @param execution
     */
    private static void loadQueued(Execution execution) {
        if (execution.getStatus() == Execution.StatusType.QUEUED) {
            return;
        }
        final Date checkStart = new Date();
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final IRI graph = createGraph(valueFactory, execution.getIri());
        final List<Statement> output = new ArrayList<>(1);

        execution.setStatus(Execution.StatusType.QUEUED);

        output.add(valueFactory.createStatement(
                valueFactory.createIRI(execution.getIri()),
                RDF.TYPE,
                valueFactory.createIRI("http://etl.linkedpipes.com/ontology/Execution"),
                graph));

        output.add(valueFactory.createStatement(
                valueFactory.createIRI(execution.getIri()),
                valueFactory.createIRI("http://etl.linkedpipes.com/ontology/execution/size"),
                valueFactory.createLiteral(
                        FileUtils.sizeOfDirectory(execution.getDirectory())),
                graph));

        output.add(valueFactory.createStatement(
                valueFactory.createIRI(execution.getIri()),
                valueFactory.createIRI("http://etl.linkedpipes.com/ontology/status"),
                valueFactory.createIRI("http://etl.linkedpipes.com/resources/status/queued"),
                graph));

        updateGenerated(execution);

        // For now set change time to every reload.
        execution.setLastChange(checkStart);
        execution.setLastCheck(checkStart);

        execution.setExecutionStatements(output);
    }

    /**
     * Load statements from given stream.
     *
     * @param stream
     * @return
     * @throws IOException
     */
    private static List<Statement> loadStream(InputStream stream)
            throws IOException, OperationFailed {
        final RDFParser reader = Rio.createParser(RDFFormat.JSONLD,
                SimpleValueFactory.getInstance());
        final List<Statement> statements = new ArrayList<>(64);
        //
        reader.setRDFHandler(new AbstractRDFHandler() {

            @Override
            public void handleStatement(Statement statement) {
                statements.add(statement);
            }

        });
        //
        try {
            reader.parse(stream, "http://localhost/base/");
        } catch (OpenRDFException ex) {
            throw new OperationFailed("Can't read data.", ex);
        }
        return statements;
    }

    /**
     *
     * @param valueFactory
     * @param execution
     * @return Graph used to store information about execution.
     */
    private static IRI createGraph(ValueFactory valueFactory, String execution) {
        return valueFactory.createIRI(execution + "/list");
    }

    public static void updateGenerated(Execution execution) {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final IRI graph = createGraph(valueFactory, execution.getIri());
        final List<Statement> output = new ArrayList<>(1);

        final IRI status;
        switch (execution.getStatus()) {
            case DANGLING:
                status = valueFactory.createIRI("http://etl.linkedpipes.com/resources/status/dangling");
                break;
            case FINISHED:
                status = valueFactory.createIRI("http://etl.linkedpipes.com/resources/status/finished");
                break;
            case QUEUED:
                status = valueFactory.createIRI("http://etl.linkedpipes.com/resources/status/queued");
                break;
            case RUNNING:
                status = valueFactory.createIRI("http://etl.linkedpipes.com/resources/status/running");
                break;
            case UNRESPONSIVE:
                status = valueFactory.createIRI("http://etl.linkedpipes.com/resources/status/unresponsive");
                break;
            default:
                status = valueFactory.createIRI("http://etl.linkedpipes.com/resources/status/unknown");
                break;
        }
        output.add(valueFactory.createStatement(
                valueFactory.createIRI(execution.getIri()),
                valueFactory.createIRI("http://etl.linkedpipes.com/ontology/statusMonitor"),
                status, graph));

        execution.setExecutionStatementsGenerated(output);
    }

}
