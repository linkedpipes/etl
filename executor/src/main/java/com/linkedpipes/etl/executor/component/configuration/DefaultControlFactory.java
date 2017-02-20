package com.linkedpipes.etl.executor.component.configuration;

import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.entity.EntityControl;
import com.linkedpipes.etl.rdf.utils.entity.EntityControlFactory;

class DefaultControlFactory implements EntityControlFactory {

    /**
     * Source with definitions.
     */
    private final RdfSource definitionSource;

    /**
     * Graph in {@link #definitionGraph} with definitions.
     */
    private final String definitionGraph;

    /**
     * @param source Does not manage the given definitionSource.
     * @param graph
     */
    public DefaultControlFactory(RdfSource source, String graph) {
        this.definitionSource = source;
        this.definitionGraph = graph;
    }

    @Override
    public EntityControl create(String type) throws RdfUtilsException {
        final DefaultControl control = new DefaultControl();
        control.loadDefinition(definitionSource, definitionGraph, type);
        return control;
    }

}
