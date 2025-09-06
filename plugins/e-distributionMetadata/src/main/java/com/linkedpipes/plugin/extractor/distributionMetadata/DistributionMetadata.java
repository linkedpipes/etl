package com.linkedpipes.plugin.extractor.distributionMetadata;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.plugin.library.rdf.RdfAdapter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.util.Repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DistributionMetadata implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "DatasetMetadata")
    public SingleGraphDataUnit inputRdf;

    @Component.OutputPort(iri = "Metadata")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public DistributionMetadataConfig configuration;

    private final List<Statement> statements = new ArrayList<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public void execute() throws LpException {

        final String datasetUri;
        if (configuration.isUseDatasetURIfromInput()) {
            datasetUri = querySingleResult("SELECT ?d WHERE "
                    + "{?d a <" + DistributionMetadataVocabulary.DCAT_DATASET_CLASS + ">}", "d");
        } else {
            datasetUri = configuration.getDatasetURI();
        }

        final String distributionUri;
        if (configuration.isGenerateDistroURIFromDataset()) {
            distributionUri = datasetUri + "/distribution";
        } else {
            distributionUri = configuration.getDistributionURI();
        }

        final String schemaUri;
        if (configuration.isSchemaFromDataset()) {
            schemaUri = querySingleResult("SELECT ?schema WHERE {<" + datasetUri + "> <" + DCTERMS.REFERENCES + "> ?schema }", "schema");
        } else {
            schemaUri = configuration.getSchema();
        }

        final String license;
        if (configuration.isLicenseFromDataset()) {
            license = querySingleResult("SELECT ?license WHERE {<" + datasetUri + "> <" + DCTERMS.LICENSE + "> ?license }", "license");
        } else {
            license = configuration.getLicense();
        }

        final String originalLanguage;
        if (configuration.isOriginalLanguageFromDataset()) {
            originalLanguage = querySingleResult("SELECT ?language WHERE {<" + datasetUri + "> <" + DCTERMS.TITLE + "> ?title FILTER(!LANGMATCHES(LANG(?title), \"en\")) BIND(LANG(?title) as ?language) }", "language");
        } else {
            originalLanguage = configuration.getLanguage_orig();
        }

        final String title_orig, title_en;
        if (configuration.isTitleFromDataset()) {
            title_en = querySingleResult("SELECT ?title WHERE {<" + datasetUri + "> <" + DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"en\"))}", "title");
            title_orig = querySingleResult("SELECT ?title WHERE {<" + datasetUri + "> <" + DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"" + originalLanguage + "\"))}", "title");
        } else {
            title_orig = configuration.getTitle_orig();
            title_en = configuration.getTitle_en();
        }

        final String description_orig, description_en;
        if (configuration.isTitleFromDataset()) {
            description_en = querySingleResult("SELECT ?description WHERE {<" + datasetUri + "> <" + DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"en\"))}", "description");
            description_orig = querySingleResult("SELECT ?description WHERE {<" + datasetUri + "> <" + DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"" + originalLanguage + "\"))}", "description");
        } else {
            description_orig = configuration.getDesc_orig();
            description_en = configuration.getDesc_en();
        }

        //
        final IRI dataset = valueFactory.createIRI(datasetUri);

        final IRI distribution = valueFactory.createIRI(distributionUri);
        addValue(dataset, DistributionMetadataVocabulary.DCAT_DISTRIBUTION, distribution);
        addValue(distribution, RDF.TYPE, DistributionMetadataVocabulary.DCAT_DISTRIBUTION_CLASS);
        addValue(distribution, RDF.TYPE, DistributionMetadataVocabulary.VOID_DATASET_CLASS);

        // Title
        addStringIfNotBlank(distribution, DCTERMS.TITLE, title_orig, originalLanguage);
        addStringIfNotBlank(distribution, DCTERMS.TITLE, title_en, "en");

        // Description
        addStringIfNotBlank(distribution, DCTERMS.DESCRIPTION, description_orig, originalLanguage);
        addStringIfNotBlank(distribution, DCTERMS.DESCRIPTION, description_en, "en");

        // Issued
        if (configuration.isIssuedFromDataset()) {
            var issued = querySingleResult("SELECT ?issued WHERE {<" + datasetUri + "> <" + DCTERMS.ISSUED + "> ?issued }", "issued");
            addValue(distribution, DCTERMS.ISSUED, valueFactory.createLiteral(issued, DistributionMetadataVocabulary.XSD_DATE));
        } else {
            addValue(distribution, DCTERMS.ISSUED, RdfAdapter.asYearMonthDay(configuration.getIssued()));
        }

        // Modified
        if (configuration.isUseNow()) {
            addValue(distribution, DCTERMS.MODIFIED, RdfAdapter.asYearMonthDay(new Date()));
        } else {
            addValue(distribution, DCTERMS.MODIFIED, RdfAdapter.asYearMonthDay(configuration.getModified()));
        }

        if (configuration.isUseTemporal()) {
            final IRI temporal = valueFactory.createIRI(distributionUri + "/temporal");
            addValue(temporal, RDF.TYPE, DCTERMS.PERIOD_OF_TIME);
            addValue(distribution, DCTERMS.TEMPORAL, temporal);
            if (configuration.isTemporalFromDataset()) {
                var temporalStart = querySingleResult("SELECT ?temporalStart WHERE {<" + datasetUri + "> <" + DCTERMS.TEMPORAL + ">/<" + DistributionMetadataVocabulary.SCHEMA_STARTDATE + "> ?temporalStart }", "temporalStart");
                addValue(temporal, DistributionMetadataVocabulary.SCHEMA_STARTDATE, valueFactory.createLiteral(temporalStart, DistributionMetadataVocabulary.XSD_DATE));
                var temporalEnd = querySingleResult("SELECT ?temporalEnd WHERE {<" + datasetUri + "> <" + DCTERMS.TEMPORAL + ">/<" + DistributionMetadataVocabulary.SCHEMA_ENDDATE + "> ?temporalEnd }", "temporalEnd");
                addValue(temporal, DistributionMetadataVocabulary.SCHEMA_ENDDATE, valueFactory.createLiteral(temporalEnd, DistributionMetadataVocabulary.XSD_DATE));
            } else {
                addValue(temporal, DistributionMetadataVocabulary.SCHEMA_STARTDATE,RdfAdapter.asYearMonthDay(configuration.getTemporalStart()));
                addValue(temporal, DistributionMetadataVocabulary.SCHEMA_ENDDATE,RdfAdapter.asYearMonthDay(configuration.getTemporalEnd()));
            }
        }

        if (!isBlank(schemaUri)) {
            addValue(distribution, DistributionMetadataVocabulary.WDRS_DESCRIBEDBY, valueFactory.createIRI(schemaUri));
        }

        if (!isBlank(configuration.getSchemaType())) {
            addValue(distribution, DistributionMetadataVocabulary.POD_DISTRIBUTION_DESCRIBREBYTYPE, valueFactory.createLiteral(configuration.getSchemaType()));
        }

        if (!isBlank(configuration.getAccessURL())) {
            addValue(distribution, DistributionMetadataVocabulary.DCAT_ACCESSURL, valueFactory.createIRI(configuration.getAccessURL()));
        }

        if (!isBlank(configuration.getDownloadURL())) {
            addValue(distribution, DistributionMetadataVocabulary.DCAT_DOWNLOADURL, valueFactory.createIRI(configuration.getDownloadURL()));
            addValue(distribution, DistributionMetadataVocabulary.VOID_DATADUMP, valueFactory.createIRI(configuration.getDownloadURL()));
        }

        if (!isBlank(configuration.getSparqlEndpointUrl())) {
            addValue(distribution, DistributionMetadataVocabulary.VOID_SPARQLENDPOINT, valueFactory.createIRI(configuration.getSparqlEndpointUrl()));
        }

        if (!isBlank(configuration.getMediaType())) {
            final IRI mediatype = valueFactory.createIRI("http://linked.opendata.cz/resource/mediaType/" + configuration.getMediaType());
            addValue(mediatype, RDF.TYPE, DCTERMS.MEDIA_TYPE_OR_EXTENT);
            addValue(mediatype, DCTERMS.TITLE, valueFactory.createLiteral(configuration.getMediaType()));
            addValue(distribution, DCTERMS.FORMAT, mediatype);
        }

        if (!isBlank(license)) {
            addValue(distribution, DCTERMS.LICENSE, valueFactory.createIRI(license));
        }

        // Lists ...
        configuration.getExampleResources().forEach((example) -> {
            addValue(distribution, DistributionMetadataVocabulary.VOID_EXAMPLERESOURCE, valueFactory.createIRI(example));
        });

        // Add all triples.
        Repositories.consume(outputRdf.getRepository(), (RepositoryConnection connection) -> {
            connection.add(statements, outputRdf.getWriteGraph());
        });

    }

    /**
     * Add string value with given language tag if the given string is not empty.
     *
     * @param subject
     * @param predicate
     * @param value
     * @param language Is not used if null.
     */
    private void addStringIfNotBlank(IRI subject, IRI predicate, String value, String language) {
        if (!isBlank(value)) {
            final Value object;
            if (language == null) {
                object = valueFactory.createLiteral(value);
            } else {
                object = valueFactory.createLiteral(value, language);
            }
            statements.add(valueFactory.createStatement(subject, predicate, object));
        }
    }

    /**
     * Add given value to
     *
     * @param subject
     * @param predicate
     * @param value
     */
    private void addValue(IRI subject, IRI predicate, Value value) {
        statements.add(valueFactory.createStatement(subject, predicate, value));
    }

    /**
     * Execute given SPARQL select and return the first result.
     *
     * @param queryAsString
     * @param bindingName Name of property to return.
     * @return
     */
    private String querySingleResult(final String queryAsString, String bindingName) throws LpException{
        return inputRdf.execute((connection) -> {
            final TupleQuery preparedQuery = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, queryAsString);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(inputRdf.getReadGraph());
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

    private static boolean isBlank(String string) {
        return string == null || string.isEmpty();
    }

}
