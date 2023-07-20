package com.linkedpipes.etl.storage.distribution.adapter;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.distribution.model.ImportPipelineOptions;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.List;
import java.util.stream.Collectors;

public class RdfToImportPipelineOptions {

    private static final String TYPE =
            "http://linkedpipes.com/ontology/UpdateOptions";

    private static final String HAS_PIPELINE =
            "http://etl.linkedpipes.com/ontology/pipeline";

    private static final String HAS_KEEP_URL =
            "http://etl.linkedpipes.com/ontology/keepPipelineUrl";

    private static final String HAS_KEEP_SUFFIX =
            "http://etl.linkedpipes.com/ontology/keepPipelineSuffix";

    private static final String HAS_LABEL =
            "http://www.w3.org/2004/02/skos/core#prefLabel";

    private static final String HAS_IMPORT =
            "http://etl.linkedpipes.com/ontology/importPipeline";

    private static final String HAS_TARGET =
            "http://etl.linkedpipes.com/ontology/targetResource";

    public static List<ImportPipelineOptions> asImportPipelineOptions(
            Statements statements) {
        StatementsSelector selector = statements.selector();
        return selector.selectByType(TYPE)
                .stream().map(statement -> loadOptions(
                        selector.selectByGraph(statement.getContext()),
                        statement.getSubject()))
                .collect(Collectors.toList());
    }

    private static ImportPipelineOptions loadOptions(
            Statements statements, Resource optionsResource) {
        StatementsSelector selector = statements.selector();
        ImportPipelineOptions result = new ImportPipelineOptions();
        for (Statement statement : selector.withSubject(optionsResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case HAS_PIPELINE:
                    if (value instanceof Resource resource) {
                        result.pipeline = resource;
                    }
                    break;
                case HAS_KEEP_URL:
                    if (value instanceof Literal literal) {
                        result.keepPipelineUrl = literal.booleanValue();
                    }
                    break;
                case HAS_KEEP_SUFFIX:
                    if (value instanceof Literal literal) {
                        result.keepPipelineSuffix = literal.booleanValue();
                    }
                    break;
                case HAS_LABEL:
                    result.targetLabel = value.stringValue();
                    break;
                case HAS_IMPORT:
                    if (value instanceof Literal literal) {
                        result.storePipeline = literal.booleanValue();
                    }
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
