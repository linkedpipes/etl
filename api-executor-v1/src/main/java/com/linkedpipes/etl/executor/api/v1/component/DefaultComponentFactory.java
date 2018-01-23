package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.service.DefaultServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class DefaultComponentFactory implements ComponentFactory {

    /**
     * Holds information about bundle content.
     */
    private static class BundleInfo {

        private final List<Class<?>> classes = new ArrayList<>(2);

        private final List<String> packages = new ArrayList<>(2);
    }

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultComponentFactory.class);

    @Override
    public ManageableComponent create(String component, String graph,
            RdfSource definition, BundleContext bundleContext)
            throws LpException {
        final BundleInfo bundleInfo = scan(bundleContext.getBundle());
        if (bundleInfo == null) {
            return null;
        }
        if (bundleInfo.classes.size() != 1) {
            LOG.info("Invalid number of component classes: {}",
                    bundleInfo.classes.size());
            return null;
        }
        // Create instance.
        final Component instance;
        try {
            instance = (Component) bundleInfo.classes.get(0).newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new LpException("Can't create component instance.", ex);
        }
        // Create abd return a wrap.
        if (SequentialExecution.class.isAssignableFrom(instance.getClass())) {
            return new SequentialWrap((SequentialExecution) instance,
                    component, definition, new DefaultServiceFactory());
        }
        return null;
    }

    /**
     * Scan the bundle for recognizable classes and packages.
     *
     * @param bundle
     * @return
     */
    private static BundleInfo scan(Bundle bundle) throws LpException {
        final BundleRevision revision = bundle.adapt(BundleRevision.class);
        final BundleWiring wiring = revision.getWiring();
        if (wiring == null) {
            LOG.info("Can't initialize bundle wiring.");
            return null;
        }
        final BundleInfo info = new BundleInfo();
        // Search.
        final Enumeration<URL> classContent =
                bundle.findEntries("/", "*.class", true);
        while (classContent.hasMoreElements()) {
            final String path = classContent.nextElement().getPath();
            final String className = path.substring(1, path.length() - 6)
                    .replaceAll("/", ".");
            // Store package.
            final String packageName =
                    className.substring(0, className.lastIndexOf("."));
            if (!info.packages.contains(packageName)) {
                info.packages.add(packageName);
            }
            // Check for nested classes.
            if (className.contains("$")) {
                continue;
            }
            // Create instance of class and check for interfaces.
            final Class<?> clazz;
            try {
                clazz = bundle.loadClass(className);
            } catch (ClassNotFoundException ex) {
                LOG.info("Can't load class: {}", className, ex);
                return null;
            }
            if (Component.class.isAssignableFrom(clazz)) {
                info.classes.add(clazz);
            }
        }
        return info;
    }
}
