package com.linkedpipes.etl.rdf.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RdfFormatter {

    private final static DateFormat DATE_FORMAT
            = new SimpleDateFormat("YYYY-MM-dd");

    private final static DateFormat TIME_FORMAT
            = new SimpleDateFormat("HH:mm:ss.SSS");

    private RdfFormatter() {

    }

    public static String toXsdDate(Date date) {
        final StringBuilder dateAsString = new StringBuilder(25);
        dateAsString.append(DATE_FORMAT.format(date));
        dateAsString.append("T");
        dateAsString.append(TIME_FORMAT.format(date));
        return dateAsString.toString();
    }

}

