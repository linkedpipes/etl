package com.linkedpipes.etl.rdf.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RdfFormatter {

    private final DateFormat dateFormat
            = new SimpleDateFormat("YYYY-MM-dd");

    private final DateFormat timeFormat
            = new SimpleDateFormat("HH:mm:ss.SSS");

    public RdfFormatter() {

    }

    public String toXsdDate(Date date) {
        final StringBuilder dateAsString = new StringBuilder(25);
        dateAsString.append(dateFormat.format(date));
        dateAsString.append("T");
        dateAsString.append(timeFormat.format(date));
        return dateAsString.toString();
    }

}

