package com.linkedpipes.etl.executor.plugin;

import com.linkedpipes.etl.executor.ConfigurationHolder;
import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.plugin.v1.ComponentFactory;
import com.linkedpipes.etl.executor.plugin.v1.ComponentV1;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.plugin.osgi.OsgiPluginService;
import com.linkedpipes.etl.executor.rdf.RdfSourceWrap;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PluginServiceHolder
        implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG =
            LoggerFactory.getLogger(PluginServiceHolder.class);

    private final ConfigurationHolder configuration;

    private final AbstractApplicationContext springContext;

    private final OsgiPluginService osgi = new OsgiPluginService();

    public PluginServiceHolder(
            ConfigurationHolder configuration,
            AbstractApplicationContext springContext) {
        this.configuration = configuration;
        this.springContext = springContext;
    }

    public Collection<PipelineExecutionObserver> getPipelineListeners()
            throws ExecutorException {
        return osgi.getPipelineListeners();
    }

    public ComponentV1 getComponent(Pipeline pipeline, String component)
            throws PluginException {
        String jarUrl = getComponentJarUrl(pipeline, component);
        checkIfBundleIsAllowed(jarUrl);
        BundleContext componentContext;
        try {
            componentContext = osgi.getComponentBundleContext(jarUrl);
        } catch (ExecutorException ex) {
            throw new PluginException("Can't load bundle: {}", jarUrl, ex);
        }
        ComponentV1 result;
        ComponentFactory factory = new ComponentFactory();
        try {
            result = factory.create(
                    component, pipeline.getPipelineGraph(),
                    wrapPipeline(pipeline), componentContext);
        } catch (LpException ex) {
            throw new PluginException(
                    "Can't create component from a bundle.", ex);
        }
        if (result == null) {
            throw new PluginException("Can't load component from: {}", jarUrl);
        }
        return result;
    }

    private String getComponentJarUrl(Pipeline pipeline, String component)
            throws PluginException {
        String query = getJarUrlQuery(component, pipeline.getPipelineGraph());
        try {
            return RdfUtils.sparqlSelectSingle(
                    pipeline.getSource(), query, "jar");
        } catch (RdfUtilsException ex) {
            throw new PluginException("Can't load component jar path.", ex);
        }
    }

    private static String getJarUrlQuery(String component, String graph) {
        return "SELECT ?jar WHERE { GRAPH <" + graph + "> { "
                + " <" + component + "> <" + LP_PIPELINE.HAS_JAR_URL
                + "> ?jar }}";
    }

    private void checkIfBundleIsAllowed(String iri) throws PluginException {
        for (String pattern : configuration.getBannedJarPatterns()) {
            if (iri.matches(pattern)) {
                throw new BannedComponent(iri, pattern);
            }
        }
    }

    private RdfSourceWrap wrapPipeline(Pipeline pipeline) {
        return new RdfSourceWrap(
                pipeline.getSource(), pipeline.getPipelineGraph());
    }

    public ManageableDataUnit getDataUnit(Pipeline pipeline, String subject)
            throws ExecutorException {
        RdfSourceWrap source = wrapPipeline(pipeline);
        for (DataUnitFactory factory : osgi.getDataUnitFactories()) {
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
        throw new PluginException(
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

    private void start() {
        try {
            osgi.startService(configuration.getOsgiStorageDirectory());
            osgi.loadLibraries(configuration.getOsgiLibDirectory());
            osgi.loadPlugins(configuration.getOsgiComponentDirectory());
        } catch (Exception ex) {
            springContext.stop();
            throw new RuntimeException("Can't start OSGI!", ex);
        }
    }

    private void stop() {
        osgi.stopService();
    }

}
