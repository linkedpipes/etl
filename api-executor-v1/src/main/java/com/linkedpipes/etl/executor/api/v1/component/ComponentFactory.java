package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.Plugin;
import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import org.osgi.framework.BundleContext;

/**
 * This class is used to load {@link SimpleComponent} from bundles.
 *
 * @author Škoda Petr
 */
public interface ComponentFactory {

    public BaseComponent create(SparqlSelect definition, String resourceIri,
            String graph, BundleContext bundleContext, Plugin.Context context)
            throws RdfException;

}
