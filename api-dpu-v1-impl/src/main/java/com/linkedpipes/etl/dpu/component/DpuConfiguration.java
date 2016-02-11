package com.linkedpipes.etl.dpu.component;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import java.io.File;

import com.linkedpipes.utils.core.entity.boundary.EntityLoader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Škoda Petr
 */
final class DpuConfiguration implements EntityLoader.Loadable {

    public static class ConfigurationHolder implements EntityLoader.Loadable {

        private final String uri;

        private int order;

        private String configurationUri = null;

        private String configurationGraph = null;

        public ConfigurationHolder(String uri) {
            this.uri = uri;
        }

        @Override
        public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
            switch (predicate) {
                case LINKEDPIPES.CONFIGURATION.HAS_ORDER:
                    try {
                        order = Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        throw new EntityLoader.LoadingFailed(
                                LINKEDPIPES.CONFIGURATION.HAS_ORDER + " must be a integer!", ex);
                    }
                    return null;
                case LINKEDPIPES.CONFIGURATION.HAS_RESOURCE:
                    configurationUri = value;
                    return null;
                case LINKEDPIPES.CONFIGURATION.HAS_GRAPH:
                    configurationGraph = value;
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public void validate() throws EntityLoader.LoadingFailed {

        }

        public String getUri() {
            return uri;
        }

        public int getOrder() {
            return order;
        }

        public String getConfigurationUri() {
            return configurationUri;
        }

        public String getConfigurationGraph() {
            return configurationGraph;
        }

    }

    private final String resourceUri;

    private String workingDirectory;

    private final List<ConfigurationHolder> configurations = new LinkedList<>();

    public DpuConfiguration(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public File getWorkingDirectory() {
        return new File(java.net.URI.create(workingDirectory));
    }

    public List<ConfigurationHolder> getConfigurations() {
        return configurations;
    }

    @Override
    public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
        switch (predicate) {
            case LINKEDPIPES.HAS_WORKING_DIRECTORY:
                workingDirectory = value;
                return null;
            case LINKEDPIPES.HAS_CONFIGURATION:
                final ConfigurationHolder holder = new ConfigurationHolder(value);
                configurations.add(holder);
                return holder;
            default:
                return null;
        }
    }

    @Override
    public void validate() throws EntityLoader.LoadingFailed {
        if (workingDirectory == null) {
            throw new EntityLoader.LoadingFailed("Missing working directory! (uri: '" + resourceUri + "')");
        }
        // Sort configurations based on order in decreasing order - so we
        // load the configuraito with highest order first.
        Collections.sort(configurations, (ConfigurationHolder left, ConfigurationHolder right) -> {
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
