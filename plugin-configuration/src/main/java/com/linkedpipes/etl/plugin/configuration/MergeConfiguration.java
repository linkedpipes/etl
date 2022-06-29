package com.linkedpipes.etl.plugin.configuration;

import com.linkedpipes.etl.plugin.configuration.model.Description;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import java.util.List;

public class MergeConfiguration {

    public List<Statement> merge(
            List<Statement> parentRdf,
            List<Statement> instanceRdf,
            Description description,
            String baseIri, IRI graph) throws InvalidConfiguration {
        List<Statement> result;
        if (description.getGlobalControl() == null) {
            result = (new MergeMemberControlledConfiguration()).merge(
                    parentRdf, instanceRdf, description, baseIri);
        } else {
            result = (new MergeGloballyControlledConfiguration()).merge(
                    parentRdf, instanceRdf, description, baseIri);
        }
        return RdfUtils.setGraph(result, graph);
    }

}
