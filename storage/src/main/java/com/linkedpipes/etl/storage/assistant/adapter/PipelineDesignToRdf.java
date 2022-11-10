package com.linkedpipes.etl.storage.assistant.adapter;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.storage.assistant.model.PipelineDesign;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class PipelineDesignToRdf {

    private static final String TYPE =
            "http://linkedpipes.com/ontology/PipelineInformation";

    private static final String HAS_TAG =
            "http://etl.linkedpipes.com/ontology/tag";

    private static final String HAS_TEMPLATE =
            "http://etl.linkedpipes.com/ontology/followup";

    private static final String HAS_SOURCE =
            "http://etl.linkedpipes.com/ontology/source";

    private static final String HAS_TARGET =
            "http://etl.linkedpipes.com/ontology/target";

    private static final String HAS_FREQUENCY =
            "http://etl.linkedpipes.com/ontology/frequency";

    public static Statements asRdf(PipelineDesign info) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        StatementsBuilder result = Statements.arrayList().builder();
        Resource resource = valueFactory.createBNode();
        result.addType(resource, TYPE);
        for (String tag : info.tags) {
            result.add(resource, HAS_TAG, tag);
        }
        for (var template : info.templates.entrySet()) {
            for (var followup : template.getValue().followup.entrySet()) {
                Resource entryResource = valueFactory.createBNode();
                result.add(resource, HAS_TEMPLATE, entryResource);
                result.add(entryResource, HAS_SOURCE, template.getKey());
                result.add(entryResource, HAS_TARGET, followup.getKey());
                result.add(entryResource, HAS_FREQUENCY, followup.getValue());
            }
        }
        return result;
    }

}
