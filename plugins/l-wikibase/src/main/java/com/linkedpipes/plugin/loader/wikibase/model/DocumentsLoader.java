package com.linkedpipes.plugin.loader.wikibase.model;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.vocabulary.RDF;
import com.linkedpipes.etl.executor.api.v1.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class DocumentsLoader {

    private static final SimpleDateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final String statementPrefix;

    private final String statementQualifierPrefix;

    private final String statementValuePrefix;

    private final String statementReferencePrefix;

    private final String propPrefix;

    private final String noValuePrefix;

    private final RdfSource source;

    public DocumentsLoader(String baseIri, RdfSource source) {
        statementPrefix = baseIri + "prop/statement/";
        statementQualifierPrefix = baseIri + "prop/qualifier/value/";
        statementValuePrefix = baseIri + "prop/statement/value/";
        statementReferencePrefix = baseIri + "prop/reference/value/";
        propPrefix = baseIri + "prop/";
        noValuePrefix = baseIri + "prop/novalue/";
        this.source = source;
    }

    public WikibaseDocument loadDocument(String iri)
            throws RdfException {
        WikibaseDocument document = new WikibaseDocument(iri);
        source.statements(iri, (predicate, value) -> {
            if (RDF.TYPE.equals(predicate)) {
                document.getTypes().add(value.asString());
            } else if (isLabelPredicate(predicate)) {
                document.setLabel(value.asString(), value.getLanguage());
            } else if (predicate.startsWith(propPrefix)) {
                WikibaseStatement statement =
                        loadStatement(predicate, value);
                if (isValidStatement(statement)) {
                    document.addStatement(statement);
                }
            }
        });
        finalizeLoading(document);
        return document;
    }

    private boolean isLabelPredicate(String predicate) {
        return SKOS.PREF_LABEL.equals(predicate) ||
                RDFS.LABEL.toString().equals(predicate);
    }

    private WikibaseStatement loadStatement(String predicate, RdfValue iri)
            throws RdfException {
        if (iri.isBlankNode()) {
            // someValue use different property prefix.
            String wikiPredicate =
                    predicate.substring(statementPrefix.length());
            return WikibaseStatement.someValue(wikiPredicate);
        }
        String wikiPredicate = predicate.substring(propPrefix.length());
        WikibaseStatement statement = new WikibaseStatement(
                iri.asString(), wikiPredicate);
        String valuePrefix = statementPrefix + wikiPredicate;
        source.statements(iri.asString(), (stPredicate, stValue) -> {
            if (RDF.TYPE.equals(stPredicate)) {
                statement.getTypes().add(stValue.asString());
            } else if (stPredicate.equals(valuePrefix)) {
                statement.setValue(stValue.asString());
            } else if (stPredicate.startsWith(statementQualifierPrefix)) {
                String stWikiPredicate =
                        stPredicate.substring(statementValuePrefix.length());
                loadPropQualifier(statement, stWikiPredicate, stValue);
            } else if (stPredicate.startsWith(statementValuePrefix)) {
                String stWikiPredicate =
                        stPredicate.substring(statementValuePrefix.length());
                if (!wikiPredicate.equals(stWikiPredicate)) {
                    throw new RdfException(
                            "Predicate missmatch for {} : {} vs. {} ",
                            iri.asString(), wikiPredicate, stWikiPredicate);
                }
                loadPropValue(statement, stValue);
            } else if (stPredicate.equals(Wikidata.DERIVED_FROM)) {
                statement.addReference(loadReference(predicate, stValue));
            }
        });
        return statement;
    }

    private void loadPropQualifier(
            WikibaseStatement statement, String predicate, RdfValue iri)
            throws RdfException {
        WikibaseValue value = loadValue(iri);
        statement.addQualifierValue(predicate, value);
    }

    private WikibaseValue loadValue(RdfValue iri) throws RdfException {
        List<String> types = new ArrayList<>();
        Map<String, RdfValue> properties = new HashMap<>();
        source.statements(iri.asString(), (predicate, value) -> {
            if (RDF.TYPE.equals(predicate)) {
                types.add(value.asString());
            } else {
                properties.put(predicate, value);
            }
        });

        try {
            if (types.contains(Wikidata.TIME_VALUE_CLASS)) {
                return loadTimeValue(properties);
            } else if (types.contains(Wikidata.GEO_VALUE_CLASS)) {
                return loadGeoValue(properties);
            } else if (types.contains(Wikidata.QUANTITY_VALUE_CLASS)) {
                return loadQuantityValue(properties);
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new RdfException("Can't load statement value.", ex);
        }
    }

    private TimeValue loadTimeValue(Map<String, RdfValue> properties)
            throws RdfException {
        TimeValue value = new TimeValue();
        if (properties.containsKey(Wikidata.TIME_CALENDAR_MODEL)) {
            value.calendarModel =
                    properties.get(Wikidata.TIME_CALENDAR_MODEL).asString();
        }
        if (properties.containsKey(Wikidata.TIME_PRECISION)) {
            value.precision =
                    properties.get(Wikidata.TIME_PRECISION).asLong()
                            .byteValue();
        }
        if (properties.containsKey(Wikidata.TIME_ZONE)) {
            value.timezone =
                    properties.get(Wikidata.TIME_ZONE).asLong().intValue();
        }
        if (properties.containsKey(Wikidata.TIME_VALUE)) {
            Calendar calendar =
                    properties.get(Wikidata.TIME_VALUE).asCalendar();
            value.year = (long) calendar.get(Calendar.YEAR);
            // Java start with 0 for first month.
            value.month = (byte) (calendar.get(Calendar.MONTH) + 1);
            value.day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
            value.hour = (byte) calendar.get(Calendar.HOUR);
            value.minute = (byte) calendar.get(Calendar.MINUTE);
            value.second = (byte) calendar.get(Calendar.SECOND);
        }
        return value;
    }

    private GlobeCoordinateValue loadGeoValue(Map<String, RdfValue> properties)
            throws RdfException {
        GlobeCoordinateValue value = new GlobeCoordinateValue();
        if (properties.containsKey(Wikidata.GEO_GLOBE)) {
            value.globe = properties.get(Wikidata.GEO_GLOBE).asString();
        }
        if (properties.containsKey(Wikidata.GEO_LONGITUDE)) {
            value.longitude = properties.get(Wikidata.GEO_LONGITUDE).asDouble();
        }
        if (properties.containsKey(Wikidata.GEO_LATITUDE)) {
            value.latitude = properties.get(Wikidata.GEO_LATITUDE).asDouble();
        }
        if (properties.containsKey(Wikidata.GEO_PRECISION)) {
            value.precision = properties.get(Wikidata.GEO_PRECISION).asDouble();
        }
        return value;
    }

    private QuantityValue loadQuantityValue(Map<String, RdfValue> properties) {
        QuantityValue value = new QuantityValue();
        if (properties.containsKey(Wikidata.QUANTITY_LOWER_BOUND)) {
            value.lowerBound =
                    new BigDecimal(properties.get(Wikidata.QUANTITY_LOWER_BOUND)
                            .asString());
        }
        if (properties.containsKey(Wikidata.QUANTITY_AMOUNT)) {
            value.amount =
                    new BigDecimal(properties.get(Wikidata.QUANTITY_AMOUNT)
                            .asString());
        }
        if (properties.containsKey(Wikidata.QUANTITY_UNIT)) {
            value.unit = properties.get(Wikidata.QUANTITY_UNIT).asString();
        }
        if (properties.containsKey(Wikidata.QUANTITY_UPPER_BOUND)) {
            value.upperBound =
                    new BigDecimal(properties.get(Wikidata.QUANTITY_UPPER_BOUND)
                            .asString());
        }
        return value;
    }

    private void loadPropValue
            (WikibaseStatement statement, RdfValue iri)
            throws RdfException {
        WikibaseValue value = loadValue(iri);
        statement.addStatementValue(value);
    }

    private WikibaseReference loadReference(String predicate, RdfValue iri)
            throws RdfException {
        String wikiPredicate = predicate.substring(propPrefix.length());
        WikibaseReference reference = new WikibaseReference(
                iri.asString(), wikiPredicate);
        source.statements(iri.asString(), (stPredicate, stValue) -> {
            if (stPredicate.startsWith(statementReferencePrefix)) {
                String stWikiPredicate =
                        stPredicate.substring(statementValuePrefix.length());
                WikibaseValue value = loadValue(stValue);
                reference.addValue(stWikiPredicate, value);
            }
        });
        return reference;
    }

    /**
     * We may load objects that are not statements.
     */
    private boolean isValidStatement(WikibaseStatement statement) {
        return statement.getTypes().contains(Wikidata.STATEMENT) ||
                statement.isSomeValue();
    }

    private void finalizeLoading(WikibaseDocument document) {
        for (String type : document.getTypes()) {
            if (type.startsWith(noValuePrefix)) {
                String predicate = type.substring(noValuePrefix.length());
                document.addNoValuePredicate(predicate);
            }
        }
    }

}
