package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a DataUnit (port).
 */
public class Port implements RdfLoader.Loadable<String> {

    private final String iri;

    private final Component owner;

    private final List<String> types = new ArrayList<>(3);

    private String binding;

    private DataSource dataSource;

    public Port(String iri, Component component) {
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

    @Override
    public RdfLoader.Loadable load(String predicate, String object)
            throws RdfUtilsException {
        switch (predicate) {
            case RDF.TYPE:
                types.add(object);
                return null;
            case LP_PIPELINE.HAS_BINDING:
                binding = object;
                return null;
            case LP_EXEC.HAS_SOURCE:
                dataSource = new DataSource();
                return dataSource;
            default:
                return null;
        }
    }

}
