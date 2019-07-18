package com.linkedpipes.etl.executor.monitor.debug;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class DebugDataFactory {

    public static DebugData create(
            Execution execution, Collection<Statement> statements) {
        Map<String, DataUnit> dataUnits =
                parseFromStatements(execution, statements);
        return new DebugData(
                execution.getId(),
                execution.getDirectory(),
                dataUnits);
    }

    private static Map<String, DataUnit> parseFromStatements(
            Execution execution, Collection<Statement> statements) {
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
            dataUnit.setOwnerExecution(execution.getId());
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

    private static boolean isDataUnitObject(Statement st) {
        return st.getPredicate().equals(RDF.TYPE)
                && st.getObject().stringValue().equals(LP_EXEC.DATA_UNIT);
    }

    private static void addStatement(Statement statement, DataUnit dataUnit) {
        switch (statement.getPredicate().stringValue()) {
            case LP_EXEC.HAS_DEBUG:
                dataUnit.setName(objectAsStr(statement));
                break;
            case LP_EXEC.HAS_DATA_PATH:
                dataUnit.setRelativeDataPath(objectAsStr(statement));
                break;
            case LP_EXEC.HAS_EXECUTION_ETL:
                dataUnit.setMappedFromExecution(objectAsStr(statement));
                break;
            default:
                break;
        }
    }

    private static String objectAsStr(Statement statement) {
        return statement.getObject().stringValue();
    }

}
