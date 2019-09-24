package com.linkedpipes.plugin.loader.wikibase.model.values;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Convert RDF value to Wikidata model, inversion to
 * {@link org.wikidata.wdtk.rdf.values.ValueConverter}
 */
public interface ValueConverter
        <V extends org.wikidata.wdtk.datamodel.interfaces.Value>  {

    Set<String> getSupportedTypes();

    V getValue(Value value, String type);

    V getValue(Collection<Statement> statements, Resource resource);

    default void register(Map<String, ValueConverter> register) {
        for (String type : this.getSupportedTypes()) {
            register.put(type, this);
        }
    }

}
