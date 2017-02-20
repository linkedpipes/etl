package com.linkedpipes.plugin.extractor.datasetMetadata;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.util.Repositories;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatasetMetadata implements Component, SequentialExecution {

    @Component.OutputPort(iri = "Metadata")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public DatasetMetadataConfig configuration;

    private Resource dataset;

    private final List<Statement> statements = new ArrayList<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() throws LpException {
        dataset = valueFactory.createIRI(configuration.getDatasetURI());
        addValue(RDF.TYPE, DatasetMetadataVocabulary.DCAT_DATASET_CLASS);
        //
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // Title
        addStringIfNotBlank(DCTERMS.TITLE, configuration.getTitle_cs(), configuration.getLanguage_orig());
        addStringIfNotBlank(DCTERMS.TITLE, configuration.getTitle_en(), "en");
        // Description.
        addStringIfNotBlank(DCTERMS.DESCRIPTION, configuration.getDesc_cs(), configuration.getLanguage_orig());
        addStringIfNotBlank(DCTERMS.DESCRIPTION, configuration.getDesc_en(), "en");
        // Issued.
        addValue(DCTERMS.ISSUED, valueFactory.createLiteral(
                dateFormat.format(configuration.getIssued()), DatasetMetadataVocabulary.XSD_DATE));
        // Modified.
        if (configuration.isUseNow()) {
            addValue(DCTERMS.MODIFIED, valueFactory.createLiteral(dateFormat.format(new Date()),
                    DatasetMetadataVocabulary.XSD_DATE));
        } else {
            addValue(DCTERMS.MODIFIED, valueFactory.createLiteral(dateFormat.format(configuration.getModified()),
                    DatasetMetadataVocabulary.XSD_DATE));
        }
        //
        addStringIfNotBlank(DCTERMS.IDENTIFIER, configuration.getIdentifier(), null);

        configuration.getKeywords_orig().forEach((keyword) -> {
            addStringIfNotBlank(DatasetMetadataVocabulary.DCAT_KEYWORD, keyword, configuration.getLanguage_orig());
        });

        configuration.getKeywords_en().forEach((keyword) -> {
            addStringIfNotBlank(DatasetMetadataVocabulary.DCAT_KEYWORD, keyword, "en");
        });

        configuration.getLanguages().forEach((language) -> {
            addValue(DCTERMS.LANGUAGE, valueFactory.createURI(language));
        });

        if (!isBlank(configuration.getContactPoint())) {
            final IRI contant = valueFactory.createIRI(configuration.getDatasetURI() + "/contactPoint");
            statements.add(valueFactory.createStatement(contant, RDF.TYPE, DatasetMetadataVocabulary.VCARD_VCARD_CLASS));
            statements.add(valueFactory.createStatement(contant, DatasetMetadataVocabulary.VCARD_HAS_EMAIL,
                    valueFactory.createLiteral(configuration.getContactPoint())));
            statements.add(valueFactory.createStatement(dataset, DatasetMetadataVocabulary.DCAT_CONTACT_POINT, contant));
        }

        if (!isBlank(configuration.getContactPointName())) {
            final IRI contact = valueFactory.createIRI(configuration.getDatasetURI() + "/contactPoint");
            statements.add(valueFactory.createStatement(contact, RDF.TYPE, DatasetMetadataVocabulary.VCARD_VCARD_CLASS));
            statements.add(valueFactory.createStatement(contact, DatasetMetadataVocabulary.VCARD_FN,
                    valueFactory.createLiteral(configuration.getContactPointName())));
            statements.add(valueFactory.createStatement(dataset, DatasetMetadataVocabulary.DCAT_CONTACT_POINT, contact));
        }

        if (!isBlank(configuration.getPeriodicity())) {
            final IRI periodicity = valueFactory.createIRI("http://linked.opendata.cz/resource/accrualPeriodicity/"
                    + configuration.getPeriodicity());
            statements.add(valueFactory.createStatement(periodicity, RDF.TYPE, DCTERMS.FREQUENCY));
            statements.add(valueFactory.createStatement(periodicity, DCTERMS.TITLE,
                    valueFactory.createLiteral(configuration.getPeriodicity())));
            statements.add(valueFactory.createStatement(dataset, DCTERMS.ACCRUAL_PERIODICITY, periodicity));
        }

        addStringIfNotBlank(DatasetMetadataVocabulary.DCAT_LANDING_PAGE, configuration.getLandingPage(), null);

        if (configuration.isUseTemporal()) {
            final IRI temporal = valueFactory.createIRI(configuration.getDatasetURI() + "/temporal");
            statements.add(valueFactory.createStatement(temporal, RDF.TYPE, DCTERMS.PERIOD_OF_TIME));
            statements.add(valueFactory.createStatement(temporal, DatasetMetadataVocabulary.SCHEMA_STARTDATE,
                    valueFactory.createLiteral(dateFormat.format(configuration.getTemporalStart()),
                    DatasetMetadataVocabulary.XSD_DATE)));
            if (configuration.isUseNowTemporalEnd()) {
                statements.add(valueFactory.createStatement(temporal, DatasetMetadataVocabulary.SCHEMA_ENDDATE,
                        valueFactory.createLiteral(dateFormat.format(new Date()),
                        DatasetMetadataVocabulary.XSD_DATE)));
            } else {
                statements.add(valueFactory.createStatement(temporal, DatasetMetadataVocabulary.SCHEMA_ENDDATE,
                        valueFactory.createLiteral(dateFormat.format(configuration.getTemporalEnd()),
                        DatasetMetadataVocabulary.XSD_DATE)));
            }
            statements.add(valueFactory.createStatement(dataset, DCTERMS.TEMPORAL, temporal));
        }

        addStringIfNotBlank(DCTERMS.SPATIAL, configuration.getSpatial(), null);

        addStringIfNotBlank(DCTERMS.REFERENCES, configuration.getSchema(), null);

        for (String author : configuration.getAuthors()) {
            addValue(DCTERMS.CREATOR, valueFactory.createURI(author));
        }

        if (!isBlank(configuration.getPublisherURI())) {
            final IRI publisher = valueFactory.createIRI(configuration.getPublisherURI());
            statements.add(valueFactory.createStatement(publisher, RDF.TYPE, FOAF.AGENT));
            addStringIfNotBlank(FOAF.NAME, configuration.getPublisherName(), null);
            statements.add(valueFactory.createStatement(dataset, DCTERMS.PUBLISHER, publisher));
        }

        if (!isBlank(configuration.getLicense())) {
            addValue(DCTERMS.LICENSE, valueFactory.createURI(configuration.getLicense()));
        }

        configuration.getSources().forEach((source) -> {
            addValue(DCTERMS.SOURCE, valueFactory.createURI(source));
        });


        configuration.getThemes().forEach((themeUri) -> {
            final IRI theme = valueFactory.createIRI(themeUri);
            statements.add(valueFactory.createStatement(theme, RDF.TYPE, SKOS.CONCEPT));
            statements.add(valueFactory.createStatement(dataset, DCTERMS.PUBLISHER, theme));
        });

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
