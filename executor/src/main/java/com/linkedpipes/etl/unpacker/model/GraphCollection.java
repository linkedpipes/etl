package com.linkedpipes.etl.unpacker.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import org.eclipse.rdf4j.model.Statement;

public class GraphCollection extends HashMap<String, Collection<Statement>> {

    @Override
    public Collection<Statement> get(Object key) {
        return Collections.unmodifiableCollection(super.get(key));
    }
}
