package com.linkedpipes.etl.executor.plugin.osgi;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.plugin.PluginHolder;
import com.linkedpipes.etl.executor.plugin.v1.PluginV1Holder;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.plugin.api.v2.LinkedPipesPlugin;
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

    private static final String V1_EMPTY_NAME = "None";

    private static final Logger LOG =
            LoggerFactory.getLogger(OsgiClassLoader.class);

    private final Map<String, PluginTemplate> unusedTemplates = new HashMap<>();

    private final Map<String, Class<?>> classes = new HashMap<>(2);

    private final Map<String, PluginHolder> holders = new HashMap<>();

    protected OsgiClassLoader(JavaPlugin javaPlugin) {
        for (PluginTemplate template : javaPlugin.templates()) {
            unusedTemplates.put(template.resource().stringValue(), template);
        }
    }

    public static Map<String, PluginHolder> load(
            JavaPlugin javaPlugin, BundleContext bundleContext)
            throws ExecutorException {
        OsgiClassLoader result = new OsgiClassLoader(javaPlugin);
        result.scan(bundleContext.getBundle());
        return result.holders;
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
        checkForV1Component();
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

    /**
     * Return matching template.
     */
    private void tryToLoadAndRegisterClass(
            Bundle bundle, String className) {
        Class<?> candidateClass;
        try {
            candidateClass = bundle.loadClass(className);
        } catch (ClassNotFoundException ex) {
            LOG.warn("Can't load class: {}", className, ex);
            return;
        }
        String componentIri = getComponentIri(candidateClass);
        if (com.linkedpipes.etl.executor.api.v1.component.Component.class.
                isAssignableFrom(candidateClass)) {
            classes.put(componentIri, candidateClass);
        }
        // TODO This is where we need to implement support for v2 components.
    }

    private String getComponentIri(Class<?> candidateClass) {
        var annotation = candidateClass.getAnnotation(
                LinkedPipesPlugin.IRI.class);
        if (annotation == null) {
            return V1_EMPTY_NAME;
        } else {
            return annotation.value();
        }
    }

    private void checkForV1Component() {
        // The version one contain only one template definition and
        // one instance of Component class.
        if (!classes.containsKey(V1_EMPTY_NAME) ||
                unusedTemplates.size() != 1) {
            return;
        }
        PluginTemplate template =
                unusedTemplates.values().iterator().next();
        Class<?> componentClass = classes.get(V1_EMPTY_NAME);
        // Create as a default.
        holders.put(
                template.resource().stringValue(),
                new PluginV1Holder(template, componentClass));
    }

}
