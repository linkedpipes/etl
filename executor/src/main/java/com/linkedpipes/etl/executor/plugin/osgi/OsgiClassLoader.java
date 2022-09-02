package com.linkedpipes.etl.executor.plugin.osgi;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.plugin.PluginHolder;
import com.linkedpipes.etl.executor.plugin.v1.PluginV1Holder;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.plugin.api.v2.ComponentV2;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

class OsgiClassLoader {

    private static final String V1_EMPTY_NAME = "None";

    private static final Logger LOG =
            LoggerFactory.getLogger(OsgiClassLoader.class);

    private final Map<String, PluginTemplate> templates = new HashMap<>();

    private final Map<String, Class<?>> classes = new HashMap<>(2);

    protected OsgiClassLoader(JavaPlugin javaPlugin) {
        for (PluginTemplate template : javaPlugin.templates()) {
            templates.put(template.resource().stringValue(), template);
        }
    }

    public static Map<String, PluginHolder> load(
            JavaPlugin javaPlugin, BundleContext bundleContext)
            throws ExecutorException {
        return new OsgiClassLoader(javaPlugin).scan(bundleContext.getBundle());
    }

    private Map<String, PluginHolder> scan(Bundle bundle)
            throws ExecutorException {
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
        return collectPluginHolders();
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
                ComponentV2.IRI.class);
        if (annotation == null) {
            return V1_EMPTY_NAME;
        } else {
            return annotation.value();
        }
    }

    private Map<String, PluginHolder> collectPluginHolders() {
        // Backward compatibility for version 1.
        if (classes.containsKey(V1_EMPTY_NAME) && templates.size() == 1) {
            PluginTemplate template = templates.values().iterator().next();
            Class<?> componentClass = classes.values().iterator().next();
            return Collections.singletonMap(
                    template.resource().stringValue(),
                    new PluginV1Holder(template, componentClass));
        }
        Map<String, PluginHolder> result = new HashMap<>();
        for (Map.Entry<String, Class<?>> classEntry : classes.entrySet()) {
            PluginTemplate template = templates.get(classEntry.getKey());
            result.put(classEntry.getKey(),
                    new PluginV1Holder(template, classEntry.getValue()));
        }
        return result;
    }

}
