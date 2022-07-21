package com.linkedpipes.etl.executor.plugin.osgi;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.library.template.plugin.PluginException;
import com.linkedpipes.etl.library.template.plugin.PluginTemplateFacade;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.plugin.api.v2.component.Component;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class OsgiPluginService {

    private static final Logger LOG =
            LoggerFactory.getLogger(OsgiPluginService.class);

    private Framework framework;

    private final List<Bundle> libraries = new LinkedList<>();

    private final Map<String, Bundle> bundles = new HashMap<>();

    public void startService(File storageDirectory) throws ExecutorException {
        Map<String, String> config = new HashMap<>();
        config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                OsgiPackageList.EXPORT_PACKAGE_LIST);
        config.put(Constants.FRAMEWORK_STORAGE,
                storageDirectory.getAbsolutePath());
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN,
                Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        FrameworkFactory frameworkFactory
                = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        framework = frameworkFactory.newFramework(config);
        try {
            framework.start();
        } catch (BundleException ex) {
            throw new ExecutorException("Can't start framework!", ex);
        }
    }

    public void stopService() {
        if (framework == null) {
            return;
        }
        LOG.trace("Closing ...");
        try {
            framework.stop();
            framework.waitForStop(0);
        } catch (BundleException ex) {
            LOG.error("Can't stop OSGI framework.", ex);
        } catch (InterruptedException ex) {
            LOG.error("Interrupted when waiting for framework to stop.", ex);
        }
        LOG.trace("Closing ... done");
    }

    public void loadLibraries(File directory) throws ExecutorException {
        LOG.debug("Loading libraries from '{}'.", directory.getAbsolutePath());
        BundleContext context = framework.getBundleContext();
        List<File> files = listDirectory(directory);
        for (File file : files) {
            if (!file.getPath().endsWith(".jar")) {
                continue;
            }
            try {
                String path = file.toURI().toString();
                libraries.add(context.installBundle(path));
            } catch (BundleException ex) {
                LOG.error("Can't load bundle '{}'.", file, ex);
                throw new ExecutorException(file.toString(), ex);
            }
        }
        // Start bundles.
        for (Bundle library : libraries) {
            startBundle(library);
        }
    }

    private List<File> listDirectory(File root) {
        File[] files = root.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        List<File> result = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                result.add(file);
            } else if (file.isDirectory()) {
                result.addAll(listDirectory(file));
            }
        }
        return result;
    }

    private void startBundle(Bundle bundle) throws ExecutorException {
        try {
            bundle.start();
        } catch (BundleException ex) {
            LOG.error("Can't start bundle '{}'.\n{}",
                    bundle.getLocation(), ex.getMessage());
            throw new ExecutorException(
                    "Can't start bundle, see logs for more details.");
        }
    }

    public void loadPlugins(File directory) throws ExecutorException {
        LOG.debug("Loading plugins from '{}'.", directory.getAbsolutePath());
        List<File> files = listDirectory(directory);
        int failedLoadings = 0;
        for (File file : files) {
            if (!file.getPath().endsWith(".jar")) {
                continue;
            }
            try {
                loadPlugin(file);
            } catch (ExecutorException ex) {
                ++failedLoadings;
            }
        }
        if (failedLoadings > 0) {
            throw new ExecutorException(
                    "Failed to load '{}' bundles out of '{}'. " +
                            "see logs for more details.",
                    failedLoadings, files.size());
        }
        LOG.debug("Loaded {} component bundles.", bundles.size());
    }

    private void loadPlugin(File file) throws ExecutorException {
        JavaPlugin plugin;
        try {
            plugin = PluginTemplateFacade.loadJavaFile(file);
        } catch (PluginException ex) {
            LOG.error("Can't load component.", ex);
            return;
        }
        //
        Bundle bundle = installComponent(file.toURI().toString());
        startBundle(bundle);
        Map<String, Class<?>> classes =
                OsgiClassLoader.load(bundle.getBundleContext());
        // TODO: As of now we support only one class per bundle.

        if (plugin.templates().size() != 1) {
            throw new ExecutorException(
                    "Invalid number of plugins detected for '{}'", file);
        }
        if (classes.size() != 1) {
            throw new ExecutorException(
                    "Invalid number components classes for '{}'", file);
        }
        String iri = plugin.iri().stringValue();
        bundles.put(iri, bundle);
    }

    private Bundle installComponent(String path) throws ExecutorException {
        Bundle bundle;
        try {
            bundle = framework.getBundleContext().installBundle(path);
        } catch (BundleException ex) {
            throw new ExecutorException(
                    "Can't load bundle from '{}'.", path, ex);
        }
        return bundle;
    }

    public List<PipelineExecutionObserver> getPipelineListeners()
            throws ExecutorException {
        return getServices(PipelineExecutionObserver.class);
    }

    private <T> List<T> getServices(Class<T> serviceClass)
            throws ExecutorException {
        BundleContext context = framework.getBundleContext();
        try {
            return context.getServiceReferences(serviceClass, null)
                    .stream()
                    .map(context::getService)
                    .collect(Collectors.toList());
        } catch (InvalidSyntaxException ex) {
            throw new ExecutorException("Can't get service list!", ex);
        }
    }

    public List<DataUnitFactory> getDataUnitFactories()
            throws ExecutorException {
        return getServices(DataUnitFactory.class);
    }

    public BundleContext getComponentBundleContext(String iri)
            throws ExecutorException {
        if (!bundles.containsKey(iri)) {
            throw new ExecutorException(
                    "Missing bundle for '{}'.", iri);
        }
        return bundles.get(iri).getBundleContext();
    }


}
