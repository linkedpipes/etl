package com.linkedpipes.plugin.transformer.rdfdifference;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryResult;

import java.util.*;

public final class RdfDifference implements Component, SequentialExecution {

    private static final int BUFFER_SIZE = 10000;

    @Component.InputPort(iri = "DataRdf")
    public SingleGraphDataUnit dataRdf;

    @Component.InputPort(iri = "ToRemoveRdf")
    public SingleGraphDataUnit toRemoveRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    private List<Statement> buffer = new ArrayList<>(BUFFER_SIZE + 1);

    private Map<Resource, Map<IRI, Set<Value>>> toRemoveIndex;

    @Override
    public void execute() throws LpException {
        buildToRemoveIndex();
        dataRdf.execute((connection) -> {
            RepositoryResult<Statement> result = connection.getStatements(
                    null, null, null, dataRdf.getReadGraph());
            addDifference(result);
        });
    }

    private void buildToRemoveIndex() throws LpException {
        toRemoveIndex = new HashMap<>();
        toRemoveRdf.execute((connection) -> {
            RepositoryResult<Statement> result = connection.getStatements(
                    null, null, null, toRemoveRdf.getReadGraph());
            while (result.hasNext()) {
                addToRemoveIndex(result.next());
            }
        });
    }

    private void addToRemoveIndex(Statement statement) {
        Map<IRI, Set<Value>> subjectMap = toRemoveIndex.get(
                statement.getSubject());
        if (subjectMap == null) {
            subjectMap = new HashMap<>();
            toRemoveIndex.put(statement.getSubject(), subjectMap);
        }
        Set<Value> predicateMap = subjectMap.get(statement.getPredicate());
        if (predicateMap == null) {
            predicateMap = new HashSet<>();
            subjectMap.put(statement.getPredicate(), predicateMap);
        }
        predicateMap.add(statement.getObject());
    }

    private void addDifference(RepositoryResult<Statement> result)
            throws LpException {
        while (result.hasNext()) {
            Statement statement = result.next();
            if (shouldRemoveStatement(statement)) {
                continue;
            }
            buffer.add(statement);
            if (buffer.size() >= BUFFER_SIZE) {
                flushBuffer();
            }
        }
        flushBuffer();
    }

    private boolean shouldRemoveStatement(Statement statement) {
        Map<IRI, Set<Value>> subjectMap = toRemoveIndex.get(
                statement.getSubject());
        if (subjectMap == null) {
            return false;
        }
        Set<Value> predicateMap = subjectMap.get(statement.getPredicate());
        if (predicateMap == null) {
            return false;
        }
        return predicateMap.contains(statement.getObject());
    }

    private void flushBuffer() throws LpException {
        outputRdf.execute((connection) -> {
            connection.add(buffer, outputRdf.getWriteGraph());
            buffer.clear();
        });
    }

}
