package com.linkedpipes.etl.storage.assistant.model;

import org.eclipse.rdf4j.model.Resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Store information used to assist user during pipeline desing.
 */
public class PipelineDesign {

    static public class Template {

        public final Map<Resource, Integer> followup;

        public Template() {
            followup = new HashMap<>();
        }

        public Template(PipelineInfo.Template template) {
            this.followup = Collections.unmodifiableMap(template.followup);
        }

        public void addAll(Template other) {
            for (var entry : other.followup.entrySet()) {
                int value = followup.getOrDefault(entry.getKey(), 0);
                followup.put(entry.getKey(), value + entry.getValue());
            }

        }

    }

    public final Set<String> tags;

    public final Map<Resource, Template> templates =
            new HashMap<>();

    public PipelineDesign() {
        tags = new HashSet<>();
    }

    public PipelineDesign(PipelineInfo info) {
        this.tags = Collections.unmodifiableSet(info.tags);
        info.templates.forEach((key, value) ->
                templates.put(key, new Template(value)));
    }

    public void addAll(PipelineDesign other) {
        tags.addAll(other.tags);
        other.templates.forEach((key, value) -> {
            Template templateInfo = templates.get(key);
            if (templateInfo == null) {
                templateInfo = new Template();
                templates.put(key, templateInfo);
            }
            templateInfo.addAll(value);
        });
    }

}
