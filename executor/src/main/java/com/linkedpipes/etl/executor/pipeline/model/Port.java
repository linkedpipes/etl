package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a DataUnit (port).
 */
public class Port implements Loadable {

    private final String iri;

    private final PipelineComponent owner;

    private final List<String> types = new ArrayList<>(3);

    private String binding;

    private DataSource dataSource;

    private boolean saveDebugData;

    public Port(String iri, PipelineComponent component) {
        this.iri = iri;
        this.owner = component;
    }

    public String getIri() {
        return iri;
    }

    public String getBinding() {
        return binding;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public boolean isInput() {
        return types.contains(LP_PIPELINE.INPUT);
    }

    public boolean isSaveDebugData() {
        return saveDebugData;
    }

    @Override
    public Loadable load(String predicate, BackendRdfValue object)
            throws RdfUtilsException {
        switch (predicate) {
            case RDF.TYPE:
                types.add(object.asString());
                return null;
            case LP_PIPELINE.HAS_BINDING:
                binding = object.asString();
                return null;
            case LP_EXEC.HAS_SOURCE:
                dataSource = new DataSource();
                return dataSource;
            case LP_EXEC.HAS_SAVE_DEBUG_DATA:
                saveDebugData = object.asBoolean();
                return null;
            default:
                return null;
        }
    }

}
