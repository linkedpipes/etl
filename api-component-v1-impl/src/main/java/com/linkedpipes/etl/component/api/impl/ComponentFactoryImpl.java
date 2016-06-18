package com.linkedpipes.etl.component.api.impl;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.executor.api.v1.Plugin;
import com.linkedpipes.etl.executor.api.v1.RdfException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.component.ComponentFactory;
import com.linkedpipes.etl.utils.core.entity.EntityLoader;
import org.osgi.framework.BundleContext;
import com.linkedpipes.etl.executor.api.v1.component.BaseComponent;

/**
 *
 * @author Škoda Petr
 */
@org.osgi.service.component.annotations.Component(immediate = true,
        service = {ComponentFactory.class})
public class ComponentFactoryImpl implements ComponentFactory {

    @Override
    public BaseComponent create(SparqlSelect definition, String resourceIri,
            String graph, BundleContext bundleContext, Plugin.Context context)
            throws RdfException {

        // Scan bundle for class with ComponentInstance class.
        final BundleInformation info = scanBundle(bundleContext.getBundle());
        if (info == null) {
            // We can not use this bundle.
            return null;
        }
        // Create an instance.
        final Component.Sequential instance;
        try {
            instance = (Component.Sequential) info.getClazz().newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw RdfException.cantCreateObject(
                    "Can't create component class.", ex);
        }
        // Load configuration.
        final ComponentConfiguration configuration
                = new ComponentConfiguration(resourceIri);
        EntityLoader.load(definition, resourceIri, graph, configuration);
        // Create wrap and return it.
        return new SimpleComponentImpl(instance, info, configuration,
                definition, graph, context);
    }

    /**
     * Scan bundle and extract it's content in form of a configuration.
     *
     * @param bundle
     * @return
     * @throws ComponentFactory.InvalidBundle
     */
    private BundleInformation scanBundle(Bundle bundle) throws RdfException {
        final List<Class<?>> mainClasses = new ArrayList<>(1);
        final List<String> packages = new ArrayList<>(2);
        //
        final BundleRevision revision = bundle.adapt(BundleRevision.class);
        final BundleWiring wiring = revision.getWiring();
        if (wiring == null) {
            throw RdfException.cantCreateObject(
                    "Can't instatntiate BundleWiring!");
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
                    throw RdfException.cantCreateObject("Can't create class:",
                            className, ex);
                }
                // Scan interfaces.
                for (Type item : clazz.getGenericInterfaces()) {
                    if (item.getTypeName().equals(
                            Component.Sequential.class.getTypeName())) {
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
            throw RdfException.cantCreateObject("Invalid number of components.",
                    mainClasses.size());
        }
        return new BundleInformation(mainClasses.get(0), packages);
    }

}
