package com.linkedpipes.etl.executor.monitor.debug;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;

/**
 * Store debug data related to an executionId.
 *
 * @author Petr Škoda
 */
public class DebugData {

    public static class DataUnit {

        /**
         * Debug name of this data unit.
         */
        private String debug;

        private List<String> debugDirectories = new LinkedList<>();

        /**
         * If not null then this data unit is mapped from another
         * executionId.
         */
        private String execution;

        DataUnit() {
        }

        public String getDebug() {
            return debug;
        }

        public List<String> getDebugDirectories() {
            return Collections.unmodifiableList(debugDirectories);
        }

        public DataUnit(String debug) {
            this.debug = debug;
        }

        public String getExecutionId() {
            if (execution == null) {
                return null;
            } else {
                return execution.substring(
                        execution.indexOf("executions/") + 11);
            }
        }

    }

    /**
     * Execution IRI.
     */
    private final String execution;

    /**
     * Execution working directory.
     */
    private final File directory;

    private final Map<String, DataUnit> dataUnits = new HashMap<>();

    /**
     * Create new debug data object.
     *
     * @param executionStatements
     * @param execution
     */
    public DebugData(List<Statement> executionStatements, Execution execution) {
        this.execution = execution.getIri();
        this.directory = execution.getDirectory();
        //
        final Map<Resource, DataUnit> loadingDataUnits = new HashMap<>();
        // Scan for data units.
        for (Statement statement : executionStatements) {
            if (statement.getPredicate().equals(RDF.TYPE)) {
                if (statement.getObject().stringValue().equals(
                        "http://etl.linkedpipes.com/ontology/DataUnit")) {
                    loadingDataUnits.put(statement.getSubject(), new DataUnit());
                }
            }
        }
        // Load content of data units.
        for (Statement statement : executionStatements) {
            final DataUnit dataUnit = loadingDataUnits.get(
                    statement.getSubject());
            if (dataUnit == null) {
                continue;
            }
            switch (statement.getPredicate().stringValue()) {
                case "http://etl.linkedpipes.com/ontology/debug":
                    dataUnit.debug = statement.getObject().stringValue();
                    break;
                case "http://etl.linkedpipes.com/ontology/debugPath":
                    dataUnit.debugDirectories.add(statement.getObject().stringValue());
                    break;
                case "http://etl.linkedpipes.com/ontology/execution":
                    dataUnit.execution = statement.getObject().stringValue();
                    break;
            }
        }
        // Store.
        for (DataUnit dataUnit : loadingDataUnits.values()) {
            this.dataUnits.put(dataUnit.debug, dataUnit);
        }
    }

    public String getExecution() {
        return execution;
    }

    public File getDirectory() {
        return directory;
    }

    public Map<String, DataUnit> getDataUnits() {
        return Collections.unmodifiableMap(dataUnits);
    }

}
