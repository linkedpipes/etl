package com.linkedpipes.commons.entities.executor;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to store labels for entities.
 *
 * @author Škoda Petr
 */
public class Labels {

    public static class Resource {

        private Map<String, String> labels;

        public Resource() {
            labels = new HashMap<>();
        }

        public Resource(Map<String, String> labels) {
            this.labels = labels;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }

        public String getLabel(String resource, String language) {
            return labels.getOrDefault(language, labels.getOrDefault("en", labels.getOrDefault("", resource)));
        }

    }

    private Map<String, Resource> resources = new HashMap<>();

    public Labels() {
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

    public void setResources(Map<String, Resource> resources) {
        this.resources = resources;
    }

    public void addLabel(String resource, String language, String label) {
        final Resource resourceObject;
        if (resources.containsKey(resource)) {
            resourceObject = resources.get(resource);
        } else {
            resourceObject = new Resource();
            resources.put(resource, resourceObject);
        }
        //
        resourceObject.labels.put(language, label);
    }

    /**
     * If resource is not presented return resource, else call {@link Resource#getLabels()} on given resource and
     * return the result.
     *
     * @param resource
     * @param language
     * @return Label for given resource.
     */
    public String getLabel(String resource, String language) {
        if (!resources.containsKey(resource)) {
            return resource;
        }
        return resources.get(resource).getLabel(resource, language);
    }

    /**
     * If resource is not presented create and return new labels, that contains only one record:
     * "": resource
     *
     * @param resource
     * @return Labels for given resource.
     */
    public Labels.Resource getLabels(String resource) {
        if (resources.containsKey(resource)) {
            return resources.get(resource);
        } else {
            final Map<String, String> newLabels = new HashMap<>(1);
            newLabels.put(resource, resource);
            return new Resource();
        }
    }

}
