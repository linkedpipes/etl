package com.linkedpipes.etl.executor.component.configuration;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.executor.rdf.entity.MergeControl;
import com.linkedpipes.etl.executor.rdf.entity.MergeControlFactory;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;

class DefaultControlFactory implements MergeControlFactory {

    /**
     * Source with definitions.
     */
    private final BackendRdfSource definitionSource;

    /**
     * Graph in {@link #definitionGraph} with definitions.
     */
    private final String definitionGraph;

    public DefaultControlFactory(BackendRdfSource source, String graph) {
        this.definitionSource = source;
        this.definitionGraph = graph;
    }

    @Override
    public MergeControl create(String type) throws RdfUtilsException {
        final DefaultControl control = new DefaultControl();
        control.loadDefinition(definitionSource, type);
        return control;
    }

}
