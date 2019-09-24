package com.linkedpipes.plugin.loader.wikibase.model.values;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.implementation.PropertyIdValueImpl;
import org.wikidata.wdtk.datamodel.implementation.TimeValueImpl;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.rdf.OwlDeclarationBuffer;
import org.wikidata.wdtk.rdf.PropertyRegister;
import org.wikidata.wdtk.rdf.RdfWriter;
import org.wikidata.wdtk.rdf.values.EntityIdValueConverter;
import org.wikidata.wdtk.rdf.values.GlobeCoordinatesValueConverter;
import org.wikidata.wdtk.rdf.values.MonolingualTextValueConverter;
import org.wikidata.wdtk.rdf.values.QuantityValueConverter;
import org.wikidata.wdtk.rdf.values.StringValueConverter;
import org.wikidata.wdtk.rdf.values.TimeValueConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RdfToValuesTest {

    @Test
    public void entityConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        writer.start();
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromEntityIdValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_ITEM);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        EntityIdValueConverter converter =
                new EntityIdValueConverter(writer, register, buffer);
        EntityIdValue expected = Datamodel.makeItemIdValue(
                "Q12", "http://www.wikidata.org/entity/");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), true);
        stream.close();
        
        RdfToEntityIdValue reverse = new RdfToEntityIdValue();
        EntityIdValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_ITEM);

        Assert.assertEquals(expected.getEntityType(), actual.getEntityType());
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getSiteIri(), actual.getSiteIri());
        Assert.assertEquals(expected.getIri(), actual.getIri());
    }

    @Test
    public void propertyConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        writer.start();
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromEntityIdValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_PROPERTY);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        EntityIdValueConverter converter =
                new EntityIdValueConverter(writer, register, buffer);
        PropertyIdValue expected = Datamodel.makePropertyIdValue(
                "P12", "http://www.wikidata.org/entity/");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), true);
        stream.close();

        RdfToEntityIdValue reverse = new RdfToEntityIdValue();
        EntityIdValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_PROPERTY);

        Assert.assertEquals(expected.getEntityType(), actual.getEntityType());
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getSiteIri(), actual.getSiteIri());
        Assert.assertEquals(expected.getIri(), actual.getIri());
    }

    @Test
    public void globalCoordinatesConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromGlobeCoordinatesValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_GLOBE_COORDINATES);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        GlobeCoordinatesValueConverter converter =
                new GlobeCoordinatesValueConverter(writer, register, buffer);
        GlobeCoordinatesValue expected = Datamodel.makeGlobeCoordinatesValue(
                30, 70, 1, "http://celestial");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), true);
        stream.close();

        RdfToGlobeCoordinatesValue reverse = new RdfToGlobeCoordinatesValue();
        GlobeCoordinatesValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_GLOBE_COORDINATES);

        Assert.assertEquals(expected.getGlobe(), actual.getGlobe());
        Assert.assertEquals(
                expected.getLatitude(), actual.getLatitude(), 0.001);
        Assert.assertEquals(
                expected.getLongitude(), actual.getLongitude(), 0.001);
        // Precision is not stored.
        Assert.assertEquals(1, actual.getPrecision(), 0.001);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void globalCoordinatesConversionShort() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromGlobeCoordinatesValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_GLOBE_COORDINATES);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        GlobeCoordinatesValueConverter converter =
                new GlobeCoordinatesValueConverter(writer, register, buffer);
        GlobeCoordinatesValue expected = Datamodel.makeGlobeCoordinatesValue(
                30, 70, 1, "http://celestial");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), false);
        stream.close();

        RdfToGlobeCoordinatesValue reverse = new RdfToGlobeCoordinatesValue();
        GlobeCoordinatesValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_GLOBE_COORDINATES);

        Assert.assertEquals(expected.getGlobe(), actual.getGlobe());
        Assert.assertEquals(
                expected.getLatitude(), actual.getLatitude(), 0.001);
        Assert.assertEquals(
                expected.getLongitude(), actual.getLongitude(), 0.001);
        Assert.assertEquals(
                expected.getPrecision(), actual.getPrecision(), 0.001);
    }


    @Test
    public void globalCoordinatesConversion() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        writer.start();
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromQuantityValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_GLOBE_COORDINATES);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        GlobeCoordinatesValueConverter converter =
                new GlobeCoordinatesValueConverter(writer, register, buffer);
        GlobeCoordinatesValue expected = Datamodel.makeGlobeCoordinatesValue(
                30, 70, 1, "http://celestial");
        Resource resource =
                SimpleValueFactory.getInstance().createIRI("http://resource");
        converter.writeValue(expected, resource);
        writer.finish();

        Model model = Rio.parse(
                new ByteArrayInputStream(stream.toByteArray()),
                "http://localhost",
                RDFFormat.NTRIPLES);
        stream.close();

        RdfToGlobeCoordinatesValue reverse = new RdfToGlobeCoordinatesValue();
        GlobeCoordinatesValue actual = reverse.getValue(model, resource);

        Assert.assertEquals(expected.getGlobe(), actual.getGlobe());
        Assert.assertEquals(
                expected.getLatitude(), actual.getLatitude(), 0.001);
        Assert.assertEquals(
                expected.getLongitude(), actual.getLongitude(), 0.001);
        Assert.assertEquals(
                expected.getPrecision(), actual.getPrecision(), 0.001);
    }


    @Test
    public void monolingualTextConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromMonolingualTextValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_MONOLINGUAL_TEXT);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        MonolingualTextValueConverter converter =
                new MonolingualTextValueConverter(writer, register, buffer);
        MonolingualTextValue expected =
                Datamodel.makeMonolingualTextValue("text", "cs");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), true);
        stream.close();

        RdfToMonolingualTextValue reverse = new RdfToMonolingualTextValue();
        MonolingualTextValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_MONOLINGUAL_TEXT);

        Assert.assertEquals(expected.getLanguageCode(), actual.getLanguageCode());
        Assert.assertEquals(expected.getText(), actual.getText());
    }

    @Test
    public void quantityConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromQuantityValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_QUANTITY);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        QuantityValueConverter converter =
                new QuantityValueConverter(writer, register, buffer);
        QuantityValue expected = Datamodel.makeQuantityValue(10L, 5L, 15L);
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), true);
        stream.close();

        RdfToQuantityValue reverse = new RdfToQuantityValue();
        QuantityValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_QUANTITY);

        // It store only value - without bounds.
        Assert.assertEquals(expected.getNumericValue(), actual.getLowerBound());
        Assert.assertEquals(expected.getNumericValue(), actual.getUpperBound());
        Assert.assertEquals(expected.getNumericValue(), actual.getNumericValue());
        Assert.assertEquals(expected.getUnit(), actual.getUnit());
        Assert.assertEquals(expected.getUnitItemId(), actual.getUnitItemId());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void quantityConversionShort() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromQuantityValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_QUANTITY);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        QuantityValueConverter converter =
                new QuantityValueConverter(writer, register, buffer);
        QuantityValue expected = Datamodel.makeQuantityValue(10l, 5l, 15l);
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), false);
        stream.close();

        RdfToQuantityValue reverse = new RdfToQuantityValue();
        QuantityValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_QUANTITY);

        Assert.assertEquals(expected.getLowerBound(), actual.getLowerBound());
        Assert.assertEquals(expected.getUpperBound(), actual.getUpperBound());
        Assert.assertEquals(expected.getNumericValue(), actual.getNumericValue());
        Assert.assertEquals(expected.getUnit(), actual.getUnit());
        Assert.assertEquals(expected.getUnitItemId(), actual.getUnitItemId());
    }

    @Test
    public void quantityConversion() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        writer.start();
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromQuantityValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_QUANTITY);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        QuantityValueConverter converter =
                new QuantityValueConverter(writer, register, buffer);
        QuantityValue expected = Datamodel.makeQuantityValue(10l, 5l, 15l);
        Resource resource =
                SimpleValueFactory.getInstance().createIRI("http://resource");
        converter.writeValue(expected, resource);
        writer.finish();
        stream.close();

        Model model = Rio.parse(
                new ByteArrayInputStream(stream.toByteArray()),
                "http://localhost",
                RDFFormat.NTRIPLES);
        stream.close();

        RdfToQuantityValue reverse = new RdfToQuantityValue();
        QuantityValue actual = reverse.getValue(model, resource);

        Assert.assertEquals(expected.getLowerBound(), actual.getLowerBound());
        Assert.assertEquals(expected.getUpperBound(), actual.getUpperBound());
        Assert.assertEquals(expected.getNumericValue(), actual.getNumericValue());
        Assert.assertEquals(expected.getUnit(), actual.getUnit());
        Assert.assertEquals(expected.getUnitItemId(), actual.getUnitItemId());
    }

    @Test
    public void stringValueStringConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromStringValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_STRING);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        StringValueConverter converter =
                new StringValueConverter(writer, register, buffer);
        StringValue expected = Datamodel.makeStringValue("value");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), true);
        stream.close();

        RdfToStringValue reverse = new RdfToStringValue();
        StringValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_STRING);

        Assert.assertEquals(expected.getString(), actual.getString());
    }

    @Test
    public void stringValueExternalIdConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromStringValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_EXTERNAL_ID);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        StringValueConverter converter =
                new StringValueConverter(writer, register, buffer);
        StringValue expected = Datamodel.makeStringValue("reference");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), true);
        stream.close();

        RdfToStringValue reverse = new RdfToStringValue();
        StringValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_EXTERNAL_ID);

        Assert.assertEquals(expected.getString(), actual.getString());
    }

    @Test
    public void stringValueMathConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromStringValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_MATH);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        StringValueConverter converter =
                new StringValueConverter(writer, register, buffer);
        StringValue expected = Datamodel.makeStringValue("math");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), true);
        stream.close();

        RdfToStringValue reverse = new RdfToStringValue();
        StringValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_MATH);

        Assert.assertEquals(expected.getString(), actual.getString());
    }

    @Test
    public void stringValueCommonsMediaConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromStringValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_COMMONS_MEDIA);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        StringValueConverter converter =
                new StringValueConverter(writer, register, buffer);
        StringValue expected =
                Datamodel.makeStringValue("Gatos_cats_7_cropped.jpg");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl(
                        "P1", "http://site/"), true);
        stream.close();

        RdfToStringValue reverse = new RdfToStringValue();
        StringValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_COMMONS_MEDIA);

        Assert.assertEquals(expected.getString(), actual.getString());
    }

    @Test
    public void stringValueUrlConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromStringValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_URL);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        StringValueConverter converter =
                new StringValueConverter(writer, register, buffer);
        StringValue expected =
                Datamodel.makeStringValue("http://localhost/link");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl(
                        "P1", "http://site/"), true);
        stream.close();

        RdfToStringValue reverse = new RdfToStringValue();
        StringValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_URL);

        Assert.assertEquals(expected.getString(), actual.getString());
    }

    @Test
    public void stringValueGeoShapeConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromStringValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_GEO_SHAPE);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        StringValueConverter converter =
                new StringValueConverter(writer, register, buffer);
        StringValue expected =
                Datamodel.makeStringValue("geo-location");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl(
                        "P1", "http://site/"), true);
        stream.close();

        RdfToStringValue reverse = new RdfToStringValue();
        StringValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_GEO_SHAPE);

        Assert.assertEquals(expected.getString(), actual.getString());
    }

    @Test
    public void stringValueTabularConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromStringValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_TABULAR_DATA);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        StringValueConverter converter =
                new StringValueConverter(writer, register, buffer);
        StringValue expected =
                Datamodel.makeStringValue("tabular-data");
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl(
                        "P1", "http://site/"), true);
        stream.close();

        RdfToStringValue reverse = new RdfToStringValue();
        StringValue actual = reverse.getValue(
                value, DatatypeIdValue.DT_TABULAR_DATA);

        Assert.assertEquals(expected.getString(), actual.getString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void timeValueConversionShort() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromTimeValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_TIME);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        TimeValueConverter converter =
                new TimeValueConverter(writer, register, buffer);
        TimeValue expected = new TimeValueImpl(
                2019, (byte)9, (byte)17, (byte)10, (byte)3, (byte)29,
                TimeValue.PREC_SECOND, 1, 2,
                3, TimeValue.CM_GREGORIAN_PRO);
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), false);
        stream.close();

        RdfToTimeValue reverse = new RdfToTimeValue();
        TimeValue actual = reverse.getValue(value, DatatypeIdValue.DT_TIME);

        Assert.assertEquals(expected.getYear(), actual.getYear());
        Assert.assertEquals(expected.getMonth(), actual.getMonth());
        Assert.assertEquals(expected.getDay(), actual.getDay());
        Assert.assertEquals(expected.getHour(), actual.getHour());
        Assert.assertEquals(expected.getMinute(), actual.getMinute());
        Assert.assertEquals(expected.getSecond(), actual.getSecond());
    }

    @Test
    public void timeValueConversionShortSimple() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        Mockito.when(register.setPropertyTypeFromTimeValue(
                Mockito.any(), Mockito.any()))
                .thenReturn(DatatypeIdValue.DT_TIME);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        TimeValueConverter converter =
                new TimeValueConverter(writer, register, buffer);
        TimeValue expected = new TimeValueImpl(
                2019, (byte)9, (byte)17, (byte)10, (byte)3, (byte)29,
                TimeValue.PREC_SECOND, 1, 2,
                3, TimeValue.CM_GREGORIAN_PRO);
        Value value = converter.getRdfValue(
                expected, new PropertyIdValueImpl("P1", "http://site/"), true);
        stream.close();

        RdfToTimeValue reverse = new RdfToTimeValue();
        TimeValue actual = reverse.getValue(value, DatatypeIdValue.DT_TIME);

        Assert.assertEquals(expected.getYear(), actual.getYear());
        Assert.assertEquals(expected.getMonth(), actual.getMonth());
        Assert.assertEquals(expected.getDay(), actual.getDay());
        Assert.assertEquals(expected.getHour(), actual.getHour());
        Assert.assertEquals(expected.getMinute(), actual.getMinute());
        Assert.assertEquals(expected.getSecond(), actual.getSecond());
    }

    @Test
    public void timeValueConversion() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfWriter writer = new RdfWriter(RDFFormat.NTRIPLES, stream);
        writer.start();
        PropertyRegister register = Mockito.mock(PropertyRegister.class);
        OwlDeclarationBuffer buffer = new OwlDeclarationBuffer();
        TimeValueConverter converter =
                new TimeValueConverter(writer, register, buffer);
        TimeValue expected = new TimeValueImpl(
                2019, (byte)9, (byte)17, (byte)10, (byte)3, (byte)29,
                TimeValue.PREC_SECOND, 1, 2,
                3, TimeValue.CM_GREGORIAN_PRO);
        Resource resource =
                SimpleValueFactory.getInstance().createIRI("http://resource");
        converter.writeValue(expected, resource);
        writer.finish();

        Model model = Rio.parse(
                new ByteArrayInputStream(stream.toByteArray()),
                "http://localhost",
                RDFFormat.NTRIPLES);
        stream.close();

        RdfToTimeValue reverse = new RdfToTimeValue();
        TimeValue actual = reverse.getValue(model, resource);

        Assert.assertEquals(expected.getYear(), actual.getYear());
        Assert.assertEquals(expected.getMonth(), actual.getMonth());
        Assert.assertEquals(expected.getDay(), actual.getDay());
        Assert.assertEquals(expected.getHour(), actual.getHour());
        Assert.assertEquals(expected.getMinute(), actual.getMinute());
        Assert.assertEquals(expected.getSecond(), actual.getSecond());
        Assert.assertEquals(expected.getPrecision(), actual.getPrecision());
        Assert.assertEquals(expected.getPreferredCalendarModel(),
                actual.getPreferredCalendarModel());
        Assert.assertEquals(expected.getPreferredCalendarModelItemId(),
                actual.getPreferredCalendarModelItemId());
        Assert.assertEquals(expected.getTimezoneOffset(),
                actual.getTimezoneOffset());
    }

}

