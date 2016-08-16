package com.linkedpipes.etl.component.api.impl;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.ComponentFactory;
import com.linkedpipes.etl.executor.api.v1.rdf.PojoLoader;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Škoda Petr
 */
@org.osgi.service.component.annotations.Component(immediate = true,
        service = {ComponentFactory.class})
public class ComponentFactoryImpl implements ComponentFactory {

    @Override
    public Component create(SparqlSelect definition, String resourceIri,
            String graph, BundleContext bundleContext,
            Component.Context context)
            throws RdfException {

        // Scan bundle for class with ComponentInstance class.
        final BundleInformation info = scanBundle(bundleContext.getBundle());
        if (info == null) {
            // We can not use this bundle.
            return null;
        }
        // Create an instance.
        final com.linkedpipes.etl.component.api.Component.Sequential instance;
        try {
            instance =
                    (com.linkedpipes.etl.component.api.Component.Sequential) info
                            .getClazz().newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw RdfException.failure("Can't create component class.", ex);
        }
        // Load configuration.
        final ComponentConfiguration configuration
                = new ComponentConfiguration(resourceIri);
        PojoLoader.load(definition, resourceIri, graph, configuration);
        // Create wrap and return it.
        return new SimpleComponentImpl(instance, info, configuration,
                definition, graph, context);
    }

    /**
     * Scan bundle and extract it's content in form of a configuration.
     *
     * @param bundle
     * @return
     */
    private BundleInformation scanBundle(Bundle bundle) throws RdfException {
        final List<Class<?>> mainClasses = new ArrayList<>(1);
        final List<String> packages = new ArrayList<>(2);
        //
        final BundleRevision revision = bundle.adapt(BundleRevision.class);
        final BundleWiring wiring = revision.getWiring();
        if (wiring == null) {
            throw RdfException.failure(
                    "Can't instantiate BundleWiring!");
        }
        // Search for classes.
        final Enumeration<URL> classContent
                = bundle.findEntries("/", "*.class", true);
        while (classContent.hasMoreElements()) {
            final String path = classContent.nextElement().getPath();
            final String className = path.substring(1, path.length() - 6)
                    .replaceAll("/", ".");
            if (!className.contains("$")) {
                final Class<?> clazz;
                try {
                    clazz = bundle.loadClass(className);
                } catch (ClassNotFoundException ex) {
                    throw RdfException.failure("Can't create class:",
                            className, ex);
                }
                // Scan interfaces.
                for (Type item : clazz.getGenericInterfaces()) {
                    if (item.getTypeName().equals(
                            com.linkedpipes.etl.component.api.Component.Sequential.class
                                    .getTypeName())) {
                        mainClasses.add(clazz);
                    }
                }
                // In evere case we can use the package name.
                final String packageName
                        = className.substring(0, className.lastIndexOf("."));
                if (!packages.contains(packageName)) {
                    packages.add(packageName);
                }
            }
        }
        //
        if (mainClasses.isEmpty()) {
            // No class that we can work with.
            return null;
        } else if (mainClasses.size() > 1) {
            // Multiple classes - invalid bundle.
            throw RdfException.failure("Invalid number of components.",
                    mainClasses.size());
        }
        return new BundleInformation(mainClasses.get(0), packages);
    }

}
