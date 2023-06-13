package com.linkedpipes.etl.storage.http.adapter;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.http.model.CreatePipelineOptions;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.List;
import java.util.stream.Collectors;

public class RdfToCreatePipelineOptions {

    private static final String TYPE =
            "http://linkedpipes.com/ontology/UpdateOptions";

    private static final String HAS_LABEL =
            "http://www.w3.org/2004/02/skos/core#prefLabel";

    private static final String HAS_TARGET =
            "http://etl.linkedpipes.com/ontology/targetResource";

    public static List<CreatePipelineOptions> asCreatePipelineOptions(
            Statements statements) {
        StatementsSelector selector = statements.selector();
        return selector.selectByType(TYPE)
                .stream().map(statement -> loadOptions(
                        selector.selectByGraph(statement.getContext()),
                        statement.getSubject()))
                .collect(Collectors.toList());
    }

    private static CreatePipelineOptions loadOptions(
            Statements statements, Resource optionsResource) {
        StatementsSelector selector = statements.selector();
        CreatePipelineOptions result = new CreatePipelineOptions();
        for (Statement statement : selector.withSubject(optionsResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case HAS_LABEL:
                    result.targetLabel = value.stringValue();
                    break;
                case HAS_TARGET:
                    if (value instanceof Resource resource) {
                        result.targetResource = resource;
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }
    
}
