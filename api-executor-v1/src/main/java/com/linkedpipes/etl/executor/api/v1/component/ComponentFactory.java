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
                super(Arrays.asList(new LocalizedException.Message(
                        message, "en")), args);
            }

        }

        public BaseComponent create(SparqlSelect definition, String resourceIri,
                String graph, BundleContext context) throws CreationFailed;

}
