package com.linkedpipes.plugin.extractor.dcatAp11Distribution;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.plugin.extractor.dcatAp11Distribution.DcatAp11DistributionConfig.LocalizedString;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.util.Repositories;

import java.text.SimpleDateFormat;
import java.util.*;

public class DcatAp11Distribution implements Component, SequentialExecution {

    @Component.InputPort(iri = "Dataset")
    public SingleGraphDataUnit inputDataset;

    @Component.OutputPort(iri = "Metadata")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public DcatAp11DistributionConfig configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private final List<Statement> statements = new ArrayList<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() throws LpException {

    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        String datasetIRI;

        if (configuration.getGetDatasetIRIFromInput() != null && configuration.getGetDatasetIRIFromInput()) {
            datasetIRI = querySingleResult("SELECT ?d WHERE "
                    + "{?d a <" + DcatAp11DistributionVocabulary.DCAT_DATASET_CLASS + ">}", "d");
            if (isBlank(datasetIRI)) {
                throw exceptionFactory.failure("Missing dataset in the input data.");
            }

        } else {
            datasetIRI = configuration.getDatasetIRI();
        }

        IRI dataset = valueFactory.createIRI(datasetIRI);

        final String distributionIRI;
        if (configuration.getGenDistroIRI() != null && configuration.getGenDistroIRI()) {
            distributionIRI = datasetIRI + "/distribution";
        } else {
            distributionIRI = configuration.getDistributionIRI();
        }

        IRI distribution = valueFactory.createIRI(distributionIRI);

    	// Mandatory
    	addIRI(dataset, RDF.TYPE, DcatAp11DistributionVocabulary.DCAT_DATASET_CLASS);
        addIRI(distribution, RDF.TYPE, DcatAp11DistributionVocabulary.DCAT_DISTRIBUTION_CLASS);
        addIRI(dataset, DcatAp11DistributionVocabulary.DCAT_DISTRIBUTION, distribution);

        if (configuration.getAccessURLs().isEmpty()) {
            throw exceptionFactory.failure("Access URL is a mandatory property.");
        } else {
            addIRIs(distribution, DcatAp11DistributionVocabulary.DCAT_ACCESSURL, configuration.getAccessURLs());
        }

        // Recommended
        addLocalizedString(distribution, DCTERMS.DESCRIPTION, configuration.getDescriptions());

        if (!isBlank(configuration.getFormatIRI())) {
            addIRI(distribution, DCTERMS.FORMAT, configuration.getFormatIRI());
            addIRI(valueFactory.createIRI(configuration.getFormatIRI()), RDF.TYPE, DCTERMS.MEDIA_TYPE_OR_EXTENT);
        }

        if (!isBlank(configuration.getLicenseIRI())) {
            addIRI(distribution, DCTERMS.LICENSE, configuration.getLicenseIRI());
            addIRI(valueFactory.createIRI(configuration.getLicenseIRI()), RDF.TYPE, DCTERMS.LICENSE_DOCUMENT);
            addIRI(valueFactory.createIRI(configuration.getLicenseIRI()), DCTERMS.TYPE, configuration.getLicenseTypeIRI());
            addIRI(valueFactory.createIRI(configuration.getLicenseTypeIRI()), RDF.TYPE, SKOS.CONCEPT);
        }

        // Optional

        if (configuration.getByteSize() != null) {
            addValue(distribution, DcatAp11DistributionVocabulary.DCAT_BYTESIZE, valueFactory.createLiteral(configuration.getByteSize().toString(), DcatAp11DistributionVocabulary.XSD_DECIMAL));
        }
        if (!isBlank(configuration.getChecksum())) {
            IRI checksumIRI = valueFactory.createIRI(distributionIRI + "/checksum");
            addIRI(distribution, DcatAp11DistributionVocabulary.SPDX_CHECKSUM, checksumIRI);
            addIRI(checksumIRI, RDF.TYPE, DcatAp11DistributionVocabulary.SPDX_CHECKSUM_CLASS);
            addValue(checksumIRI, DcatAp11DistributionVocabulary.SPDX_CHECKSUM_VALUE, valueFactory.createLiteral(configuration.getChecksum(), DcatAp11DistributionVocabulary.XSD_HEXBINARY));
            addIRI(checksumIRI, DcatAp11DistributionVocabulary.SPDX_ALGORITHM, DcatAp11DistributionVocabulary.SPDX_SHA1);
        }

        addIRIs(distribution, FOAF.PAGE, configuration.getDocumentationIRIs());
        addIRIs(distribution, DcatAp11DistributionVocabulary.DCAT_DOWNLOADURL, configuration.getDownloadURLs());
        if (configuration.getLanguagesFromDataset() != null && configuration.getLanguagesFromDataset()) {
            List<Map<String, Value>> result = executeSelectQuery("SELECT ?language WHERE {<" + datasetIRI + "> <" + DCTERMS.LANGUAGE + "> ?language . }");
            for (Map<String, Value> map : result) {
                IRI language = valueFactory.createIRI(map.get("language").stringValue());
                addIRI(distribution, DCTERMS.LANGUAGE, language);
                addIRI(language, RDF.TYPE, DCTERMS.LINGUISTIC_SYSTEM);
            }
        } else {
            addIRIs(distribution, DCTERMS.LANGUAGE, configuration.getLanguages());
            for (String language : configuration.getLanguages())
            {
                addIRI(valueFactory.createIRI(language), RDF.TYPE, DCTERMS.LINGUISTIC_SYSTEM);
            }
        }

        addIRIs(distribution, DCTERMS.CONFORMS_TO, configuration.getConformsToIRIs());

        if (!isBlank(configuration.getMediaType())) {
            IRI mediaType = valueFactory.createIRI("http://www.iana.org/assignments/media-types/" + configuration.getMediaType());
            addIRI(distribution, DcatAp11DistributionVocabulary.DCAT_MEDIATYPE, mediaType);
            addIRI(mediaType, RDF.TYPE, DCTERMS.MEDIA_TYPE_OR_EXTENT);
        }

        String issued;
        if (configuration.getIssuedFromDataset() != null && configuration.getIssuedFromDataset()) {
            issued = querySingleResult("SELECT ?issued WHERE {<" + datasetIRI + "> <" + DCTERMS.ISSUED + "> ?issued }", "issued");
            if (isBlank(issued)) {
                throw exceptionFactory.failure("Missing release date property in the input data.");
            }
            addValue(distribution, DCTERMS.ISSUED, valueFactory.createLiteral(issued, DcatAp11DistributionVocabulary.XSD_DATE));
        } else if (configuration.getIssued() != null) {
            issued = sdf.format(configuration.getIssued());
            addValue(distribution, DCTERMS.ISSUED, valueFactory.createLiteral(issued, DcatAp11DistributionVocabulary.XSD_DATE));
        }

        if (!isBlank(configuration.getRightsIRI())) {
            addIRI(distribution, DCTERMS.RIGHTS, configuration.getRightsIRI());
            addIRI(valueFactory.createIRI(configuration.getRightsIRI()), RDF.TYPE, DCTERMS.RIGHTS_STATEMENT);
        }

        if (!isBlank(configuration.getStatusIRI())) {
            addIRI(distribution, DcatAp11DistributionVocabulary.ADMS_STATUS, configuration.getStatusIRI());
            addIRI(valueFactory.createIRI(configuration.getStatusIRI()), RDF.TYPE, SKOS.CONCEPT);
        }

        addLocalizedString(distribution, DCTERMS.TITLE, configuration.getTitles());

        String modified;
        if (configuration.getModifiedFromDataset() != null && configuration.getModifiedFromDataset()) {
            modified = querySingleResult("SELECT ?modified WHERE {<" + datasetIRI + "> <" + DCTERMS.MODIFIED + "> ?modified }", "modified");
            if (!isBlank(modified)) {
                addValue(distribution, DCTERMS.MODIFIED,
                        valueFactory.createLiteral(modified,
                                DcatAp11DistributionVocabulary.XSD_DATE));
            }
        }
        else if (configuration.getModified() != null) {
            if (configuration.getModifiedNow() != null && configuration.getModifiedNow()) {
                addValue(distribution, DCTERMS.MODIFIED,
                        valueFactory.createLiteral(sdf.format(new Date()),
                                DcatAp11DistributionVocabulary.XSD_DATE));
            } else {
                addValue(distribution, DCTERMS.MODIFIED,
                        valueFactory.createLiteral(sdf.format(configuration.getModified()),
                                DcatAp11DistributionVocabulary.XSD_DATE));
            }
        }

        // Implementation Guidelines

        addIRIs(distribution, DCTERMS.SPATIAL, configuration.getSpatialIRIs());
        for (String s : configuration.getSpatialIRIs()) {
            addIRI(valueFactory.createIRI(s), RDF.TYPE, DCTERMS.LOCATION);
        }

        if ((configuration.getTemporalStart() != null) || (configuration.getTemporalEnd() != null)) {
            IRI temporal = valueFactory.createIRI(distributionIRI + "/temporal");
            addIRI(temporal, RDF.TYPE, DCTERMS.PERIOD_OF_TIME);
            addIRI(distribution, DCTERMS.TEMPORAL, temporal);
            if (configuration.getTemporalStart() != null) {
                addValue(temporal, DcatAp11DistributionVocabulary.SCHEMA_STARTDATE, valueFactory.createLiteral(sdf.format(configuration.getTemporalStart()), DcatAp11DistributionVocabulary.XSD_DATE));
            }
            if (configuration.getTemporalEnd() != null) {
                addValue(temporal, DcatAp11DistributionVocabulary.SCHEMA_ENDDATE, valueFactory.createLiteral(sdf.format(configuration.getTemporalEnd()), DcatAp11DistributionVocabulary.XSD_DATE));
            }
        }

        //StatDCAT-AP draft 4
        if (!isBlank(configuration.getDistributionTypeIRI())) {
            addIRI(distribution, DCTERMS.TYPE, configuration.getDistributionTypeIRI());
            addIRI(valueFactory.createIRI(configuration.getDistributionTypeIRI()), RDF.TYPE, SKOS.CONCEPT);
        }

        // Add all triples.
        Repositories.consume(outputRdf.getRepository(), (RepositoryConnection connection) -> {
            connection.add(statements, outputRdf.getWriteGraph());
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
        return inputDataset.execute((connection) -> {
            final TupleQuery preparedQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryAsString);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(inputDataset.getReadGraph());
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
        return inputDataset.execute((connection) -> {
            final List<Map<String, Value>> output = new LinkedList<>();
            final TupleQuery preparedQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryAsString);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(inputDataset.getReadGraph());
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
