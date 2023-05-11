package com.linkedpipes.etl.library.rdf;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Models;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Use only for testing.
 */
public class StatementsCompare {

    public static boolean isIsomorphic(
            Collection<Statement> expected, Collection<Statement> actual) {
        boolean result = Models.isomorphic(actual, expected);
        if (!result) {
            Set<Statement> actualSet = new HashSet<>(actual);
            Set<Statement> expectedSet = new HashSet<>(expected);
            System.out.println("Size expected: " + expectedSet.size()
                    + " actual: " + actualSet.size());
            System.out.println("Missing:");
            for (Statement statement : expectedSet) {
                if (actualSet.contains(statement)) {
                    continue;
                }
                System.out.println("- " + statement);
            }
            System.out.println("Extra:");
            for (Statement statement : actualSet) {
                if (expectedSet.contains(statement)) {
                    continue;
                }
                System.out.println("+ " + statement);
            }
        }
        return result;
    }

}
