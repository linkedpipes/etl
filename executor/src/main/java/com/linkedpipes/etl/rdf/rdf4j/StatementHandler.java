package com.linkedpipes.etl.rdf.rdf4j;

import org.eclipse.rdf4j.model.Statement;

@FunctionalInterface
public interface StatementHandler {

    void handle(Statement statement) throws Exception;

}

