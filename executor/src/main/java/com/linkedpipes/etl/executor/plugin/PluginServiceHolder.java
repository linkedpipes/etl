package com.linkedpipes.etl.executor.plugin;

import com.linkedpipes.etl.executor.ConfigurationHolder;
import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.plugin.v1.PluginV1Instance;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.plugin.osgi.OsgiPluginService;
import com.linkedpipes.etl.executor.plugin.v1.PluginV1Holder;
import com.linkedpipes.etl.executor.rdf.RdfSourceWrap;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
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

    public PluginV1Instance getComponent(Pipeline pipeline, String component)
            throws ExecutorException {
        String template = getComponentTemplateIri(pipeline, component);
        checkIfBundleIsAllowed(template);
        PluginHolder pluginHolder = osgi.getPlugin(template);
        if (pluginHolder instanceof PluginV1Holder v1Holder) {
            return v1Holder.createInstance(pipeline, component);
        }
        throw new ExecutorException("Unknown plugin holder '{}'.",
                pluginHolder.getClass().getName());
    }

    private String getComponentTemplateIri(Pipeline pipeline, String component)
            throws ExecutorException {
        String query = getComponentTemplateQuery(
                component, pipeline.getPipelineGraph());
        try {
            return RdfUtils.sparqlSelectSingle(
                    pipeline.getSource(), query, "iri");
        } catch (RdfUtilsException ex) {
            throw new ExecutorException("Can't load component jar path.", ex);
        }
    }

    private static String getComponentTemplateQuery(
            String component, String graph) {
        return "SELECT ?iri WHERE { GRAPH <" + graph + "> { "
                + " <" + component + "> <" + LP_PIPELINE.HAS_TEMPLATE
                + "> ?iri }}";
    }

    private void checkIfBundleIsAllowed(String iri) throws ExecutorException {
        for (String pattern : configuration.getBannedJarPatterns()) {
            if (iri.matches(pattern)) {
                throw new BannedComponent(iri, pattern);
            }
        }
    }

    public ManageableDataUnit getDataUnit(Pipeline pipeline, String subject)
            throws ExecutorException {
        RdfSourceWrap source = new RdfSourceWrap(
                pipeline.getSource(), pipeline.getPipelineGraph());
        for (DataUnitFactory factory : osgi.getDataUnitFactories()) {
            try {
                ManageableDataUnit dataUnit = factory.create(
                        subject, pipeline.getPipelineGraph(), source);
                if (dataUnit != null) {
                    return dataUnit;
                }
            } catch (LpException ex) {
                LOG.error("Can't create data unit '{}' with '{}'.",
                        subject, factory.getClass().getName(), ex);
            }
        }
        throw new ExecutorException(
                "No factory can instantiate required data unit '{}'.",
                subject);
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
