package com.linkedpipes.etl.storage.http.adapter;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.storage.http.model.ImportResponse;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class ImportResponseToRdf {

    private static final String REPORT =
            "http://linkedpipes.com/ontology/ImportReport";

    private static final String HAS_PIPELINE =
            "http://linkedpipes.com/ontology/hasPipeline";

    private static final String PIPELINE =
            "http://linkedpipes.com/ontology/Pipeline";

    private static final String HAS_LABEL =
            "http://www.w3.org/2004/02/skos/core#prefLabel";

    private static final String HAS_TAG =
            "http://etl.linkedpipes.com/ontology/tag";

    private static final String HAS_LOCAL =
            "http://etl.linkedpipes.com/ontology/localResource";

    private static final String HAS_ERROR =
            "http://etl.linkedpipes.com/ontology/errorMessage";

    private static final String HAS_STORED =
            "http://etl.linkedpipes.com/ontology/stored";

    private static final String HAS_TEMPLATE =
            "http://linkedpipes.com/ontology/hasTemplate";

    private static final String REFERENCE_TEMPLATE =
            "http://linkedpipes.com/ontology/Template";

    public static Statements asRdf(ImportResponse model) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        StatementsBuilder result = Statements.arrayList().builder();
        Resource report = valueFactory.createBNode();

        result.addType(report, REPORT);
        for (ImportResponse.Pipeline pipeline : model.pipelines()) {
            Resource resource = pipeline.original();
            result.addType(resource, PIPELINE);
            result.add(report, HAS_PIPELINE, resource);
            result.add(resource, HAS_LABEL, pipeline.label());
            for (String tag : pipeline.tags()) {
                result.add(resource, HAS_TAG, tag);
            }
            result.add(resource, HAS_LOCAL, pipeline.local());
            result.add(resource, HAS_STORED, pipeline.stored());
            if (pipeline.exception() != null) {
                result.add(resource, HAS_ERROR,
                        pipeline.exception().getMessage());
            }
        }

        for (ImportResponse.ReferenceTemplate template :
                model.referenceTemplates()) {
            Resource resource = template.original();
            result.addType(resource, REFERENCE_TEMPLATE);
            result.add(report, HAS_TEMPLATE, resource);
            result.add(resource, HAS_LABEL, template.label());
            for (String tag : template.tags()) {
                result.add(resource, HAS_TAG, tag);
            }
            result.add(resource, HAS_LOCAL, template.local());
            result.add(resource, HAS_STORED, template.stored());
            if (template.exception() != null) {
                result.add(resource, HAS_ERROR,
                        template.exception().getMessage());
            }
        }

        return result;
    }

}
