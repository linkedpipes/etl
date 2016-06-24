package com.linkedpipes.plugin.extractor.dcatAp11DatasetMetadata;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.plugin.extractor.dcatAp11DatasetMetadata.DcatAp11DatasetMetadataConfig.LocalizedString;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.Component.Sequential;

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

public class DcatAp11DatasetMetadata implements Sequential {

    @Component.OutputPort(id = "Metadata")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public DcatAp11DatasetMetadataConfig configuration;

    private final List<Statement> statements = new ArrayList<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() {

    	IRI dataset = valueFactory.createIRI(configuration.getDatasetIRI());

    	//Mandatory
    	addIRI(dataset, RDF.TYPE, DcatAp11DatasetMetadataVocabulary.DCAT_DATASET_CLASS);
    	addLocalizedString(dataset, DCTERMS.TITLE, configuration.getTitles());
    	addLocalizedString(dataset, DCTERMS.DESCRIPTION, configuration.getDescriptions());
    	
    	//Recommended
    	if (!isBlank(configuration.getContactPointEmail()) || !isBlank(configuration.getContactPointName()))
    	{
    		IRI contactPoint = valueFactory.createIRI(configuration.getDatasetIRI() + "/contactPoint");
    		addIRI(dataset, DcatAp11DatasetMetadataVocabulary.DCAT_CONTACT_POINT, contactPoint);
    		addIRI(contactPoint, RDF.TYPE, configuration.getContactPointTypeIRI());
    		addValue(contactPoint, DcatAp11DatasetMetadataVocabulary.VCARD_FN, configuration.getContactPointName());
    		addValue(contactPoint, DcatAp11DatasetMetadataVocabulary.VCARD_HAS_EMAIL, configuration.getContactPointEmail());
    	}
    	addLocalizedString(dataset, DcatAp11DatasetMetadataVocabulary.DCAT_KEYWORD, configuration.getKeywords());
    	addIRI(dataset, DcatAp11DatasetMetadataVocabulary.DCAT_THEME, configuration.getEuThemeIRI());
    	addIRIs(dataset, DcatAp11DatasetMetadataVocabulary.DCAT_THEME, configuration.getOtherThemeIRIs());
    	if (!isBlank(configuration.getPublisherIRI())) {
    		IRI publisher = valueFactory.createIRI(configuration.getPublisherIRI());
    		addIRI(dataset, DCTERMS.PUBLISHER, publisher);
    		addIRI(publisher, RDF.TYPE, FOAF.AGENT);
    		addLocalizedString(publisher, FOAF.NAME, configuration.getPublisherNames());
    		addIRI(publisher, DCTERMS.TYPE, configuration.getPublisherTypeIRI());
    	}
    	
    	//Optional
    	addIRIs(dataset, DCTERMS.LANGUAGE, configuration.getLanguages());
    	addIRI(dataset, DCTERMS.ACCRUAL_PERIODICITY, configuration.getAccrualPeriodicityIRI());
    	addValue(dataset, DCTERMS.ISSUED, valueFactory.createLiteral(configuration.getIssued()));
    	addValue(dataset, DCTERMS.MODIFIED, valueFactory.createLiteral(configuration.getModified()));
    	addIRIs(dataset, DCTERMS.SPATIAL, configuration.getSpatialIRIs());
    	if ((configuration.getTemporalStart() != null) || (configuration.getTemporalEnd() != null)) {
    		IRI temporal = valueFactory.createIRI(configuration.getDatasetIRI() + "/temporal");
    		
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
    	if (value != null) statements.add(valueFactory.createStatement(subject, predicate, value));
    }
    private void addValue(IRI subject, IRI predicate, String value) {
    	if (value != null) statements.add(valueFactory.createStatement(subject, predicate, valueFactory.createLiteral(value)));
    }

    private void addIRI(IRI subject, IRI predicate, String stringIRI) {
    	if (!isBlank(stringIRI)) statements.add(valueFactory.createStatement(subject, predicate, valueFactory.createIRI(stringIRI)));
    }
    private void addIRI(IRI subject, IRI predicate, IRI object) {
    	statements.add(valueFactory.createStatement(subject, predicate, object));
    }

    private static boolean isBlank(String string) {
        return string == null || string.isEmpty();
    }

}
