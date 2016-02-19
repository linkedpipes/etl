package com.linkedpipes.commons.entities.executor;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes structure of data in the debug directory.
 *
 * @author Petr Škoda
 */
public class DebugStructure {

    public static class DataUnit {

        /**
         * Data unit instance IRI.
         */
        private String iri;

        /**
         * List of debug directory paths. Joined content of those directories
         * is considered to be the content of data unit  debug directory.
         */
        private List<String> debugDirectories = new ArrayList<>(2);

        /**
         * Path to directory where the content of data unit is saved.
         */
        private String saveDirectory;

        /**
         * Shall be removed, used only to help Frontend.
         */
        private String componentUri;

        /**
         * Shall be removed, used only to help Frontend.
         */
        private String uriFragment;

        public DataUnit() {
        }

        public String getIri() {
            return iri;
        }

        public void setIri(String iri) {
            this.iri = iri;
        }

        public List<String> getDebugDirectories() {
            return debugDirectories;
        }

        public void setDebugDirectories(List<String> debugDirectories) {
            this.debugDirectories = debugDirectories;
        }

        public String getSaveDirectory() {
            return saveDirectory;
        }

        public void setSaveDirectory(String saveDirectory) {
            this.saveDirectory = saveDirectory;
        }

        public String getComponentUri() {
            return componentUri;
        }

        public void setComponentUri(String componentUri) {
            this.componentUri = componentUri;
        }

        public String getUriFragment() {
            return uriFragment;
        }

        public void setUriFragment(String uriFragment) {
            this.uriFragment = uriFragment;
        }

    }

    /**
     * List of data units with debugging data.
     */
    private List<DataUnit> dataUnits = new ArrayList<>();

    public DebugStructure() {
    }

    public List<DataUnit> getDataUnits() {
        return dataUnits;
    }

    public void setDataUnits(List<DataUnit> dataUnits) {
        this.dataUnits = dataUnits;
    }

}
