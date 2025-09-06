package com.linkedpipes.etl.library.rdf;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RdfAdapter {

    private final static DateFormat YearMonthDay;

    private final static ValueFactory valueFactory = SimpleValueFactory.getInstance();

    static {
        YearMonthDay = new SimpleDateFormat("yyyy-MM-dd");
        // This is a default time zone.
        var timeZone = TimeZone.getTimeZone("GMT");
        YearMonthDay.setTimeZone(timeZone);
    }

    /**
     * @deprecated Use version with {@link Literal} instead.
     */
    public static Date fromYearMonthDay(String value) throws ParseException {
        return YearMonthDay.parse(value);
    }

    public static Date fromYearMonthDay(Literal value) throws ParseException {
        return YearMonthDay.parse(value.toString());
    }

    public static Literal asRdfj4Literal(Date value) {
        return valueFactory.createLiteral(YearMonthDay.format(value));
    }

}
