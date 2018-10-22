package com.linkedpipes.etl.dataunit.core;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.api.v1.vocabulary.RDF;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Base configuration entity for core DataUnit instance.
 */
public class DataUnitConfiguration implements Loadable {

    private final String resource;

    private String binding;

    private final List<String> types = new LinkedList<>();

    private String group;

    private String workingDirectory;

    public DataUnitConfiguration(String resource) {
        this.resource = resource;
    }

    public DataUnitConfiguration(
            String resource, String binding, String group,
            String workingDirectory) {
        this.resource = resource;
        this.binding = binding;
        this.group = group;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public Loadable load(String predicate, RdfValue object) {
        switch (predicate) {
            case RDF.TYPE:
                this.types.add(object.asString());
                break;
            case LP_PIPELINE.HAS_BINDING:
                this.binding = object.asString();
                break;
            case LP_EXEC.HAS_DATA_UNIT_GROUP:
                this.group = object.asString();
                break;
            case LP_EXEC.HAS_WORKING_DIRECTORY:
                this.workingDirectory = object.asString();
                break;
        }
        return null;
    }

    public String getResource() {
        return this.resource;
    }

    public String getBinding() {
        return this.binding;
    }

    public List<String> getTypes() {
        return Collections.unmodifiableList(this.types);
    }

    public String getGroup() {
        return this.group;
    }

    public File getWorkingDirectory() {
        return new File(URI.create(this.workingDirectory));
    }

}
