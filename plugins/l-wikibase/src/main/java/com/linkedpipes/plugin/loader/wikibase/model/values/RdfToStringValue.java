package com.linkedpipes.plugin.loader.wikibase.model.values;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.rdf.Vocabulary;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RdfToStringValue implements ValueConverter<StringValue> {

    private final static String SPECIAL_FILE_PATH = "Special:FilePath/";

    private final static String FILE = "File:";

    private final static Set<String> SUPPORTED = new HashSet<>(List.of(
            Vocabulary.DT_STRING,
            Vocabulary.DT_EXTERNAL_ID,
            Vocabulary.DT_MATH,
            Vocabulary.DT_COMMONS_MEDIA,
            Vocabulary.DT_URL,
            Vocabulary.DT_GEO_SHAPE,
            Vocabulary.DT_TABULAR_DATA
    ));

    @Override
    public Set<String> getSupportedTypes() {
        return SUPPORTED;
    }

    @Override
    public StringValue getValue(Value value, String type) {
        switch (type) {
            case Vocabulary.DT_COMMONS_MEDIA:
                return commonFileUrl(value);
            case Vocabulary.DT_GEO_SHAPE:
            case Vocabulary.DT_TABULAR_DATA:
                return commonDataUrl(value);
            case Vocabulary.DT_STRING:
            case Vocabulary.DT_EXTERNAL_ID:
            case Vocabulary.DT_MATH:
            case Vocabulary.DT_URL:
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
        if (Vocabulary.DT_URL.equals(type)) {
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
