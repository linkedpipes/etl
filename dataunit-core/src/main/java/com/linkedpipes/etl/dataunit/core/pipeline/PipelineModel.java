package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;

import java.util.ArrayList;
import java.util.List;

public class PipelineModel {

    Pipeline pipeline;

    public void load(String pipeline, String graph, RdfSource source)
            throws RdfException {
        this.pipeline = new Pipeline();
        RdfToPojoLoader.load(source, pipeline, graph, this.pipeline);
    }

    public void clear() {
        pipeline = null;
    }

    public List<String> getSourcesFor(String dataUnitIri) throws LpException {

        DataUnit dataUnit = pipeline.getDataUnit(dataUnitIri);
        if (dataUnit == null) {
            throw new LpException("Missing data unit object: {}", dataUnitIri);
        }

        Component component = pipeline.getDataUnitOwner(dataUnit);
        if (component == null) {
            throw new LpException("Missing component form: {}", dataUnitIri);
        }

        List<String> output = new ArrayList<>();
        String componentResource = component.getResource();
        String binding = dataUnit.getBinding();
        for (Connection conn : pipeline.getConnections().values()) {
            if (conn.getTargetComponent().equals(componentResource) &&
                    conn.getTargetBinding().equals(binding)) {
                Component sourceComponent = pipeline.getComponents()
                        .get(conn.getSourceComponent());
                String sourceBinding = conn.getSourceBinding();
                //
                sourceComponent.getDataUnits().stream()
                        .filter(item -> item.getBinding().equals(sourceBinding))
                        .forEach(item -> output.add(item.getResource()));
            }
        }

        return output;
    }

    public String getRdfRepository() {
        Repository repository = pipeline.getRepository();
        if (repository == null) {
            return null;
        }
        if (!repository.getTypes().contains(LP_PIPELINE.RDF_REPOSITORY)) {
            return null;
        }
        return repository.getResource();
    }

    public String getRdfRepositoryPolicy() {
        if (pipeline.getExecutionProfile() == null) {
            return LP_PIPELINE.SINGLE_REPOSITORY;
        }
        return pipeline.getExecutionProfile().getRdfRepositoryPolicy();
    }

    public String getRdfRepositoryType() {
        if (pipeline.getExecutionProfile() == null) {
            return LP_PIPELINE.NATIVE_STORE;
        }
        return pipeline.getExecutionProfile().getRdfRepositoryType();
    }

}
