package com.linkedpipes.etl.storage.assistant.model;

import org.eclipse.rdf4j.model.Resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Holds information about a pipeline.
 */
public class PipelineInfo {

    static public class Template {

        public final Resource resource;

        public final Map<Resource, Integer> followup = new HashMap<>();

        public Template(Resource resource) {
            this.resource = resource;
        }

    }

    /**
     * Pipeline resource.
     */
    public final Resource resource;

    /**
     * Pipeline label.
     */
    public final String label;

    /**
     * Pipeline tags.
     */
    public final HashSet<String> tags = new HashSet<>();

    /**
     * Information about templates.
     */
    public final Map<Resource, Template> templates = new HashMap<>();

    public PipelineInfo(Resource resource, String label) {
        this.resource = resource;
        this.label = label;
    }

}
