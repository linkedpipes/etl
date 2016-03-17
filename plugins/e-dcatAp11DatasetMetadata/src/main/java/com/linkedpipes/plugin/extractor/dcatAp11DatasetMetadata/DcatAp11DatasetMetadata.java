package com.linkedpipes.plugin.extractor.dcatAp11DatasetMetadata;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.util.Repositories;

/**
 *
 * @author Petr Škoda
 */
public class DcatAp11DatasetMetadata implements SequentialExecution {

    @DataProcessingUnit.OutputPort(id = "Metadata")
    public WritableSingleGraphDataUnit outputRdf;

    @DataProcessingUnit.Configuration
    public DcatAp11DatasetMetadataConfig configuration;

    private Resource dataset;

    private final List<Statement> statements = new ArrayList<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute(Context context) throws NonRecoverableException {

        // Add all triples.
        Repositories.consume(outputRdf.getRepository(), (RepositoryConnection connection) -> {
            connection.add(statements, outputRdf.getGraph());
        });

    }

    /**
     * Add string value with given language tag if the given string is not empty.
     *
     * @param predicate
     * @param value
     * @param language Is not used if null.
     */
    private void addStringIfNotBlank(IRI predicate, String value, String language) {
        if (isBlank(value)) {
            return;
        }
        final Value object;
        if (language == null)  {
            object = valueFactory.createLiteral(value);
        } else {
            object = valueFactory.createLiteral(value, language);
        }
        statements.add(valueFactory.createStatement(dataset, predicate, object));
    }

    private void addValue(IRI predicate, Value value) {
        statements.add(valueFactory.createStatement(dataset, predicate, value));
    }

    private static boolean isBlank(String string) {
        return string == null || string.isEmpty();
    }

}
