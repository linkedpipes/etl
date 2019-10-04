package com.linkedpipes.plugin.loader.wikibase.model.values;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RdfToStringValue implements ValueConverter<StringValue> {

    private static String SPECIAL_FILE_PATH = "Special:FilePath/";

    private static String FILE = "File:";

    private static Set<String> SUPPORTED = new HashSet<>(Arrays.asList(
            DatatypeIdValue.DT_STRING,
            DatatypeIdValue.DT_EXTERNAL_ID,
            DatatypeIdValue.DT_MATH,
            DatatypeIdValue.DT_COMMONS_MEDIA,
            DatatypeIdValue.DT_URL,
            DatatypeIdValue.DT_GEO_SHAPE,
            DatatypeIdValue.DT_TABULAR_DATA
    ));

    @Override
    public Set<String> getSupportedTypes() {
        return SUPPORTED;
    }

    @Override
    public StringValue getValue(Value value, String type) {
        switch (type) {
            case DatatypeIdValue.DT_COMMONS_MEDIA:
                return commonFileUrl(value);
            case DatatypeIdValue.DT_GEO_SHAPE:
            case DatatypeIdValue.DT_TABULAR_DATA:
                return commonDataUrl(value);
            case DatatypeIdValue.DT_STRING:
            case DatatypeIdValue.DT_EXTERNAL_ID:
            case DatatypeIdValue.DT_MATH:
            case DatatypeIdValue.DT_URL:
                return Datamodel.makeStringValue(value.stringValue());
            default:
                return null;
        }
    }

    @Override
    public StringValue getValue(
            Collection<Statement> statements,
            Resource resource,
            String type) {
        if (DatatypeIdValue.DT_URL.equals(type)) {
            return Datamodel.makeStringValue(resource.stringValue());
        }
        throw new UnsupportedOperationException();
    }

    private StringValue commonFileUrl(Value value) {
        String result;
        String valueStr = value.stringValue();
        if (valueStr.contains(SPECIAL_FILE_PATH)) {
            int index = valueStr.lastIndexOf(SPECIAL_FILE_PATH);
            result = valueStr.substring(index + SPECIAL_FILE_PATH.length());
        } else if (valueStr.contains(FILE)) {
            int index = valueStr.lastIndexOf(FILE);
            result = valueStr.substring(index + FILE.length());
        } else {
            throw new RuntimeException("Unknown commons prefix: " + value);
        }
        result = URLDecoder.decode(result, StandardCharsets.UTF_8);
        return Datamodel.makeStringValue(result);
    }

    private StringValue commonDataUrl(Value value) {
        String prefix = "http://commons.wikimedia.org/data/main/";
        String result = value.stringValue().substring(prefix.length());
        return Datamodel.makeStringValue(result);
    }

}
