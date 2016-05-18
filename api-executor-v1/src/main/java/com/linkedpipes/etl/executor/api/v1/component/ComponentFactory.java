package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.exception.LocalizedException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.util.Arrays;
import org.osgi.framework.BundleContext;

/**
 * This class is used to load {@link SimpleComponent} from bundles.
 *
 * @author Škoda Petr
 */
public interface ComponentFactory {

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
         * @param context
         * @return {@link ManagableDataUnit} or null.
         * @throws CreationFailed
         */
        public BaseComponent create(SparqlSelect definition, String resourceIri,
                String graph, BundleContext context) throws CreationFailed;

}
