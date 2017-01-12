package com.linkedpipes.etl.dataunit.core;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Base configuration entity for core DataUnit instance.
 */
public class BaseConfiguration implements RdfLoader.Loadable<String> {

    private final String iri;

    private final String graph;

    private String binding;

    private final List<String> sources = new LinkedList<>();

    private final List<String> types = new LinkedList<>();

    protected BaseConfiguration(String iri, String graph) {
        this.iri = iri;
        this.graph = graph;
    }

    @Override
    public RdfLoader.Loadable load(String predicate, String object)
            throws RdfUtilsException {
        switch (predicate) {
            case RDF.TYPE:
                types.add(object);
                break;
            case LP_PIPELINE.HAS_BINDING:
                binding = object;
                break;
        }
        return null;
    }

    /**
     * Validate entity after it has been loaded.
     */
    public void validate() throws LpException {
        if (binding == null) {
            throw new LpException("Missing 'binding' property for: {}", iri);
        }
    }

    /**
     * Load additional information, must be called after the entity is loaded.
     *
     * @param source
     */
    public void loadSources(RdfSource source) throws RdfUtilsException {
        final List<Map<String, String>> sources =
                RdfUtils.sparqlSelect(source, sourceQuery(iri, binding, graph));
        for (Map<String, String> binding : sources) {
            this.sources.add(binding.get("dataUnit"));
        }
    }

    /**
     * @param iri
     * @param binding
     * @param graph
     * @return Query for sources of given data unit.
     */
    private static String sourceQuery(
            String iri, String binding, String graph) {
        return "SELECT ?dataUnit FROM <" + graph + "> WHERE {\n" +
                " ?targetComponent a <" + LP_PIPELINE.COMPONENT + "> ;\n" +
                "  <" + LP_PIPELINE.HAS_DATA_UNIT + "> <" + iri + "> .\n" +
                "\n" +
                " ?connection a <" + LP_PIPELINE.CONNECTION + "> ;\n" +
                "  <" + LP_PIPELINE.HAS_SOURCE_BINDING +
                "> ?sourceBinding ;\n" +
                "  <" + LP_PIPELINE.HAS_SOURCE_COMPONENT +
                "> ?sourceComponent ;\n" +
                "  <" + LP_PIPELINE.HAS_TARGET_BINDING +
                "> \"" + binding + "\" ;\n" +
                "  <" + LP_PIPELINE.HAS_TARGET_COMPONENT +
                "> ?targetComponent .\n" +
                "\n" +
                " ?sourceComponent a <" + LP_PIPELINE.COMPONENT + "> ;\n" +
                "  <" + LP_PIPELINE.HAS_DATA_UNIT + "> ?dataUnit .\n" +
                "\n" +
                " ?dataUnit a <" + LP_PIPELINE.OUTPUT + "> ;\n" +
                "  <" + LP_PIPELINE.HAS_BINDING + "> ?sourceBinding .\n" +
                "\n" +
                "}";
    }

    public String getBinding() {
        return binding;
    }

    public List<String> getSources() {
        return Collections.unmodifiableList(sources);
    }

    public List<String> getTypes() {
        return Collections.unmodifiableList(types);
    }

}
