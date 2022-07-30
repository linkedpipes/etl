package com.linkedpipes.etl.library.rdf;

import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Use only for testing.
 */
public class StatementsCompare {

    public static boolean equal(
            Collection<Statement> expected,
            Collection<Statement> actual) {
        Set<Statement> expectedSet = new HashSet<>(expected);
        Set<Statement> actualSet = new HashSet<>(actual);
        return expectedSet.size() == actualSet.size() &&
                expectedSet.containsAll(actualSet);
    }

}
