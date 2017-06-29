package com.linkedpipes.etl.storage.unpacker.model;

import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class GraphCollection extends HashMap<String, Collection<Statement>> {

    @Override
    public Collection<Statement> get(Object key) {
        return Collections.unmodifiableCollection(super.get(key));
    }

}
