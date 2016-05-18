package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.util.Arrays;

/**
 *
 * @author Škoda Petr
 */
public interface DataUnitFactory {

    public class CreationFailed extends LocalizedException {

        public CreationFailed(String message, Object... args) {
            super(Arrays.asList(new LocalizedException.LocalizedString(
                    message, "en")), args);
        }

    }

    /**
     * Create {@link ManagableDataUnit} that implements given interfaces.
     * Returned object should not yet been initialized.
     *
     * @param definition
     * @param resourceIri
     * @param graph
     * @return {@link ManagableDataUnit} or null.
     * @throws CreationFailed
     */
    public ManagableDataUnit create(SparqlSelect definition, String resourceIri,
            String graph) throws CreationFailed;

}
