package com.linkedpipes.etl.plugin.library.rdf;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class RdfAdapter {

    private static final DateFormat YearMonthDay;

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    static {
        YearMonthDay = new SimpleDateFormat("yyyy-MM-dd");
        // This is a default time zone.
        var timeZone = TimeZone.getTimeZone("GMT");
        YearMonthDay.setTimeZone(timeZone);
    }

    public static Date fromYearMonthDay(String value) throws ParseException {
        return YearMonthDay.parse(value);
    }

    public static Literal asYearMonthDay(Date value) {
        return valueFactory.createLiteral(YearMonthDay.format(value));
    }
}
