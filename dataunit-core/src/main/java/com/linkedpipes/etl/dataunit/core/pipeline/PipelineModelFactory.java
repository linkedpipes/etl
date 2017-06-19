package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.InvalidNumberOfResults;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PipelineModelFactory {

    private String pipeline;

    private String graph;

    private RdfSource source;

    public PipelineModel createModel(RdfSource source,
            String pipeline, String graph) throws RdfUtilsException {
        this.pipeline = pipeline;
        this.graph = graph;
        this.source = source;

        Map<String, Port> ports = loadPort();
        ExecutionProfile profile = loadProfileOrDefault();
        preparePortGroups(ports, profile);
        return new PipelineModel(ports);
    }

    private Map<String, Port> loadPort() throws RdfUtilsException {
        List<String> iris = getPortIris();
        Map<String, Port> ports = new HashMap<>();
        for (String iri : iris) {
            ports.put(iri, loadPort(iri));
        }
        return ports;
    }

    private List<String> getPortIris() throws RdfUtilsException {
        return RdfUtils.sparqlSelect(source, getPortsQuery()).stream()
                .map(binding -> binding.get("port"))
                .collect(Collectors.toList());
    }

    private String getPortsQuery() {
        return "SELECT ?port FROM <" + graph + "> WHERE { \n" +
                " <" + pipeline + "> <" + LP_PIPELINE.HAS_COMPONENT + ">" +
                " ?component . \n" +
                " ?component <" + LP_PIPELINE.HAS_DATA_UNIT + "> ?port .\n" +
                "}";
    }

    private Port loadPort(String iri) throws RdfUtilsException {
        List<String> types = loadTypes(iri);
        List<String> sources = loadSources(iri);
        return new Port(iri, types, sources);
    }

    private List<String> loadTypes(String iri) throws RdfUtilsException {
        final List<String> types = new ArrayList<>(4);
        source.triples(iri, graph, triple -> {
            if (triple.getPredicate().equals(RDF.TYPE)) {
                types.add(triple.getObject().asString());
            }
        });
        return types;
    }

    private List<String> loadSources(String iri) throws RdfUtilsException {
        return RdfUtils.sparqlSelect(source, getSourceQuery(iri)).stream()
                .map(binding -> binding.get("dataUnit"))
                .collect(Collectors.toList());
    }

    private String getSourceQuery(String iri) {
        return "SELECT ?dataUnit FROM <" + graph + "> WHERE {\n" +
                " <" + iri + "> <" + LP_PIPELINE.HAS_BINDING +
                "> ?binding .\n" +
                "\n" +
                " ?targetComponent a <" + LP_PIPELINE.COMPONENT + "> ;\n" +
                "  <" + LP_PIPELINE.HAS_DATA_UNIT + "> <" + iri + "> .\n" +
                "\n" +
                " ?connection a <" + LP_PIPELINE.CONNECTION + "> ;\n" +
                "  <" + LP_PIPELINE.HAS_SOURCE_BINDING +
                "> ?sourceBinding ;\n" +
                "  <" + LP_PIPELINE.HAS_SOURCE_COMPONENT +
                "> ?sourceComponent ;\n" +
                "  <" + LP_PIPELINE.HAS_TARGET_BINDING +
                "> ?binding ;\n" +
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

    private ExecutionProfile loadProfileOrDefault() throws RdfUtilsException {
        String iri;
        try {
            iri = RdfUtils.sparqlSelectSingle(source,
                    getProfileIriQuery(), "profile");
        } catch (InvalidNumberOfResults ex) {
            return ExecutionProfile.getDefault();
        }
        ExecutionProfile profile = new ExecutionProfile();
        RdfUtils.load(source, iri, graph, profile);
        return profile;
    }

    private String getProfileIriQuery() {
        return "SELECT ?profile FROM <" + graph + "> WHERE {" +
                " <" + pipeline + "> <" + LP_PIPELINE.HAS_PROFILE + ">" +
                " ?profile. " +
                "}";
    }

    private void preparePortGroups(Map<String, Port> ports,
            ExecutionProfile profile) {
        PortGroupGenerator generator = PortGroupGenerator.createGenerator(
                profile.getRdfRepositoryPolicy());
        generator.generateAndAddPortGroups(ports);
    }

}
