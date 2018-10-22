package com.linkedpipes.etl.executor.monitor.debug;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class DebugData {

    private final String execution;

    private final File executionDirectory;

    private final Map<String, DataUnit> dataUnits;

    public DebugData(Collection<Statement> statements, Execution execution) {
        this.execution = execution.getIri();
        this.executionDirectory = execution.getDirectory();
        this.dataUnits = parseFromStatements(statements);
    }

    private Map<String, DataUnit> parseFromStatements(
            Collection<Statement> statements) {
        Map<Resource, DataUnit> newDataUnits = statements.stream()
                .filter((st) -> isDataUnitObject(st))
                .collect(Collectors.toMap(
                        (st) -> st.getSubject(),
                        (st) -> new DataUnit()));
        // Load content of data units.
        for (Statement statement : statements) {
            DataUnit dataUnit = newDataUnits.get(statement.getSubject());
            if (dataUnit == null) {
                continue;
            }
            addStatement(statement, dataUnit);
        }
        // Some may not be loaded, so there is just a reference with no
        // additional info.
        return newDataUnits.entrySet().stream()
                .filter((entry) -> entry.getValue().getName() != null)
                .collect(Collectors.toMap(
                        (entry) -> entry.getValue().getName(),
                        (entry) -> entry.getValue()));
    }

    private boolean isDataUnitObject(Statement st) {
        return st.getPredicate().equals(RDF.TYPE) &&
                st.getObject().stringValue().equals(LP_EXEC.DATA_UNIT);
    }

    private void addStatement(Statement statement, DataUnit dataUnit) {
        switch (statement.getPredicate().stringValue()) {
            case LP_EXEC.HAS_DEBUG:
                dataUnit.setName(
                        statement.getObject().stringValue());
                break;
            case LP_EXEC.HAS_DATA_PATH:
                dataUnit.setRelativeDataPath(
                        statement.getObject().stringValue());
                break;
            case LP_EXEC.HAS_EXECUTION_ETL:
                dataUnit.setMappedFromExecution(
                        statement.getObject().stringValue());
                break;
        }
    }

    public String getExecution() {
        return execution;
    }

    public File getExecutionDirectory() {
        return executionDirectory;
    }

    public Map<String, DataUnit> getDataUnits() {
        return Collections.unmodifiableMap(dataUnits);
    }

}
