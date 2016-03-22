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
 * Store debug data related to an execution.
 *
 * @author Petr Škoda
 */
public class DebugData {

    public static class DataUnit {

        private String id;

        private List<String> debugDirectories = new LinkedList<>();

        DataUnit() {
        }

        public String getId() {
            return id;
        }

        public List<String> getDebugDirectories() {
            return Collections.unmodifiableList(debugDirectories);
        }

    }

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
                    dataUnit.id = statement.getObject().stringValue();
                    break;
                case "http://etl.linkedpipes.com/ontology/debugPath":
                    dataUnit.debugDirectories.add(statement.getObject().stringValue());
                    break;
            }
        }
        // Store.
        for (DataUnit dataUnit : loadingDataUnits.values()) {
            this.dataUnits.put(dataUnit.id, dataUnit);
        }
    }

    public File getDirectory() {
        return directory;
    }

    public Map<String, DataUnit> getDataUnits() {
        return Collections.unmodifiableMap(dataUnits);
    }

}
