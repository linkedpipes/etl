package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Describe options that can be used to modify pipeline unpacking.
 *
 * @author Petr Škoda
 */
class UnpackOptions implements PojoLoader.Loadable {

    public static final IRI TYPE;

    static {
        final ValueFactory fv = SimpleValueFactory.getInstance();
        TYPE = fv.createIRI(
                "http://etl.linkedpipes.com/ontology/ExecutionOptions");
    }

    public static class ComponentMapping {

        private String source;

        private String target;

    };

    public static class ExecutionMapping {

        private String execution;

        private List<ComponentMapping> components = new LinkedList<>();

    }

    private String debugToComponent;

    private List<ExecutionMapping> executionMapping = new LinkedList<>();

}
