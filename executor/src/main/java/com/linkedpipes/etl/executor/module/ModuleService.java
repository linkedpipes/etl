package com.linkedpipes.etl.executor.module;

import com.linkedpipes.etl.executor.Configuration;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.api.v1.component.DefaultComponentFactory;
import com.linkedpipes.etl.executor.api.v1.component.ManageableComponent;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.rdf.RdfSourceWrap;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.plugin.loader.PluginJarFile;
import com.linkedpipes.plugin.loader.PluginLoader;
import com.linkedpipes.plugin.loader.PluginLoaderException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ModuleService implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG =
            LoggerFactory.getLogger(ModuleService.class);

    private static final String LP_PACKAGE =
            "com.linkedpipes.etl.executor.api.v1";

    private static final String EXPORT_PACKAGE_LIST = ""
            + "" // javax additional - FIND BUNDLE WITH THIS !
            + "javax.servlet;version=\"2.4.0\","
            + "javax.servlet.http;version=\"2.4.0\","
            + "javax.xml.bind;version=\"2.3.0\","
            + "javax.xml.bind.util;version=\"2.3.0\","
            + "javax.xml.bind.annotation;version=\"2.3.0\","
            + "javax.xml.bind.annotation.adapters;version=\"2.3.0\","
            + "javax.annotation;version=\"1.3.2\","
            + "" // slf4j
            + "org.slf4j;version=\"1.7.21\","
            + "org.slf4j.helpers;version=\"1.7.21\","
            + "org.slf4j.spi;version=\"1.7.21\","
            + "" // logback
            + "ch.qos.logback.classic;version=\"1.1.7\","
            + "ch.qos.logback.classic.joran;version=\"1.1.7\","
            + "ch.qos.logback.core;version=\"1.1.7\","
            + "ch.qos.logback.core.joran.action;version=\"1.1.7\","
            + "ch.qos.logback.core.joran.spi;version=\"1.1.7\","
            + "ch.qos.logback.core.rolling;version=\"1.1.7\","
            + "ch.qos.logback.core.util;version=\"1.1.7\","
            + "" // log4j
            + "org.apache.log4j;version=\"1.7.18\","
            + "org.apache.log4j.helpers;version=\"1.7.18\","
            + "org.apache.log4j.api;version=\"1.7.18\","
            + "org.apache.log4j.xml;version=\"1.7.18\","
            + "" // core API
            + LP_PACKAGE + ";version=\"0.0.0\","
            + LP_PACKAGE + ".component;version=\"0.0.0\","
            + LP_PACKAGE + ".component.task;version=\"0.0.0\","
            + LP_PACKAGE + ".dataunit;version=\"0.0.0\","
            + LP_PACKAGE + ".event;version=\"0.0.0\","
            + LP_PACKAGE + ".rdf;version=\"0.0.0\","
            + LP_PACKAGE + ".rdf.model;version=\"0.0.0\","
            + LP_PACKAGE + ".rdf.pojo;version=\"0.0.0\","
            + LP_PACKAGE + ".service;version=\"0.0.0\","
            + LP_PACKAGE + ".report;version=\"0.0.0\","
            + LP_PACKAGE + ".vocabulary;version=\"0.0.0\","
            + "com.linkedpipes.etl.rdf.utils;version=\"0.0.0\"";

    private Framework framework;

    private final List<Bundle> libraries = new LinkedList<>();

    private final Map<String, Bundle> components = new HashMap<>();

    private Configuration configuration;

    private AbstractApplicationContext springContext;

    @Autowired
    public ModuleService(
            Configuration configuration,
            AbstractApplicationContext appContext) {
        this.configuration = configuration;
        this.springContext = appContext;
    }

    public Collection<PipelineExecutionObserver> getPipelineListeners()
            throws ModuleException {
        return getServices(PipelineExecutionObserver.class);
    }

    public ManageableComponent getComponent(Pipeline pipeline, String component)
            throws ModuleException {
        String jar = getComponentJar(pipeline, component);
        BundleContext componentContext =
                getComponentBundle(jar).getBundleContext();
        ManageableComponent manageableComponent;
        RdfSourceWrap source = wrapPipeline(pipeline);
        DefaultComponentFactory factory = new DefaultComponentFactory();
        try {
            manageableComponent = factory.create(
                    component, pipeline.getPipelineGraph(),
                    source, componentContext);
        } catch (LpException ex) {
            throw new ModuleException(
                    "Can't create component from a bundle.", ex);
        }
        if (manageableComponent == null) {
            throw new ModuleException("Can't load bundle: {}", jar);
        }
        checkIfBundleIsAllowed(jar);
        return manageableComponent;
    }

    private String getComponentJar(Pipeline pipeline, String component)
            throws ModuleException {
        String query = getJarPathQuery(component, pipeline.getPipelineGraph());
        try {
            return RdfUtils.sparqlSelectSingle(
                    pipeline.getSource(), query, "jar");
        } catch (RdfUtilsException ex) {
            throw new ModuleException("Can't load component jar path.", ex);
        }
    }

    private static String getJarPathQuery(String component, String graph) {
        return "SELECT ?jar WHERE { GRAPH <" + graph + "> { "
                + " <" + component + "> <" + LP_PIPELINE.HAS_JAR_URL
                + "> ?jar }}";
    }

    private String getBundleIri(String path) throws ModuleException {
        try {
            return configuration.getStorageAddress()
                    + "/api/v1/jars/file?iri="
                    + URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new ModuleException("Invalid encoding!", ex);
        }
    }

    private Bundle getComponentBundle(String iri) throws ModuleException {
        if (components.containsKey(iri)) {
            return components.get(iri);
        }
        String path = getBundleIri(iri);
        Bundle bundle = installComponentBundle(path);
        try {
            bundle.start();
        } catch (BundleException ex) {
            throw new ModuleException("Can't start bundle!", ex);
        }
        components.put(iri, bundle);
        return bundle;
    }

    private Bundle installComponentBundle(String path) throws ModuleException {
        Bundle bundle;
        try {
            LOG.info("Loading jar file from: {}", path);
            bundle = framework.getBundleContext().installBundle(path);
        } catch (BundleException ex) {
            throw new ModuleException("Can't load bundle for: {}", path, ex);
        }
        return bundle;
    }

    private RdfSourceWrap wrapPipeline(Pipeline pipeline) {
        return new RdfSourceWrap(
                pipeline.getSource(), pipeline.getPipelineGraph());
    }

    private void checkIfBundleIsAllowed(String iri) throws ModuleException {
        for (String pattern : configuration.getBannedJarPatterns()) {
            if (iri.matches(pattern)) {
                throw new BannedComponent(iri, pattern);
            }
        }
    }

    public ManageableDataUnit getDataUnit(Pipeline pipeline, String subject)
            throws ModuleException {
        RdfSourceWrap source = wrapPipeline(pipeline);
        for (DataUnitFactory factory : getServices(DataUnitFactory.class)) {
            try {
                ManageableDataUnit dataUnit = factory.create(
                        subject, pipeline.getPipelineGraph(), source);
                if (dataUnit != null) {
                    return dataUnit;
                }
            } catch (LpException ex) {
                LOG.error("Can't create data unit '{}' with: {}",
                        subject, factory.getClass().getName(), ex);
            }
        }
        throw new ModuleException(
                "No factory can instantiate required data unit.");
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextStartedEvent) {
            start();
        } else if (event instanceof ContextStoppedEvent) {
            // This is Felix dependant:
            // FelixDispatchQueue is thread used by felix, it's created as non
            // daemon. For this reason we need to manually stop the framework
            // and not wait on OnDestroy call.
            stop();
        }
    }

    private <T> Collection<T> getServices(Class<T> clazz)
            throws ModuleException {
        BundleContext context = framework.getBundleContext();
        try {
            return context.getServiceReferences(clazz, null)
                    .stream()
                    .map(reference -> context.getService(reference))
                    .collect(Collectors.toList());
        } catch (InvalidSyntaxException ex) {
            throw new ModuleException("Can't get service list!", ex);
        }
    }

    private void start() {
        Map<String, String> config = new HashMap<>();
        config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                EXPORT_PACKAGE_LIST);
        config.put(Constants.FRAMEWORK_STORAGE,
                configuration.getOsgiStorageDirectory());
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN,
                Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

        FrameworkFactory frameworkFactory
                = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        framework = frameworkFactory.newFramework(config);
        try {
            framework.start();
        } catch (BundleException ex) {
            springContext.stop();
            throw new RuntimeException("Can't start framework!", ex);
        }

        loadLibraries();
        loadComponents();
    }

    private void loadLibraries() {
        BundleContext context = framework.getBundleContext();
        scanDirectory(configuration.getOsgiLibDirectory(), (file) -> {
            if (file.getPath().endsWith(".jar")) {
                LOG.info("Loading library: {}", file);
                try {
                    String path = file.toURI().toString();
                    libraries.add(context.installBundle(path));
                } catch (BundleException ex) {
                    LOG.error("Can't load bundle: {}", file, ex);
                    springContext.stop();
                    throw new RuntimeException(file.toString(), ex);
                }
            }
        });
        // Start bundles.
        libraries.forEach((bundle) -> startBundle(bundle));
    }

    private void scanDirectory(File root, Consumer<File> consumer) {
        File[] files = root.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                consumer.accept(file);
            } else if (file.isDirectory()) {
                scanDirectory(file, consumer);
            }
        }
    }

    private void startBundle(Bundle bundle) {
        try {
            bundle.start();
        } catch (BundleException ex) {
            LOG.error("Can't start bundle: {}", bundle.getSymbolicName(), ex);
            springContext.stop();
            throw new RuntimeException(
                    "Can not start bundle, see logs for more details.");
        }
    }

    private void stop() {
        LOG.debug("Closing ...");
        if (framework != null) {
            stopFramework();
        }
        LOG.debug("Closing ... done");
    }

    private void stopFramework() {
        try {
            framework.stop();
            framework.waitForStop(0);
        } catch (BundleException ex) {
            LOG.error("Can't stop OSGI framework.", ex);
        } catch (InterruptedException ex) {
            LOG.error("Interrupted when waiting for framework to stop.", ex);
        }
    }

    private void loadComponents() {
        PluginLoader loader = new PluginLoader();
        scanDirectory(configuration.getOsgiComponentDirectory(), (file) -> {
            if (!file.getPath().endsWith(".jar")) {
                return;
            }
            List<PluginJarFile> plugins;
            try {
                plugins = loader.loadReferences(file);
            } catch (PluginLoaderException ex) {
                LOG.error("Can't load component.", ex);
                return;
            }
            Bundle bundle;
            try {
                bundle = installComponentBundle(file.toURI().toString());
            } catch (ModuleException ex) {
                LOG.error("Can't load component bundle.", ex);
                return;
            }
            for (PluginJarFile plugin : plugins) {
                LOG.info(
                        "Loaded component '{}' from '{}'",
                        plugin.getIri(), plugin.getJar());
                // As of now we store under JAR iri as a single
                // JAr file can contain only one component.
                components.put(plugin.getJar(), bundle);
            }
        });
        // Start bundles.
        LOG.info("Starting component bundles ...");
        components.values().forEach((bundle) -> {
            try {
                bundle.start();
            } catch (BundleException ex) {
                LOG.error("Can't start component bundle: {}",
                        bundle.getSymbolicName(), ex);
            }
        });
        LOG.info("Starting component bundles ... done");
    }

}
