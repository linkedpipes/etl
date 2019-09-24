package com.linkedpipes.plugin.loader.wikibase.model.values;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RdfToMonolingualTextValue
        implements ValueConverter<MonolingualTextValue>{

    private static Set<String> SUPPORTED = new HashSet<>(Arrays.asList(
            DatatypeIdValue.DT_MONOLINGUAL_TEXT
    ));

    @Override
    public Set<String> getSupportedTypes() {
        return SUPPORTED;
    }

    @Override
    public MonolingualTextValue getValue(Value value, String type) {
        Literal literal = (Literal)value;
        return Datamodel.makeMonolingualTextValue(
                literal.getLabel(),
                literal.getLanguage().get());
    }

    @Override
    public MonolingualTextValue getValue(
            Collection<Statement> statements, Resource resource) {
        throw new UnsupportedOperationException();
    }

}
