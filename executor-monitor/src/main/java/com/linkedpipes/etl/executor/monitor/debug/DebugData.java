package com.linkedpipes.etl.executor.monitor.debug;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Store debugName data related to an executionId.
 */
public class DebugData {

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
     * Create new debugName data object.
     *
     * @param executionStatements
     * @param execution
     */
    public DebugData(Collection<Statement> executionStatements, Execution execution) {
        this.execution = execution.getIri();
        this.directory = execution.getDirectory();
        //
        Map<Resource, DataUnit> loadingDataUnits = new HashMap<>();
        // Scan for data units.
        for (Statement statement : executionStatements) {
            if (statement.getPredicate().equals(RDF.TYPE)) {
                if (statement.getObject().stringValue().equals(
                        "http://etl.linkedpipes.com/ontology/DataUnit")) {
                    loadingDataUnits
                            .put(statement.getSubject(), new DataUnit());
                }
            }
        }
        // Load content of data units.
        for (Statement statement : executionStatements) {
            DataUnit dataUnit = loadingDataUnits.get(statement.getSubject());
            if (dataUnit == null) {
                continue;
            }
            switch (statement.getPredicate().stringValue()) {
                case "http://etl.linkedpipes.com/ontology/debug":
                    dataUnit.setDebugName(statement.getObject().stringValue());
                    break;
                case "http://etl.linkedpipes.com/ontology/dataPath":
                    dataUnit.setDataPath(statement.getObject().stringValue());
                    break;
                case "http://etl.linkedpipes.com/ontology/execution":
                    dataUnit.setExecution(statement.getObject().stringValue());
                    break;
            }
        }
        // Store.
        for (DataUnit dataUnit : loadingDataUnits.values()) {
            this.dataUnits.put(dataUnit.getDebugName(), dataUnit);
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
