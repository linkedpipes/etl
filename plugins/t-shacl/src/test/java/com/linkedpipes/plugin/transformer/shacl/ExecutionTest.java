package com.linkedpipes.plugin.transformer.shacl;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.ShaclSailValidationException;
import org.eclipse.rdf4j.sail.shacl.results.ValidationReport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

public class ExecutionTest {

    @Test
    public void main() throws IOException {

        ShaclSail shaclSail = new ShaclSail(new MemoryStore());
        SailRepository sailRepository = new SailRepository(shaclSail);
        sailRepository.init();

        try (SailRepositoryConnection connection = sailRepository.getConnection()) {
            connection.begin();
            StringReader shaclRules = new StringReader(
                    String.join("\n", "",
                            "@prefix ex: <http://example.com/ns#> .",
                            "@prefix sh: <http://www.w3.org/ns/shacl#> .",
                            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
                            "@prefix foaf: <http://xmlns.com/foaf/0.1/>.",

                            "ex:PersonShape",
                            "  a sh:NodeShape  ;",
                            "  sh:targetClass foaf:Person ;",
                            "  sh:property ex:PersonShapeProperty .",

                            "ex:PersonShapeProperty ",
                            "  sh:path foaf:age ;",
                            "  sh:datatype xsd:int ;",
                            "  sh:maxCount 1 ;",
                            "  sh:minCount 1 ."
                    ));

            connection.add(shaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
            connection.commit();
        }

        try (SailRepositoryConnection connection = sailRepository.getConnection()) {
            connection.begin();

            StringReader invalidSampleData = new StringReader(
                    String.join("\n", "",
                            "@prefix ex: <http://example.com/ns#> .",
                            "@prefix foaf: <http://xmlns.com/foaf/0.1/>.",
                            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",

                            "ex:peter a foaf:Person ;",
                            "  foaf:age 20, \"30\"^^xsd:int  ."

                    ));

            connection.add(invalidSampleData, "", RDFFormat.TURTLE);
            try {
                connection.commit();
            } catch (RepositoryException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof ShaclSailValidationException) {
                    ValidationReport validationReport = ((ShaclSailValidationException) cause).getValidationReport();
                    Model validationReportModel = ((ShaclSailValidationException) cause).validationReportAsModel();
                    return;
                }
                throw exception;
            }
        }
    }

}
