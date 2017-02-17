package com.linkedpipes.plugin.extractor.voidDataset;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.Component.Sequential;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import com.linkedpipes.plugin.extractor.voidDataset.VoidDatasetConfig.LocalizedString;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.util.Repositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class VoidDataset implements Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(VoidDataset.class);

    @Component.InputPort(id = "Distribution", optional = true)
    public SingleGraphDataUnit inputDistribution;

    @Component.OutputPort(id = "Metadata")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public VoidDatasetConfig configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private final List<Statement> statements = new ArrayList<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() throws LpException {

        String distributionIRI;

        if (configuration.getGetDistributionIRIFromInput() != null && configuration.getGetDistributionIRIFromInput()) {
            distributionIRI = querySingleResult("SELECT ?d WHERE "
                    + "{?d a <" + VoidDatasetVocabulary.DCAT_DISTRIBUTION_CLASS + ">}", "d");
            if (isBlank(distributionIRI)) {
                throw exceptionFactory.failure("Missing distribution in the input data.");
            }

        } else {
            distributionIRI = configuration.getDistributionIRI();
        }

        IRI distribution = valueFactory.createIRI(distributionIRI);

    	addIRI(distribution, RDF.TYPE, VoidDatasetVocabulary.VOID_DATASET_CLASS);
        addIRIs(distribution, VoidDatasetVocabulary.VOID_EXAMPLE_RESOURCE, configuration.getExampleResourceIRIs());

        if (!isBlank(configuration.getSparqlEndpointIRI())) {
            addIRI(distribution, VoidDatasetVocabulary.VOID_SPARQL_ENDPOINT, configuration.getSparqlEndpointIRI());
        }

        if (configuration.getCopyDownloadURLsToDataDumps() != null && configuration.getCopyDownloadURLsToDataDumps()) {
            List<Map<String, Value>> results = executeSelectQuery("SELECT ?downloadURL WHERE { <" + distributionIRI + "> <" + VoidDatasetVocabulary.DCAT_DOWNLOADURL + "> ?downloadURL .}");
            List<String> downloadURLs = new LinkedList<>();
            for (Map<String, Value> result : results) {
                downloadURLs.add(result.get("downloadURL").toString());
            }
            addIRIs(distribution, VoidDatasetVocabulary.VOID_DATA_DUMP, downloadURLs);
        }

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
    private void addStringIfNotBlank(IRI subject, IRI predicate, String value, String language) {
        if (isBlank(value)) {
            return;
        }
        final Value object;
        if (language == null)  {
            object = valueFactory.createLiteral(value);
        } else {
            object = valueFactory.createLiteral(value, language);
        }
        statements.add(valueFactory.createStatement(subject, predicate, object));
    }

    private void addLocalizedString(IRI subject, IRI predicate, List<LocalizedString> strings) {
        for (LocalizedString s : strings) {
        	statements.add(valueFactory.createStatement(subject, predicate, valueFactory.createLiteral(s.getValue(), s.getLanguage())));
        }
    }

    private void addIRIs(IRI subject, IRI predicate, List<String> IRIs) {
        for (String s : IRIs) {
        	statements.add(valueFactory.createStatement(subject, predicate, valueFactory.createIRI(s)));
        }
    }

    private void addValue(IRI subject, IRI predicate, Value value) {
    	if (value != null) {
            statements.add(valueFactory.createStatement(subject, predicate, value));
        }
    }
    private void addValue(IRI subject, IRI predicate, String value) {
    	if (!isBlank(value)) {
            statements.add(valueFactory.createStatement(subject, predicate, valueFactory.createLiteral(value)));
        }
    }

    private void addIRI(IRI subject, IRI predicate, String stringIRI) {
    	if (!isBlank(stringIRI)) {
            statements.add(valueFactory.createStatement(subject, predicate, valueFactory.createIRI(stringIRI)));
        }
    }
    
    private void addIRI(IRI subject, IRI predicate, IRI object) {
    	statements.add(valueFactory.createStatement(subject, predicate, object));
    }

    private static boolean isBlank(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Execute given SPARQL select and return the first result.
     *
     * @param queryAsString
     * @param bindingName Name of property to return.
     * @return
     */
    private String querySingleResult(final String queryAsString, String bindingName) throws LpException{
        return inputDistribution.execute((connection) -> {
            final TupleQuery preparedQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryAsString);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(inputDistribution.getGraph());
            preparedQuery.setDataset(dataset);
            //
            final TupleQueryResult result = preparedQuery.evaluate();
            //
            if (!result.hasNext()) {
                return null;
            }
            final Value value = result.next().getValue(bindingName);
            if (value == null) {
                return null;
            }
            return value.stringValue();
        });
    }

    private List<Map<String, Value>> executeSelectQuery(final String queryAsString) throws LpException {
        return inputDistribution.execute((connection) -> {
            final List<Map<String, Value>> output = new LinkedList<>();
            final TupleQuery preparedQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryAsString);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(inputDistribution.getGraph());
            preparedQuery.setDataset(dataset);
            //
            TupleQueryResult result = preparedQuery.evaluate();
            while (result.hasNext()) {
                final BindingSet binding = result.next();
                final Map<String, Value> row = new HashMap<>();
                binding.forEach((item) -> {
                    row.put(item.getName(), item.getValue());
                });
                output.add(row);
            }

            return output;
        });
    }

}
