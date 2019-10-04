package com.linkedpipes.plugin.loader.wikibase.model.values;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.rdf.RdfWriter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RdfToGlobeCoordinatesValue
        implements ValueConverter<GlobeCoordinatesValue> {

    private static Set<String> SUPPORTED = new HashSet<>(Arrays.asList(
            DatatypeIdValue.DT_GLOBE_COORDINATES
    ));

    @Override
    public Set<String> getSupportedTypes() {
        return SUPPORTED;
    }

    @Override
    public GlobeCoordinatesValue getValue(Value value, String type) {
        if (value instanceof IRI) {
            // org.wikidata.wdtk.rdf.Vocabulary.getGlobeCoordinatesValueUri
            throw new UnsupportedOperationException();
        } else if   (value instanceof Literal) {
            return getValueFromLiteral((Literal) value);
        } else {
            return null;
        }
    }

    private GlobeCoordinatesValue getValueFromLiteral(Literal value) {
        String strValue = value.getLabel();
        String globeIri;
        if (strValue.startsWith("<")) {
            // <http://celestial> Point(30.0 70.0)
            globeIri = strValue.substring(1, strValue.indexOf(">"));
        } else {
            // Point(30.0 70.0)
            // Use earth as a default.
            globeIri = GlobeCoordinatesValue.GLOBE_EARTH;
        }
        String point = strValue.substring(
                strValue.indexOf("Point(") + "Point(".length(),
                strValue.length() - 1);
        String[] locations = point.split(" ");

        return Datamodel.makeGlobeCoordinatesValue(
                Double.parseDouble(locations[0]),
                Double.parseDouble(locations[1]),
                1, // we use this as a default.
                globeIri);
    }

    @Override
    public GlobeCoordinatesValue getValue(
            Collection<Statement> statements, Resource resource, String type) {
        double latitude = 0;
        double longitude = 0;
        double precision = 0;
        String globeIri = null;

        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            IRI predicate = statement.getPredicate();
            Value object = statement.getObject();
            if (RdfWriter.WB_GEO_LATITUDE.equals(predicate)) {
                latitude = ((Literal)object).doubleValue();
            } else if (RdfWriter.WB_GEO_LONGITUDE.equals(predicate)) {
                longitude = ((Literal)object).doubleValue();
            } else if (RdfWriter.WB_GEO_PRECISION.equals(predicate)) {
                precision = ((Literal)object).doubleValue();
            } else if (RdfWriter.WB_GEO_GLOBE.equals(predicate)) {
                globeIri = object.stringValue();
            }
        }
        return Datamodel.makeGlobeCoordinatesValue(
                latitude, longitude, precision, globeIri);
    }
}
