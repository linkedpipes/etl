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
            super(Arrays.asList(new LocalizedString(message, "en")), args);
        }

    }

    /**
     * Create {@link ManagableDataUnit} that implements given interfaces. Returned object should not yet been
     * initialized.
     *
     * @param definition
     * @param resourceUri
     * @param graph
     * @return Null if this factory can not create {@link ManagableDataUnit} with given interfaces.
     * @throws com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory.CreationFailed
     */
    public ManagableDataUnit create(SparqlSelect definition, String resourceUri, String graph) throws CreationFailed;

}
