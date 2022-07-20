package com.linkedpipes.etl.library.template.configuration;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class MergeConfiguration {

    public List<Statement> merge(
            List<Statement> parentRdf,
            List<Statement> instanceRdf,
            ConfigurationDescription description,
            String baseIri, Resource graph) throws ConfigurationException {
        List<Statement> result;
        if (description.globalControlProperty() == null) {
            result = (new MergeMemberControlledConfiguration()).merge(
                    parentRdf, instanceRdf, description, baseIri);
        } else {
            result = (new MergeGloballyControlledConfiguration()).merge(
                    parentRdf, instanceRdf, description, baseIri);
        }
        return Statements.wrap(result).withGraph(graph).asList();
    }

}
