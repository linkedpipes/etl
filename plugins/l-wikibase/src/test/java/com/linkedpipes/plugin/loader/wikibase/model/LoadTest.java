package com.linkedpipes.plugin.loader.wikibase.model;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.test.TestUtils;
import com.linkedpipes.etl.test.suite.Rdf4jSource;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

public class LoadTest {

    @Test
    public void loadSimpleValue() throws IOException, RdfException {
        Rdf4jSource source = new Rdf4jSource();
        source.loadFile(TestUtils.fileFromResource(
                "simple-value.ttl"));
        DocumentsLoader loader = new DocumentsLoader(
                "https://wikibase.opendata.cz/", source);
        WikibaseDocument document = loader.loadDocument(
                "https://wikibase.opendata.cz/entity/Q2077");
        //
        Assert.assertEquals("https://wikibase.opendata.cz/entity/Q2077",
                document.getIri());
        Assert.assertEquals("Adolfovský dub",
                document.getLabels().get("cs"));
        Assert.assertEquals(1,
                document.getStatements().size());
        WikibaseStatement st = document.getStatements().get(0);
        Assert.assertEquals("103064",
                st.getSimpleValue());
        Assert.assertEquals(
                "https://wikibase.opendata.cz/entity/statement/" +
                        "Q2077-088175BA-D4FF-488D-A555-CC0CE9F48BE6",
                st.getIri());
        Assert.assertEquals("Q2077$088175BA-D4FF-488D-A555-CC0CE9F48BE6",
                st.getStatementId());
    }

    @Test
    public void loadTimeValueAndQualifier() throws IOException, RdfException {
        Rdf4jSource source = new Rdf4jSource();
        source.loadFile(TestUtils.fileFromResource(
                "time-value-and-qualifier.ttl"));
        DocumentsLoader loader = new DocumentsLoader(
                "https://wikibase.opendata.cz/", source);
        WikibaseDocument document = loader.loadDocument("urn:NewItem");
        //
        Assert.assertEquals("Timevalue test item",
                document.getLabels().get("en"));
        Assert.assertEquals(1,
                document.getStatements().size());
        WikibaseStatement st = document.getStatements().get(0);
        Assert.assertEquals(1,
                st.getStatementValues().size());
        Assert.assertEquals("P11",
                st.getPredicate());
        TimeValue value = (TimeValue) st.getStatementValues().get(0);
        Assert.assertEquals("http://www.wikidata.org/entity/Q1985727",
                value.calendarModel
        );
        Assert.assertEquals(11, value.precision.byteValue());
        Assert.assertEquals((Integer) 0, value.timezone);
        Assert.assertEquals("http://www.wikidata.org/entity/Q1985727",
                value.calendarModel
        );
        Assert.assertEquals(1990, value.year.longValue());
        Assert.assertEquals(11, value.month.intValue());
        Assert.assertEquals(1, value.day.byteValue());
        Assert.assertEquals(0, value.hour.byteValue());
        Assert.assertEquals(0, value.minute.byteValue());
        Assert.assertEquals(0, value.second.byteValue());

        Assert.assertEquals(1,
                st.getQualifierValues("P11").size());
        TimeValue qualifier = (TimeValue) st.getQualifierValues("P11").get(0);
        Assert.assertEquals("http://www.wikidata.org/entity/Q1985727",
                qualifier.calendarModel
        );
        Assert.assertEquals(11, qualifier.precision.intValue());
        Assert.assertEquals((Integer) 0,
                qualifier.timezone
        );
        Assert.assertEquals("http://www.wikidata.org/entity/Q1985727",
                qualifier.calendarModel
        );
        Assert.assertEquals(2020, qualifier.year.longValue());
        Assert.assertEquals(1, qualifier.month.byteValue());
        Assert.assertEquals(1, qualifier.day.byteValue());
        Assert.assertEquals(0, qualifier.hour.byteValue());
        Assert.assertEquals(0, qualifier.minute.byteValue());
        Assert.assertEquals(0, qualifier.second.byteValue());

    }

    @Test
    public void loadSomeValue() throws IOException, RdfException {
        Rdf4jSource source = new Rdf4jSource();
        source.loadFile(TestUtils.fileFromResource(
                "some-value.ttl"));
        DocumentsLoader loader = new DocumentsLoader(
                "https://wikibase.opendata.cz/", source);
        WikibaseDocument document = loader.loadDocument("urn:NewItem");
        //
        Assert.assertEquals(1,
                document.getStatements().size());
        WikibaseStatement st = document.getStatements().get(0);
        Assert.assertTrue(st.isSomeValue());
        Assert.assertEquals("P11", st.getPredicate());
    }

    @Test
    public void loadQuantityValueAndQualifier() throws IOException, RdfException {
        Rdf4jSource source = new Rdf4jSource();
        source.loadFile(TestUtils.fileFromResource(
                "quantity-value-and-qualifier.ttl"));
        DocumentsLoader loader = new DocumentsLoader(
                "https://wikibase.opendata.cz/", source);
        WikibaseDocument document = loader.loadDocument("urn:NewItem");
        //
        Assert.assertEquals(1,
                document.getStatements().size());
        WikibaseStatement st = document.getStatements().get(0);
        Assert.assertEquals(1,
                st.getStatementValues().size());
        Assert.assertEquals("P8",
                st.getPredicate());
        Assert.assertNull(st.getSimpleValue());
        QuantityValue value = (QuantityValue) st.getStatementValues().get(0);
        Assert.assertEquals(new BigDecimal("362.0"),
                value.amount);
        Assert.assertEquals(new BigDecimal("362.0"),
                value.lowerBound);
        Assert.assertEquals("https://wikibase.opendata.cz/entity/Q2153",
                value.unit);
        Assert.assertEquals(new BigDecimal("362.0"),
                value.upperBound);
        QuantityValue qualifier =
                (QuantityValue) st.getQualifierValues("P8").get(0);
        Assert.assertEquals(new BigDecimal("3.62"),
                qualifier.amount);
        Assert.assertEquals(new BigDecimal("3.62"),
                qualifier.lowerBound);
        Assert.assertEquals("https://wikibase.opendata.cz/entity/Q2106",
                qualifier.unit);
        Assert.assertEquals(new BigDecimal("3.62"),
                qualifier.upperBound);
    }

    @Test
    public void loadNoValue() throws IOException, RdfException {
        Rdf4jSource source = new Rdf4jSource();
        source.loadFile(TestUtils.fileFromResource(
                "no-value.ttl"));
        DocumentsLoader loader = new DocumentsLoader(
                "https://wikibase.opendata.cz/", source);
        WikibaseDocument document = loader.loadDocument("urn:NewItem");
        //
        Assert.assertEquals(1,
                document.getNoValuePredicates().size());
    }

    @Test
    public void loadGeoValueAndQualifier() throws IOException, RdfException {
        Rdf4jSource source = new Rdf4jSource();
        source.loadFile(TestUtils.fileFromResource(
                "geo-value-and-qualifier.ttl"));
        DocumentsLoader loader = new DocumentsLoader(
                "https://wikibase.opendata.cz/", source);
        WikibaseDocument document = loader.loadDocument("urn:NewItem");
        //
        Assert.assertEquals(1,
                document.getStatements().size());
        WikibaseStatement st = document.getStatements().get(0);
        GlobeCoordinateValue value =
                (GlobeCoordinateValue) st.getStatementValues().get(0);
        Assert.assertEquals("http://www.wikidata.org/entity/Q2",
                value.globe);
        Assert.assertEquals((Double) 1.2933333333333E1,
                value.latitude);
        Assert.assertEquals((Double) 3.53E1,
                value.longitude);
        Assert.assertEquals((Double) 2.77778E-4,
                value.precision);
        GlobeCoordinateValue qualifier =
                (GlobeCoordinateValue) st.getQualifierValues("P12").get(0);
        Assert.assertEquals("http://www.wikidata.org/entity/Q2",
                qualifier.globe);
        Assert.assertEquals((Double) 5.0675546897005E1,
                qualifier.latitude);
        Assert.assertEquals((Double) 1.5538850308905E1,
                qualifier.longitude);
        Assert.assertEquals((Double) 1.0E-6,
                qualifier.precision);
    }

    @Test
    public void loadDeriveFrom() throws IOException, RdfException {
        Rdf4jSource source = new Rdf4jSource();
        source.loadFile(TestUtils.fileFromResource(
                "derive-from.ttl"));
        DocumentsLoader loader = new DocumentsLoader(
                "https://wikibase.opendata.cz/", source);
        WikibaseDocument document = loader.loadDocument("urn:NewItem");
        //
        Assert.assertEquals(1,
                document.getStatements().size());
        WikibaseStatement primary = document.getStatements().get(0);
        Assert.assertEquals(1,
                primary.getStatementValues().size());
        Assert.assertEquals(1,
                primary.getReferences().size());
        WikibaseReference reference = primary.getReferences().get(0);
        Assert.assertEquals(2, reference.getValues().size());
    }

//    public void parserDateTest() throws ParseException {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
//        format.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//        Calendar valueDate = GregorianCalendar.getInstance();
//        valueDate.set(1990, Calendar.NOVEMBER, 1, 0, 0, 0);
//        valueDate.setTimeZone(TimeZone.getTimeZone("UTC"));
//        String valueStr = "1990-11-01T00:00:00+00:00";
//
//        Assert.assertEquals(valueDate.getTime(),
//                format.parse(valueStr));
//
//        Calendar qualifierDate = GregorianCalendar.getInstance();
//        qualifierDate.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
//        qualifierDate.setTimeZone(TimeZone.getTimeZone("UTC"));
//        String qualifierStr = "2020-01-01T00:00:00Z";
//
//        Assert.assertEquals(qualifierDate.getTime(),
//                format.parse(qualifierStr));
//    }

}
