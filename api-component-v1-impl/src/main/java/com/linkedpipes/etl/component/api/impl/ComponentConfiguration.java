package com.linkedpipes.etl.component.api.impl;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.PojoLoader;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describes information stored in RDF that are used for execution
 * of a component.
 *
 * This does not represent the configuration loaded into component.
 */
final class ComponentConfiguration implements PojoLoader.Loadable {

    /**
     * Represent a resource that holds the configuration.
     */
    static class Configuration implements PojoLoader.Loadable {

        private final String iri;

        /**
         * Order in which the configuration should be loaded. The value
         * determines priority of configuration entity loading.
         */
        private int order;

        /**
         * URI of the configuration resource.
         */
        private String configurationIri = null;

        /**
         * Graph with the configuration.
         */
        private String configurationGraph = null;

        Configuration(String iri) {
            this.iri = iri;
        }

        @Override
        public PojoLoader.Loadable load(String predicate, String value)
                throws RdfException {
            switch (predicate) {
                case LINKEDPIPES.CONFIGURATION.HAS_ORDER:
                    try {
                        order = Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        throw RdfException.invalidProperty(iri,
                                LINKEDPIPES.CONFIGURATION.HAS_ORDER,
                                "Must be a string.");
                    }
                    return null;
                case LINKEDPIPES.CONFIGURATION.HAS_RESOURCE:
                    configurationIri = value;
                    return null;
                case LINKEDPIPES.CONFIGURATION.HAS_GRAPH:
                    configurationGraph = value;
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public void validate() {

        }

        public String getConfigurationIri() {
            return configurationIri;
        }

        public String getConfigurationGraph() {
            return configurationGraph;
        }

    }

    /**
     * URI of DPU resource.
     */
    private final String resourceIri;

    /**
     * DPU working directory.
     */
    private String workingDirectory;

    /**
     * Store referenced configurations.
     */
    private final List<Configuration> configurations = new LinkedList<>();

    ComponentConfiguration(String resourceIri) {
        this.resourceIri = resourceIri;
    }

    public String getResourceIri() {
        return resourceIri;
    }

    public File getWorkingDirectory() {
        return new File(java.net.URI.create(workingDirectory));
    }

    /**
     * Ordered list of configurations as they should be loaded.
     *
     * @return
     */
    public List<Configuration> getConfigurations() {
        return configurations;
    }

    @Override
    public PojoLoader.Loadable load(String predicate, String value)
            throws RdfException {
        switch (predicate) {
            case LINKEDPIPES.HAS_WORKING_DIRECTORY:
                workingDirectory = value;
                return null;
            case LINKEDPIPES.HAS_CONFIGURATION:
                final Configuration holder = new Configuration(value);
                configurations.add(holder);
                return holder;
            default:
                return null;
        }
    }

    @Override
    public void validate() throws RdfException {
        if (workingDirectory == null) {
            throw RdfException.missingProperty(resourceIri,
                    LINKEDPIPES.HAS_WORKING_DIRECTORY);
        }
        // Sort configurations based on order in decreasing order - so we
        // load the configuration with highest order first.
        Collections.sort(configurations,
                (Configuration left, Configuration right) -> {
                    if (left.order < right.order) {
                        return 1;
                    } else if (left.order > right.order) {
                        return -1;
                    } else {
                        return 0;
                    }
                });
    }

}
