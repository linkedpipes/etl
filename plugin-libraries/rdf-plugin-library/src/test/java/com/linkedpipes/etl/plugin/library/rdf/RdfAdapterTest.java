package com.linkedpipes.etl.plugin.library.rdf;

import java.text.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RdfAdapterTest {

    @Test
    public void testParseYearMonthDate() throws ParseException {
        var date = RdfAdapter.fromYearMonthDay("2025-09-05");
        var literal = RdfAdapter.asYearMonthDay(date);
        Assertions.assertEquals("2025-09-05", literal.stringValue());
    }
}
