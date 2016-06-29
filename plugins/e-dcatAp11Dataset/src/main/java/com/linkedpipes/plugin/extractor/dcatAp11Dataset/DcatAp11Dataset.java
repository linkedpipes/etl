package com.linkedpipes.plugin.extractor.dcatAp11Dataset;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.plugin.extractor.dcatAp11Dataset.DcatAp11DatasetConfig.LocalizedString;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.Component.Sequential;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.util.Repositories;

public class DcatAp11Dataset implements Sequential {

    @Component.OutputPort(id = "Metadata")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public DcatAp11DatasetConfig configuration;

    private final List<Statement> statements = new ArrayList<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() {
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    	IRI dataset = valueFactory.createIRI(configuration.getDatasetIRI());

    	//Mandatory
    	addIRI(dataset, RDF.TYPE, DcatAp11DatasetVocabulary.DCAT_DATASET_CLASS);
    	addLocalizedString(dataset, DCTERMS.TITLE, configuration.getTitles());
    	addLocalizedString(dataset, DCTERMS.DESCRIPTION, configuration.getDescriptions());
    	
    	//Recommended
    	if (!isBlank(configuration.getContactPointEmail()) || !isBlank(configuration.getContactPointName()))
    	{
    		IRI contactPoint = valueFactory.createIRI(configuration.getDatasetIRI() + "/contactPoint");
    		addIRI(dataset, DcatAp11DatasetVocabulary.DCAT_CONTACT_POINT, contactPoint);
    		addIRI(contactPoint, RDF.TYPE, configuration.getContactPointTypeIRI());
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
    	
    	//Optional
    	for (DcatAp11DatasetConfig.Language l : configuration.getLanguages()) {
            addIRI(dataset, DCTERMS.LANGUAGE, valueFactory.createIRI(l.getIri()));
            addIRI(valueFactory.createIRI(l.getIri()), RDF.TYPE, DCTERMS.LINGUISTIC_SYSTEM);
        }

    	String periodicityIRI = configuration.getAccrualPeriodicityIRI();
    	if (!isBlank(periodicityIRI)) {
    		addIRI(dataset, DCTERMS.ACCRUAL_PERIODICITY, periodicityIRI);
    		addIRI(valueFactory.createIRI(periodicityIRI), RDF.TYPE, DCTERMS.FREQUENCY);
    	}
    	
    	if (configuration.getIssued() != null) addValue(dataset, DCTERMS.ISSUED, valueFactory.createLiteral(sdf.format(configuration.getIssued()), DcatAp11DatasetVocabulary.XSD_DATE));
		if (configuration.getModified() != null) addValue(dataset, DCTERMS.MODIFIED, valueFactory.createLiteral(sdf.format(configuration.getModified()), DcatAp11DatasetVocabulary.XSD_DATE));
    	addIRIs(dataset, DCTERMS.SPATIAL, configuration.getSpatialIRIs());
    	for (String s : configuration.getSpatialIRIs()) addIRI(valueFactory.createIRI(s), RDF.TYPE, DCTERMS.LOCATION);
    	
    	if ((configuration.getTemporalStart() != null) || (configuration.getTemporalEnd() != null)) {
    		IRI temporal = valueFactory.createIRI(configuration.getDatasetIRI() + "/temporal");
    		addIRI(temporal, RDF.TYPE, DCTERMS.PERIOD_OF_TIME);
			if (configuration.getTemporalStart() != null) addValue(temporal, DcatAp11DatasetVocabulary.SCHEMA_STARTDATE, valueFactory.createLiteral(sdf.format(configuration.getTemporalStart()), DcatAp11DatasetVocabulary.XSD_DATE));
			if (configuration.getTemporalEnd() != null) addValue(temporal, DcatAp11DatasetVocabulary.SCHEMA_ENDDATE, valueFactory.createLiteral(sdf.format(configuration.getTemporalEnd()), DcatAp11DatasetVocabulary.XSD_DATE));
    	}
    	addIRIs(dataset, FOAF.PAGE, configuration.getDocumentationIRIs());
    	for (String s : configuration.getDocumentationIRIs()) addIRI(valueFactory.createIRI(s), RDF.TYPE, FOAF.DOCUMENT);
    	
    	String accessRightsIRI = configuration.getAccessRightsIRI(); 
    	if (!isBlank(accessRightsIRI)) {
    		addIRI(dataset, DCTERMS.ACCESS_RIGHTS, accessRightsIRI);
    		addIRI(valueFactory.createIRI(accessRightsIRI), RDF.TYPE, DCTERMS.RIGHTS_STATEMENT);
    	}
    	
    	//should be 0..n - can be added
    	addValue(dataset, DCTERMS.IDENTIFIER, configuration.getIdentifier());
    	
    	addIRI(dataset, DCTERMS.TYPE, configuration.getDatasetTypeIRI());
    	
    	//In DCAT-AP 1.1 spec this seems as an RDF Resource. However, see https://joinup.ec.europa.eu/node/150349/ 
    	addLocalizedString(dataset, DCTERMS.PROVENANCE, configuration.getProvenance());
    	
    	//Maybe move somewhere else...? Like distributions
    	addIRIs(dataset, DcatAp11DatasetVocabulary.ADMS_SAMPLE, configuration.getSampleIRIs());
    	
    	addIRIs(dataset, DcatAp11DatasetVocabulary.DCAT_LANDING_PAGE, configuration.getLandingPageIRIs());
    	addIRIs(dataset, DCTERMS.RELATION, configuration.getRelatedIRIs());
    	addIRIs(dataset, DCTERMS.CONFORMS_TO, configuration.getConfromsToIRIs());
    	addIRIs(dataset, DCTERMS.SOURCE, configuration.getSourceIRIs());
    	addIRIs(dataset, DCTERMS.HAS_VERSION, configuration.getHasVersionIRIs());
    	addIRIs(dataset, DCTERMS.IS_VERSION_OF, configuration.getIsVersionOfIRIs());
    	
    	addValue(dataset, OWL.VERSIONINFO, configuration.getVersion());
    	addLocalizedString(dataset, DcatAp11DatasetVocabulary.ADMS_VERSIONNOTES, configuration.getVersionNotes());
    	
        //TODO:
//    	other Identifiers


    	
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
    	if (!isBlank(value)) statements.add(valueFactory.createStatement(subject, predicate, valueFactory.createLiteral(value)));
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
