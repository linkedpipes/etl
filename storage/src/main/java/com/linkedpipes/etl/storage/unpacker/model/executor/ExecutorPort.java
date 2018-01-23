package com.linkedpipes.etl.storage.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.BackendTripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;

import java.util.LinkedList;
import java.util.List;

public class ExecutorPort {

    private String iri;

    private List<String> types = new LinkedList<>();

    private String binding;

    private List<String> requirements = new LinkedList<>();

    private ExecutorDataSource dataSource = null;

    private boolean saveDebugData = false;

    private Integer group = null;

    public ExecutorPort() {
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public void write(BackendTripleWriter writer) {
        for (String type : types) {
            writer.iri(iri, RDF.TYPE, type);
        }
        writer.string(iri, LP_PIPELINE.HAS_BINDING, binding, null);
        for (String requirement : requirements) {
            writer.iri(iri, LP_PIPELINE.HAS_REQUIREMENT, requirement);
        }
        writer.bool(iri, LP_EXEC.HAS_SAVE_DEBUG_DATA, saveDebugData);
        if (isMapped()) {
            writeDataSource(writer);
        }
        if (group != null) {
            writer.typed(iri, LP_EXEC.HAS_DATA_UNIT_GROUP,
                    group.toString(), XSD.INTEGER);
        }
    }

    private boolean isMapped() {
        return dataSource != null;
    }

    public void writeDataSource(BackendTripleWriter writer) {
        String dataSourceIri = iri + "/dataSource";
        writer.iri(iri, LP_EXEC.HAS_DATA_SOURCE, dataSourceIri);
        dataSource.write(dataSourceIri, writer);
    }

    public String getIri() {
        return iri;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public void setSaveDebugData(boolean saveDebugData) {
        this.saveDebugData = saveDebugData;
    }

    public void setDataSource(ExecutorDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

}
