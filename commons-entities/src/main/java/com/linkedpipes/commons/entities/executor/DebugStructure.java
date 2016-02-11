package com.linkedpipes.commons.entities.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes structure of data in the debug directory.
 *
 * @author Petr Škoda
 */
public class DebugStructure {

    public static class DataUnit {

        /**
         * List of types.
         */
        private List<String> types = new ArrayList<>(4);

        /**
         * Path to directory with debug data.
         */
        private String debugDirectory;

        /**
         * Binding.
         */
        private String binding;

        /**
         * URI of owning component in scope of the execution execution.
         */
        private String componentUri;

        public DataUnit() {

        }

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }

        public String getDebugDirectory() {
            return debugDirectory;
        }

        public void setDebugDirectory(String debugDirectory) {
            this.debugDirectory = debugDirectory;
        }

        public String getBinding() {
            return binding;
        }

        public void setBinding(String binding) {
            this.binding = binding;
        }

        public String getComponentUri() {
            return componentUri;
        }

        public void setComponentUri(String componentUri) {
            this.componentUri = componentUri;
        }

    }

    /**
     * List of data units with debugging data. The key refers to data unit name ie. uriFragment (part of URI).
     */
    private Map<String, DataUnit> dataUnits = new HashMap<>();

    /**
     * Id of an execution ie part of execution URI.
     */
    private String executionId;

    public DebugStructure() {
    }

    public Map<String, DataUnit> getDataUnits() {
        return dataUnits;
    }

    public void setDataUnits(Map<String, DataUnit> dataUnits) {
        this.dataUnits = dataUnits;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

}
