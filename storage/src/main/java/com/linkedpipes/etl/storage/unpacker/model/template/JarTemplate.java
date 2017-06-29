package com.linkedpipes.etl.storage.unpacker.model.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.LinkedList;
import java.util.List;

public class JarTemplate extends Template {

    public static final String TYPE = LP_PIPELINE.JAS_TEMPLATE;

    private List<String> types = new LinkedList<>();

    private String jar;

    private List<String> requirements = new LinkedList<>();

    private List<TemplatePort> ports = new LinkedList<>();

    private boolean supportControl = false;

    @Override
    public Loadable load(String predicate, RdfValue value)
            throws RdfUtilsException {
        switch (predicate) {
            case RDF.TYPE:
                types.add(value.asString());
                return null;
            case LP_PIPELINE.HAS_JAR_URL:
                jar = value.asString();
                return null;
            case LP_PIPELINE.HAS_REQUIREMENT:
                requirements.add(value.asString());
                return null;
            case LP_PIPELINE.HAS_DATA_UNIT:
                TemplatePort port = new TemplatePort();
                ports.add(port);
                return port;
            case LP_PIPELINE.HAS_SUPPORT_CONTROLS:
                supportControl = value.asBoolean();
                return null;
            default:
                return super.load(predicate, value);
        }
    }

    public List<String> getTypes() {
        return types;
    }

    public String getJar() {
        return jar;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public List<TemplatePort> getPorts() {
        return ports;
    }

    @Override
    public String getConfigGraph() {
        return iri + "/configuration";
    }

    public boolean isSupportControl() {
        return supportControl;
    }

}
