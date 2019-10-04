package com.linkedpipes.plugin.loader.wikibase.model.values;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.rdf.RdfWriter;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RdfToTimeValue implements ValueConverter<TimeValue> {

    private static Set<String> SUPPORTED = new HashSet<>(Arrays.asList(
            DatatypeIdValue.DT_TIME
    ));

    @Override
    public Set<String> getSupportedTypes() {
        return SUPPORTED;
    }

    @Override
    public TimeValue getValue(Value value, String type) {
        if (value instanceof IRI) {
            return getValueFromIri((IRI) value);
        } else if (value instanceof Literal) {
            return getValueFromLiteral((Literal) value);
        } else {
            return null;
        }
    }

    private TimeValue getValueFromIri(IRI value) {
        // org.wikidata.wdtk.rdf.Vocabulary.getTimeValueUri(value)
        throw new UnsupportedOperationException();
    }

    private TimeValue getValueFromLiteral(Literal value) {
        XMLGregorianCalendar calendar = value.calendarValue();
        return Datamodel.makeTimeValue(
                calendar.getYear(), (byte) calendar.getMonth(),
                (byte) calendar.getDay(), (byte) calendar.getHour(),
                (byte) calendar.getMinute(), (byte) calendar.getSecond(),
                (byte) 0, 0, 0,
                0, TimeValue.CM_GREGORIAN_PRO);
    }

    @Override
    public TimeValue getValue(
            Collection<Statement> statements, Resource resource, String type) {
        // Default values.
        String calendarModel = TimeValue.CM_GREGORIAN_PRO;
        byte precision = 0;
        int timezone = 0;
        long year = 0;
        byte month = 0;
        byte day = 0;
        byte hour = 0;
        byte minute = 0;
        byte second = 0;
        //
        for (Statement statement : statements) {
            if (!statement.getSubject().equals(resource)) {
                continue;
            }
            Value object = statement.getObject();
            IRI predicate = statement.getPredicate();
            if (predicate.equals(RdfWriter.WB_TIME)) {
                XMLGregorianCalendar calendar =
                        ((Literal) object).calendarValue();
                year = calendar.getYear();
                month = (byte) calendar.getMonth();
                day = (byte) calendar.getDay();
                hour = (byte) calendar.getHour();
                minute = (byte) calendar.getMinute();
                second = (byte) calendar.getSecond();
            } else if (predicate.equals(RdfWriter.WB_TIME_PRECISION)) {
                precision = ((Literal) object).byteValue();
            } else if (predicate.equals(RdfWriter.WB_TIME_TIMEZONE)) {
                timezone = ((Literal) object).intValue();
            } else if (predicate.equals(RdfWriter.WB_TIME_CALENDAR_MODEL)) {
                calendarModel = object.stringValue();
            }
        }
        int beforeTolerance = 0;
        int afterTolerance = 0;
        return Datamodel.makeTimeValue(
                year, month, day, hour, minute, second,
                precision, beforeTolerance, afterTolerance,
                timezone, calendarModel);
    }

}

