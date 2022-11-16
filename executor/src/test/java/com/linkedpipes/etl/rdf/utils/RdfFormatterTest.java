package com.linkedpipes.etl.rdf.utils;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class RdfFormatterTest {

    @Test
    public void testXsdDate() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(2010, 2, 30, 19, 30, 14);
        calendar.set(Calendar.MILLISECOND, 856);
        RdfFormatter format = new RdfFormatter();
        Assertions.assertEquals("2010-03-30T19:30:14.856",
                format.toXsdDate(calendar.getTime()));
    }

}
