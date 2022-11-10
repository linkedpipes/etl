package com.linkedpipes.etl.storage.distribution.adapter;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.storage.distribution.model.ImportTemplateOptions;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.List;
import java.util.stream.Collectors;

public class RdfToImportTemplateOptions {

    private static final String TYPE =
            "http://linkedpipes.com/ontology/UpdateOptions";

    private static final String HAS_UPDATE_TEMPLATES =
            "http://etl.linkedpipes.com/ontology/updateExistingTemplates";

    private static final String HAS_IMPORT_TEMPLATES =
            "http://etl.linkedpipes.com/ontology/importNewTemplates";

    public static List<ImportTemplateOptions> asImportTemplateOptions(
            Statements statements) {
        StatementsSelector selector = statements.selector();
        return selector.selectByType(TYPE)
                .stream().map(statement -> loadOptions(
                        selector.selectByGraph(statement.getContext()),
                        statement.getSubject()))
                .collect(Collectors.toList());
    }

    private static ImportTemplateOptions loadOptions(
            Statements statements, Resource resource) {
        StatementsSelector selector = statements.selector();
        ImportTemplateOptions result = new ImportTemplateOptions();
        for (Statement statement : selector.withSubject(resource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case HAS_UPDATE_TEMPLATES:
                    if (value instanceof Literal literal) {
                        result.updateExistingTemplates = literal.booleanValue();
                    }
                    break;
                case HAS_IMPORT_TEMPLATES:
                    if (value instanceof Literal literal) {
                        result.importNewTemplates = literal.booleanValue();
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }

}
