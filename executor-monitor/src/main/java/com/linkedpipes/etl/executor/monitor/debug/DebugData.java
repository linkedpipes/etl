package com.linkedpipes.etl.executor.monitor.debug;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Store debugName data related to an executionId.
 *
 * @author Petr Škoda
 */
public class DebugData {

    public static class DataUnit {

        /**
         * Name of this data unit used for debug.
         */
        private String debugName;

        /**
         * Relative path to data directory.
         */
        private String dataPath;

        /**
         * If not null then this data unit is mapped from another
         * executionId.
         */
        private String execution;

        /**
         * List of debugging directories.
         */
        private List<File> debugDirectories = Collections.EMPTY_LIST;

        private boolean loaded = false;

        DataUnit() {
        }

        public String getDebugName() {
            return debugName;
        }

        public DataUnit(String debug) {
            this.debugName = debug;
        }

        public String getDataPath() {
            return dataPath;
        }

        public String getExecutionId() {
            if (execution == null) {
                return null;
            } else {
                return execution.substring(
                        execution.indexOf("executions/") + 11);
            }
        }

        public List<File> getDebugDirectories() {
            return Collections.unmodifiableList(debugDirectories);
        }

        /**
         * If {@link #debugDirectories} are not set then load them.
         * Given execution must match the {@link #execution}.
         *
         * @param executionDirectory
         */
        public void loadDebugDirectories(File executionDirectory) {
            if (loaded) {
                // Already loaded.
                return;
            }
            final ObjectMapper mapper = new ObjectMapper();
            final JavaType type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class);
            //
            final File inputFile = new File(executionDirectory,
                    dataPath + "/debug.json");
            final List<String> relativePaths;
            try {
                relativePaths = mapper.readValue(inputFile, type);
                this.debugDirectories = new ArrayList<>(relativePaths.size());
                for (String item : relativePaths) {
                    this.debugDirectories.add(new File(executionDirectory,
                            dataPath + File.separator + item));
                }
                // If we fail we don't mark the data as loaded,
                // thus next time we will try to load again.
                // The file may not be ready in time of call of this object,
                // but could be created later.
                loaded = true;
            } catch (IOException ex) {
                this.debugDirectories = Collections.EMPTY_LIST;
                LOG.error("Can't read data unit debug file from: {}",
                        inputFile, ex);
            }
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(DebugData.class);

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
                    dataUnit.debugName = statement.getObject().stringValue();
                    break;
                case "http://etl.linkedpipes.com/ontology/dataPath":
                    dataUnit.dataPath = statement.getObject().stringValue();
                    break;
                case "http://etl.linkedpipes.com/ontology/execution":
                    dataUnit.execution = statement.getObject().stringValue();
                    break;
            }
        }
        // Store.
        for (DataUnit dataUnit : loadingDataUnits.values()) {
            this.dataUnits.put(dataUnit.debugName, dataUnit);
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
