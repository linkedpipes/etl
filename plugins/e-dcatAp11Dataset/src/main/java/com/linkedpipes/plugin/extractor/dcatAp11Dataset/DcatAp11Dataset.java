package com.linkedpipes.plugin.extractor.dcatAp11Dataset;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.plugin.library.rdf.RdfAdapter;
import com.linkedpipes.plugin.extractor.dcatAp11Dataset.DcatAp11DatasetConfig.LocalizedString;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.util.Repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DcatAp11Dataset implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "Metadata")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public DcatAp11DatasetConfig configuration;

    private final List<Statement> statements = new ArrayList<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() {

    	IRI dataset = valueFactory.createIRI(configuration.getDatasetIRI());

    	// Mandatory
    	addIRI(dataset, RDF.TYPE, DcatAp11DatasetVocabulary.DCAT_DATASET_CLASS);
    	addLocalizedString(dataset, DCTERMS.TITLE, configuration.getTitles());
    	addLocalizedString(dataset, DCTERMS.DESCRIPTION, configuration.getDescriptions());

    	// Recommended
    	if (!isBlank(configuration.getContactPointEmail()) || !isBlank(configuration.getContactPointName()))
    	{
    		IRI contactPoint = valueFactory.createIRI(configuration.getDatasetIRI() + "/contactPoint");
    		addIRI(dataset, DcatAp11DatasetVocabulary.DCAT_CONTACT_POINT, contactPoint);
    		addIRI(contactPoint, RDF.TYPE, configuration.getContactPointTypeIRI());

            // Unnecessary, to satisfy validator rule #43 http://52.50.205.146:3031/sparql-doc/rule-43.html
            addIRI(contactPoint, RDF.TYPE, DcatAp11DatasetVocabulary.VCARD_KIND_CLASS);

            addValue(contactPoint, DcatAp11DatasetVocabulary.VCARD_FN, configuration.getContactPointName());
    		addValue(contactPoint, DcatAp11DatasetVocabulary.VCARD_HAS_EMAIL, configuration.getContactPointEmail());
    	}
    	addLocalizedString(dataset, DcatAp11DatasetVocabulary.DCAT_KEYWORD, configuration.getKeywords());
    	addIRI(dataset, DcatAp11DatasetVocabulary.DCAT_THEME, configuration.getEuThemeIRI());
    	addIRIs(dataset, DcatAp11DatasetVocabulary.DCAT_THEME, configuration.getOtherThemeIRIs());
    	if (!isBlank(configuration.getPublisherIRI())) {
    		IRI publisher = valueFactory.createIRI(configuration.getPublisherIRI());
    		addIRI(dataset, DCTERMS.PUBLISHER, publisher);
    		addIRI(publisher, RDF.TYPE, FOAF.AGENT);
    		addLocalizedString(publisher, FOAF.NAME, configuration.getPublisherNames());
    		addIRI(publisher, DCTERMS.TYPE, configuration.getPublisherTypeIRI());
    	}

    	// Optional
    	for (String l : configuration.getLanguages()) {
            addIRI(dataset, DCTERMS.LANGUAGE, valueFactory.createIRI(l));
            addIRI(valueFactory.createIRI(l), RDF.TYPE, DCTERMS.LINGUISTIC_SYSTEM);
        }

    	String periodicityIRI = configuration.getAccrualPeriodicityIRI();
    	if (!isBlank(periodicityIRI)) {
    		addIRI(dataset, DCTERMS.ACCRUAL_PERIODICITY, periodicityIRI);
    		addIRI(valueFactory.createIRI(periodicityIRI), RDF.TYPE, DCTERMS.FREQUENCY);
    	}

    	if (configuration.getIssued() != null) {
            addValue(dataset, DCTERMS.ISSUED, RdfAdapter.asYearMonthDay(configuration.getIssued()));
        }
		if (configuration.getModifiedNow() != null && configuration.getModifiedNow()) {
            addValue(dataset, DCTERMS.MODIFIED, RdfAdapter.asYearMonthDay(new Date()));
        } else {
            if (configuration.getModified() != null) {
                addValue(dataset, DCTERMS.MODIFIED, RdfAdapter.asYearMonthDay(configuration.getModified()));
            }
        }
    	addIRIs(dataset, DCTERMS.SPATIAL, configuration.getSpatialIRIs());
    	for (String s : configuration.getSpatialIRIs()) {
            addIRI(valueFactory.createIRI(s), RDF.TYPE, DCTERMS.LOCATION);
        }

    	if ((configuration.getTemporalStart() != null) || (configuration.getTemporalEnd() != null)) {
    		IRI temporal = valueFactory.createIRI(configuration.getDatasetIRI() + "/temporal");
    		addIRI(dataset, DCTERMS.TEMPORAL, temporal);
            addIRI(temporal, RDF.TYPE, DCTERMS.PERIOD_OF_TIME);
			if (configuration.getTemporalStart() != null) {
                addValue(temporal, DcatAp11DatasetVocabulary.SCHEMA_STARTDATE,
                        RdfAdapter.asYearMonthDay(configuration.getTemporalStart()));
            }
			if (configuration.getTemporalEnd() != null) {
                addValue(temporal, DcatAp11DatasetVocabulary.SCHEMA_ENDDATE,
                        RdfAdapter.asYearMonthDay(configuration.getTemporalEnd()));
            }
    	}
    	addIRIs(dataset, FOAF.PAGE, configuration.getDocumentationIRIs());
    	for (String s : configuration.getDocumentationIRIs()) {
            addIRI(valueFactory.createIRI(s), RDF.TYPE, FOAF.DOCUMENT);
        }

    	String accessRightsIRI = configuration.getAccessRightsIRI();
    	if (!isBlank(accessRightsIRI)) {
    		addIRI(dataset, DCTERMS.ACCESS_RIGHTS, accessRightsIRI);
    		addIRI(valueFactory.createIRI(accessRightsIRI), RDF.TYPE, DCTERMS.RIGHTS_STATEMENT);
    	}

    	// should be 0..n - can be added
    	addValue(dataset, DCTERMS.IDENTIFIER, configuration.getIdentifier());

    	addIRI(dataset, DCTERMS.TYPE, configuration.getDatasetTypeIRI());

    	// In DCAT-AP 1.1 spec this seems as an RDF Resource. However, see https://joinup.ec.europa.eu/node/150349/
    	addLocalizedString(dataset, DCTERMS.PROVENANCE, configuration.getProvenance());

    	// Maybe move somewhere else...? Like distributions
    	addIRIs(dataset, DcatAp11DatasetVocabulary.ADMS_SAMPLE, configuration.getSampleIRIs());

    	if (!isBlank(configuration.getCatalogIRI())) {
            IRI catalog = valueFactory.createIRI(configuration.getCatalogIRI());
            addIRI(catalog, RDF.TYPE, DcatAp11DatasetVocabulary.DCAT_CATALOG_CLASS);
            addIRI(catalog, DcatAp11DatasetVocabulary.DCAT_DATASET, dataset);
        }

        addIRIs(dataset, DcatAp11DatasetVocabulary.DCAT_LANDING_PAGE, configuration.getLandingPageIRIs());
    	addIRIs(dataset, DCTERMS.RELATION, configuration.getRelatedIRIs());
    	addIRIs(dataset, DCTERMS.CONFORMS_TO, configuration.getConformsToIRIs());
    	addIRIs(dataset, DCTERMS.SOURCE, configuration.getSourceIRIs());
    	addIRIs(dataset, DCTERMS.HAS_VERSION, configuration.getHasVersionIRIs());
    	addIRIs(dataset, DCTERMS.IS_VERSION_OF, configuration.getIsVersionOfIRIs());

    	addValue(dataset, OWL.VERSIONINFO, configuration.getVersion());
    	addLocalizedString(dataset, DcatAp11DatasetVocabulary.ADMS_VERSIONNOTES, configuration.getVersionNotes());

        // TODO:
    	// other Identifiers

        // StatDCAT-AP draft 4
        addIRIs(dataset, DcatAp11DatasetVocabulary.STAT_ATTRIBUTE, configuration.getAttributeIRIs());
        for (String s : configuration.getAttributeIRIs()) {
            addIRI(valueFactory.createIRI(s), RDF.TYPE, DcatAp11DatasetVocabulary.QB_ATTRIBUTEPROPERTY_CLASS);
        }
        addIRIs(dataset, DcatAp11DatasetVocabulary.STAT_DIMENSION, configuration.getDimensionIRIs());
        for (String s : configuration.getDimensionIRIs()) {
            addIRI(valueFactory.createIRI(s), RDF.TYPE, DcatAp11DatasetVocabulary.QB_DIMENSIONPROPERTY_CLASS);
        }
        if (configuration.getNumSeries() != null) {
            addValue(dataset, DcatAp11DatasetVocabulary.STAT_NUMSERIES, valueFactory.createLiteral(configuration.getNumSeries().toString(), DcatAp11DatasetVocabulary.XSD_INTEGER));
        }
        addIRIs(dataset, DcatAp11DatasetVocabulary.DQV_HASQUALITYANNOTATION, configuration.getQualityAnnotationIRIs());
        for (String s : configuration.getQualityAnnotationIRIs()) {
            addIRI(valueFactory.createIRI(s), RDF.TYPE, DcatAp11DatasetVocabulary.OA_ANNOTATION_CLASS);
        }
        addIRIs(dataset, DcatAp11DatasetVocabulary.STAT_STATMEASURE, configuration.getUnitOfMeasurementIRIs());
        for (String s : configuration.getUnitOfMeasurementIRIs()) {
            addIRI(valueFactory.createIRI(s), RDF.TYPE, SKOS.CONCEPT);
        }

    	// Add all triples.
        Repositories.consume(outputRdf.getRepository(), (RepositoryConnection connection) -> {
            connection.add(statements, outputRdf.getWriteGraph());
        });

    }

    private void addLocalizedString(IRI subject, IRI predicate, List<LocalizedString> strings) {
        for (LocalizedString s : strings) {
        	statements.add(valueFactory.createStatement(subject, predicate,
                    createString(s.getValue(), s.getLanguage())));
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

    private Value createString(String value, String language) {
        if (language == null || language.isEmpty()) {
            return valueFactory.createLiteral(value);
        } else {
            return valueFactory.createLiteral(value, language);
        }
    }

}
