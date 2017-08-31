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
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import org.osgi.framework.*;
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
import java.util.*;
import java.util.function.Consumer;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ModuleFacade implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG =
            LoggerFactory.getLogger(ModuleFacade.class);

    private static final String EXPORT_PACKAGE_LIST = ""
            + "" // javax additional - FIND BUNDLE WITH THIS !
            + "javax.servlet;version=\"2.4.0\","
            + "javax.servlet.http;version=\"2.4.0\","
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
            + "com.linkedpipes.etl.executor.api.v1;version=\"0.0.0\","
            + "com.linkedpipes.etl.executor.api.v1.component;version=\"0.0.0\","
            + "com.linkedpipes.etl.executor.api.v1.component.task;version=\"0.0.0\","
            + "com.linkedpipes.etl.executor.api.v1.dataunit;version=\"0.0.0\","
            + "com.linkedpipes.etl.executor.api.v1.event;version=\"0.0.0\","
            + "com.linkedpipes.etl.executor.api.v1.rdf;version=\"0.0.0\","
            + "com.linkedpipes.etl.executor.api.v1.service;version=\"0.0.0\","
            + "com.linkedpipes.etl.executor.api.v1.report;version=\"0.0.0\","
            + "com.linkedpipes.etl.executor.api.v1.vocabulary;version=\"0.0.0\","
            + "com.linkedpipes.etl.rdf.utils;version=\"0.0.0\","
            + "com.linkedpipes.etl.rdf.utils.entity;version=\"0.0.0\","
            + "com.linkedpipes.etl.rdf.utils.model;version=\"0.0.0\","
            + "com.linkedpipes.etl.rdf.utils.pojo;version=\"0.0.0\","
            + "com.linkedpipes.etl.rdf.utils.vocabulary;version=\"0.0.0\"";

    private Framework framework;

    private final List<Bundle> libraries = new LinkedList<>();

    private final Map<String, Bundle> components = new HashMap<>();

    private Configuration configuration;

    private AbstractApplicationContext springContext;

    @Autowired
    public ModuleFacade(Configuration configuration,
            AbstractApplicationContext appContext) {
        this.configuration = configuration;
        this.springContext = appContext;
    }

    /**
     * Collection of all loaded execution listeners.
     *
     * @return
     */
    public Collection<PipelineExecutionObserver> getPipelineListeners()
            throws ModuleException {
        return getServices(PipelineExecutionObserver.class);
    }

    /**
     * Create and return component that matches given specification.
     *
     * @param pipeline
     * @param component
     * @return Never null.
     */
    public ManageableComponent getComponent(Pipeline pipeline, String component)
            throws ModuleException {
        // If custom ComponentFactory should be used, put here
        // similar code as for getDataUnit.
        final String jar;
        try {
            jar = RdfUtils.sparqlSelectSingle(pipeline.getSource(),
                    getJarPathQuery(component, pipeline.getPipelineGraph()),
                    "jar");
        } catch (RdfUtilsException ex) {
            throw new ModuleException("Can't load component jar path.", ex);
        }
        // Check if the JAR IRI is not banned.
        for (final String pattern : configuration.getBannedJarPatterns()) {
            if (jar.matches(pattern)) {
                throw new ModuleException(
                        "The required component file with IRI: {} , " +
                                "is banned by the configuration (pattern: {}).",
                        jar, pattern);
            }
        }
        //
        final BundleContext componentContext =
                getBundle(jar).getBundleContext();
        final DefaultComponentFactory factory = new DefaultComponentFactory();
        final ManageableComponent manageableComponent;
        try {
            manageableComponent = factory.create(component,
                    pipeline.getPipelineGraph(), pipeline.getSource(),
                    componentContext);
        } catch (LpException ex) {
            throw new ModuleException("Can't create component from bundle.",
                    ex);
        }
        if (manageableComponent == null) {
            throw new ModuleException("Can't load bundle: {}", jar);
        }
        return manageableComponent;
    }

    /**
     * Create and return manageable data unit that matches given specification.
     *
     * @param pipeline
     * @param subject
     * @return Does not return null!
     */
    public ManageableDataUnit getDataUnit(Pipeline pipeline, String subject)
            throws ModuleException {
        for (DataUnitFactory factory : getServices(DataUnitFactory.class)) {
            try {
                final ManageableDataUnit dataUnit = factory.create(
                        subject, pipeline.getPipelineGraph(),
                        pipeline.getSource());
                if (dataUnit != null) {
                    return dataUnit;
                }
            } catch (LpException ex) {
                LOG.error("Can't create data unit.", ex);
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

    /**
     * @param <T>
     * @param clazz
     * @return Services of given interface.
     */
    protected <T> Collection<T> getServices(Class<T> clazz)
            throws ModuleException {
        final BundleContext context = framework.getBundleContext();
        try {
            final Collection<ServiceReference<T>> references
                    = context.getServiceReferences(clazz, null);
            final List<T> serviceList = new ArrayList<>(references.size());
            for (ServiceReference<T> reference : references) {
                serviceList.add(context.getService(reference));
            }
            return serviceList;
        } catch (InvalidSyntaxException ex) {
            throw new ModuleException("Can't get service list!", ex);
        }
    }

    protected void start() {
        final FrameworkFactory frameworkFactory
                = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        final Map<String, String> config = new HashMap<>();
        config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                EXPORT_PACKAGE_LIST);
        config.put(Constants.FRAMEWORK_STORAGE,
                configuration.getOsgiStorageDirectory());
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN,
                Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        framework = frameworkFactory.newFramework(config);
        try {
            framework.start();
        } catch (BundleException ex) {
            springContext.stop();
            throw new RuntimeException("Can't start framework!", ex);
        }
        final BundleContext context = framework.getBundleContext();
        // Load libraries.
        scanDirectory(configuration.getOsgiLibDirectory(), (file) -> {
            if (file.getPath().endsWith(".jar")) {
                try {
                    libraries.add(context.installBundle(
                            file.toURI().toString()));
                } catch (BundleException ex) {
                    LOG.error("Can't load bundle: {}", file, ex);
                    springContext.stop();
                    throw new RuntimeException(file.toString(), ex);
                }
            }
        });
        // Start library bundles.
        libraries.forEach((bundle) -> {
            try {
                bundle.start();
            } catch (BundleException ex) {
                LOG.error("Can't start bundle: {}",
                        bundle.getSymbolicName(), ex);
                springContext.stop();
                throw new RuntimeException(ex);
            }
        });
    }

    protected void stop() {
        LOG.debug("Closing ...");
        if (framework != null) {
            try {
                framework.stop();
                framework.waitForStop(0);
            } catch (BundleException ex) {
                LOG.error("Can't stop OSGI framework.", ex);
            } catch (InterruptedException ex) {
                LOG.error("Interrupted when waiting for framework to closeRepository.",
                        ex);
            }
        }
        LOG.debug("Closing ... done");
    }

    /**
     * Recursively list files in given directory and it's subdirectories.
     *
     * @param root
     * @param consumer
     */
    protected void scanDirectory(File root, Consumer<File> consumer) {
        if (root.listFiles() == null) {
            return;
        }
        for (File file : root.listFiles()) {
            if (file.isFile()) {
                consumer.accept(file);
            } else if (file.isDirectory()) {
                scanDirectory(file, consumer);
            }
        }
    }

    protected static String getJarPathQuery(String component, String graph) {
        return "SELECT ?jar WHERE { GRAPH <" + graph + "> { " +
                " <" + component + "> <" + LP_PIPELINE.HAS_JAR_URL +
                "> ?jar }}";
    }

    /**
     * @param path
     * @return Never null.
     */
    protected Bundle getBundle(String path) throws ModuleException {
        if (!components.containsKey(path)) {
            // Ask storage component for the component.
            final String bundleIri;
            try {
                bundleIri = configuration.getStorageAddress() +
                        "/api/v1/jars/file?iri=" +
                        URLEncoder.encode(path, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new ModuleException("Invalid encoding!", ex);
            }
            LOG.info("Loading jar file from: {}", path);
            final Bundle bundle;
            try {
                bundle = framework.getBundleContext().installBundle(bundleIri);
            } catch (BundleException ex) {
                throw new ModuleException("Can't load bundle!", ex);
            }
            try {
                bundle.start();
            } catch (BundleException ex) {
                throw new ModuleException("Can't start bundle!", ex);
            }
            components.put(path, bundle);
        }
        return components.get(path);
    }

}
