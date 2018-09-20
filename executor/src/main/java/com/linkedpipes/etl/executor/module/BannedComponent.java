package com.linkedpipes.etl.executor.module;

public class BannedComponent extends ModuleException {

    public BannedComponent(String iri, String pattern) {
        super("Required component: {} is banned by: {}", iri, pattern);
    }

}
