package com.linkedpipes.etl.executor.plugin.osgi;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

class OsgiClassLoader {

    private static final String EMPTY_NAME = "None";

    private static final Logger LOG =
            LoggerFactory.getLogger(OsgiClassLoader.class);

    private final Map<String, Class<?>> classes = new HashMap<>(2);

    protected OsgiClassLoader() {
    }

    public static Map<String, Class<?>> load(
            BundleContext bundleContext)
            throws ExecutorException {
        OsgiClassLoader result = new OsgiClassLoader();
        result.scan(bundleContext.getBundle());
        return result.classes;
    }

    private void scan(Bundle bundle) throws ExecutorException {
        BundleRevision revision = bundle.adapt(BundleRevision.class);
        BundleWiring wiring = revision.getWiring();
        if (wiring == null) {
            throw new ExecutorException("Can't initialize bundle wiring.");
        }
        Enumeration<URL> classContent = listClassEntries(bundle);
        while (classContent.hasMoreElements()) {
            String path = classContent.nextElement().getPath();
            String className = pathToClassName(path);
            if (isNested(className)) {
                continue;
            }
            tryToLoadAndRegisterClass(bundle, className);
        }
    }

    private Enumeration<URL> listClassEntries(Bundle bundle) {
        return bundle.findEntries("/", "*.class", true);
    }

    private String pathToClassName(String path) {
        int to = path.length() - ".class".length();
        return path.substring(1, to).replaceAll("/", ".");
    }

    private boolean isNested(String className) {
        return className.contains("$");
    }

    private void tryToLoadAndRegisterClass(Bundle bundle, String className) {
        Class<?> candidateClass;
        try {
            candidateClass = bundle.loadClass(className);
        } catch (ClassNotFoundException ex) {
            LOG.info("Can't load class: {}", className, ex);
            return;
        }
        if (!Component.class.isAssignableFrom(candidateClass)) {
            return;
        }
        //
        // TODO This is where we need to implement support for v2 components.
        //
        classes.put(EMPTY_NAME, candidateClass);
    }

}
