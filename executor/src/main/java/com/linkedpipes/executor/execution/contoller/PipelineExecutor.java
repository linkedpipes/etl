package com.linkedpipes.executor.execution.contoller;

import com.linkedpipes.executor.execution.util.RequirementProcessor;
import com.linkedpipes.executor.execution.util.LogerUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.linkedpipes.commons.entities.executor.DebugStructure;
import com.linkedpipes.commons.entities.executor.DebugStructure.DataUnit;
import com.linkedpipes.executor.Configuration;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.plugin.ExecutionListener;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.commons.entities.executor.ExecutionStatus;
import com.linkedpipes.commons.entities.executor.Labels;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration;
import com.linkedpipes.executor.execution.entity.event.ExecutionBeginImpl;
import com.linkedpipes.executor.execution.entity.event.ExecutionCancelled;
import com.linkedpipes.executor.execution.entity.event.ExecutionEndImpl;
import com.linkedpipes.executor.execution.entity.event.ExecutionFailed;
import com.linkedpipes.executor.execution.entity.event.StopExecution;
import com.linkedpipes.executor.logging.boundary.MdcValue;
import com.linkedpipes.executor.module.boundary.ModuleFacade;
import com.linkedpipes.executor.rdf.boundary.DefinitionStorage;
import com.linkedpipes.executor.rdf.boundary.MessageStorage;
import com.linkedpipes.executor.rdf.boundary.RdfOperationFailed;
import com.linkedpipes.etl.utils.core.entity.EntityLoader;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration.Component.ExecutionType;
import org.apache.commons.io.FilenameUtils;

/**
 * Used to execute pipeline.
 *
 * @author Škoda Petr
 */
public final class PipelineExecutor implements MessageStorage.MessageListener {

    /**
     * Used to store clean-up actions.
     */
    @FunctionalInterface
    private interface AfterExecution {

        public void execute() throws Exception;

    }

    /**
     * Used to jump out of execution function.
     */
    private static class ExecutionFailedException extends Exception {

    }

    private static final String QUERY_PIPELINE_RESOURCE = ""
            + "SELECT ?pipeline ?graph WHERE {\n"
            + "  GRAPH ?graph { ?pipeline a <" + LINKEDPIPES.PIPELINE + "> . }\n"
            + "}";

    private static final String QUERY_LABELS = ""
            + "SELECT ?uri ?label ?lang WHERE {\n"
            + "  ?uri <http://www.w3.org/2004/02/skos/core#prefLabel> ?label .\n"
            + "BIND(LANG(?label) AS ?lang)\n"
            + "} VALUES (?type) {\n"
            + " (<" + LINKEDPIPES.PIPELINE + ">) \n"
            + " (<" + LINKEDPIPES.COMPONENT + ">) \n"
            + "}";

    private static final Logger LOG = LoggerFactory.getLogger(PipelineExecutor.class);

    private final ModuleFacade moduleFacade;

    /**
     * Methods stored here are called after the execution ends. This is used to dynamically add methods that shall be
     * executed on clean-up. Method are executed in reverse order to insertion.
     *
     * Method stored in this function must not throw!
     */
    private final Stack<AfterExecution> afterExecution = new Stack<>();

    /**
     * If true then execution should be canceled. If set to true then cancel message is broadcasted, if component is
     * executed then cancel method is called and the execution is stopped as soon as possible.
     *
     * The {@link #stopExecution} property is not set to true.
     *
     * All this happen in the main execute method, to make it easier.
     */
    private boolean cancelExecution = false;

    /**
     * If true then execution should stop as soon as possible. While cancel is used to cancel on user request stop is
     * used to cancel for inner reason (failures).
     */
    private boolean stopExecution = false;

    private final String executionUri;

    private final String executionId;

    private DefinitionStorage definitionStorage;

    private MessageStorage messageStorage;

    private ResourceManager resourceManager;

    private Appender<ILoggingEvent> pipelineAppender;

    private final List<Appender<ILoggingEvent>> componentAppenders = new ArrayList<>(2);

    private CancelAwareContextImpl executorApiContext;

    private PipelineConfiguration pipeline;

    private final StatusKeeper statusKeeper;

    /**
     * Debug structure used to write debug data at the end of execution.
     */
    private final DebugStructure debugStructure = new DebugStructure();

    public PipelineExecutor(ModuleFacade moduleFacade, Configuration configuration, String executionId) {
        this.moduleFacade = moduleFacade;
        // TODO Replace wtih property from configuration?
        this.executionUri = configuration.getExecutionPrefix() + executionId;
        this.executionId = executionId;
        statusKeeper = new StatusKeeper(executionId, this.executionUri);
    }

    /**
     *
     * @param executionDirectory Execution directory.
     * @return False if initialization failed.
     */
    public boolean initialize(File executionDirectory) {
        // Prepare resource manager.
        resourceManager = new ResourceManager(executionDirectory);
        statusKeeper.setResourceManager(resourceManager);
        return true;
    }

    /**
     * Execute the pipeline.
     *
     */
    public void execute() {
        MDC.put(MdcValue.SYSTEM_FLAG, null);
        pipelineAppender = LogerUtils.createSystemAppender(resourceManager.getSystemLogFile());
        //
        try {
            statusKeeper.pipelineStarts();
            initializeContext();
            loadPipelineConfiguration();
            writeLabelsFile();
            innerExecute();
        } catch (ExecutionFailedException ex) {
            LOG.error("Execution failed!", ex);
        } catch (Throwable ex) {
            LOG.error("Execution for Throwable!", ex);
            statusKeeper.throwable(ex);
        }
        LOG.info("Executing actions after execution");
        // Execute after-executors ~ clean up.
        while (!afterExecution.isEmpty()) {
            try {
                afterExecution.pop().execute();
            } catch (Throwable ex) {
                LOG.error("After execution function failed!", ex);
            }
        }
        //
        LOG.info("Saving debug data.");
        final ObjectMapper json = new ObjectMapper();
        final File statusFile = resourceManager.getDebugFile();
        try {
            json.writerWithDefaultPrettyPrinter().writeValue(statusFile, debugStructure);
        } catch (IOException ex) {
            LOG.error("Can't write debug file!", ex);
        }
        //
        statusKeeper.pipelineEnd();
        LOG.info("Execution completed.");
        // Logging.
        LogerUtils.destroyAppenders(Arrays.asList(pipelineAppender));
        MDC.remove(MdcValue.SYSTEM_FLAG);
    }

    protected void initializeContext() throws ExecutionFailedException, ModuleFacade.ModuleException {
        final File definitionDir = resourceManager.getDefinitionDirectory();
        // There should be a definition file.
        final File[] files = definitionDir.listFiles((File dir, String name) -> name.contains("definition."));
        if (files.length == 0) {
            // Missing definition file.
            throw reportInitializationFailure("Missing definition file!");
        }
        final File definitionFile = files[0];
        final String definitionFileExtension = FilenameUtils.getExtension(definitionFile.toString());

        // Prepare and load definition data unit.
        try {
            definitionStorage = new DefinitionStorage(resourceManager.getDefinitionRepositryDir());
            switch (definitionFileExtension) {
                case "jsonld":
                    definitionStorage.load(definitionFile, RDFFormat.JSONLD);
                    break;
                case "trig":
                    definitionStorage.load(definitionFile, RDFFormat.TRIG);
                    break;
                case "ttl":
                    definitionStorage.load(definitionFile, RDFFormat.TURTLE);
                    break;
                default:
                    throw reportInitializationFailure("Invalid extension of a definition file: %s",
                            definitionFileExtension);
            }

            afterExecution.add(() -> {
                definitionStorage.store(resourceManager.getDefinitionDumpFile(), RDFFormat.JSONLD);
                definitionStorage.close();
            });
        } catch (RepositoryException ex) {
            throw reportInitializationFailure(ex, "Can't create repository for pipeline definition!");
        } catch (RdfOperationFailed ex) {
            throw reportInitializationFailure(ex, "Can't load definition file!");
        }
        // Prepare message data unit.
        try {
            messageStorage = new MessageStorage(resourceManager.getMessageRepositryDir(), executionUri);
            afterExecution.add(() -> {
                messageStorage.store(resourceManager.getMessageDumpFile(), RDFFormat.JSONLD);
                messageStorage.close();
            });
        } catch (RepositoryException ex) {
            throw reportInitializationFailure(ex, "Can't create repository for execution messages!");
        }
        // Add message listeners.
        messageStorage.addListener(this);
        messageStorage.addListener(statusKeeper);
        moduleFacade.getMessageListeners().forEach((listener) -> messageStorage.addListener((message) -> {
            listener.onMessage(message);
        }));
    }

    protected void loadPipelineConfiguration() throws ExecutionFailedException {
        try {
            final List<Map<String, String>> resourceList = definitionStorage.executeSelect(
                    QUERY_PIPELINE_RESOURCE);
            if (resourceList.size() != 1) {
                throw reportInitializationFailure("Wrong number of pipeline definitions: %d", resourceList.size());
            }
            // Set definition graph.
            definitionStorage.setDefinitionGraphUri(resourceList.get(0).get("graph"));
            // Create configuraiton class.
            pipeline = new PipelineConfiguration(resourceList.get(0).get("pipeline"));
            EntityLoader.load(definitionStorage, resourceList.get(0).get("pipeline"),
                    definitionStorage.getDefinitionGraphUri(), pipeline);
        } catch (SparqlSelect.QueryException | EntityLoader.LoadingFailed ex) {
            throw reportInitializationFailure(ex, "Can't load pipeline definition!");
        }
        // ...
        statusKeeper.setPipeline(pipeline);
    }

    protected void innerExecute() throws ExecutionFailedException, ModuleFacade.ModuleException {
        // Process requirements.
        try {
            RequirementProcessor.handle(pipeline, resourceManager, definitionStorage);
        } catch (SparqlSelect.QueryException | RdfOperationFailed ex) {
            throw reportInitializationFailure(ex, "Can't modify pipeline execution!");
        } catch (RequirementProcessor.InvalidRequirement ex) {
            throw reportInitializationFailure(ex, "Can't load requirement!");
        }
        // Notify plugins about new pipeline execution.
        executorApiContext = new CancelAwareContextImpl(messageStorage);
        for (final ExecutionListener plugin : moduleFacade.getExecutionListeners()) {
            try {
                plugin.onExecutionBegin(definitionStorage, pipeline.getUri(), definitionStorage.getDefinitionGraphUri(),
                        executorApiContext);
                afterExecution.add(() -> {
                    plugin.onExecutionEnd();
                });
            } catch (ExecutionListener.InitializationFailure ex) {
                throw reportInitializationFailure(ex, "Can't initialize plugin: '%s' !",
                        plugin.getClass().getSimpleName());
            }
        }
        // Prepare components and data units instances.
        final Map<String, Component> componentInstances = loadComponents();
        final Map<String, ManagableDataUnit> dataUnitInstances = loadDataUnits();
        afterExecution.push(() -> {
            for (ManagableDataUnit dataUnit : dataUnitInstances.values()) {
                DebugStructure.DataUnit dataUnitInfo = null;
                for (DataUnit item : debugStructure.getDataUnits()) {
                    if (item.getIri().equals(dataUnit.getResourceUri())) {
                        dataUnitInfo = item;
                        break;
                    }
                }
                // Save data.
                final File saveFile = resourceManager.getWorkingDir("save-");
                try {
                    dataUnit.save(saveFile);
                } catch (Exception ex) {
                    LOG.error("Can't save data unit.", ex);
                }

                if (dataUnitInfo == null) {
                    // Missing data unit info.
                    LOG.info("Missing data unit info for: {} data saved to: {}", dataUnit.getResourceUri(), saveFile);
                } else {
                    PipelineConfiguration.Component component = pipeline.getComponent(dataUnitInfo.getComponentUri());
                    if (component != null && component.getExecutionType() != ExecutionType.SKIP) {
                        dataUnitInfo.setSaveDirectory(saveFile.getPath());
                        //
                        try {
                            final File debugFile = resourceManager.getWorkingDir("debug-");
                            final List<File> debugFiles = dataUnit.dumpContent(debugFile);
                            dataUnitInfo.getDebugDirectories().add(debugFile.getPath());
                            for (File file : debugFiles) {
                                dataUnitInfo.getDebugDirectories().add(file.getPath());
                            }
                        } catch (Exception ex) {
                            LOG.error("Can't dump content of data unit.", ex);
                        }
                    }
                }
                //
                try {
                    dataUnit.close();
                } catch (Exception ex) {
                    LOG.error("Can't close data unit.", ex);
                }
            }
        });
        messageStorage.publish(new ExecutionBeginImpl(pipeline));
        // Set initial states to components, so it does not look like some DPUs shall be executed while
        // they should not be.
        for (PipelineConfiguration.Component component : pipeline.getComponents()) {
            if (component.getExecutionType() == ExecutionType.SKIP) {
                statusKeeper.componentSkipped(component.getUri());
            } else if (component.getExecutionType() == ExecutionType.MAPPED) {
                statusKeeper.componentMapped(component.getUri());
            }
        }
        // Used to check if the execution has been cancelled.
        boolean cancelRequestProcessed = false;
        for (PipelineConfiguration.Component component : pipeline.getComponents()) {
            //
            if (component.getExecutionType() == ExecutionType.SKIP) {
                continue;
            }
            //
            final Component componentInstance = componentInstances.get(component.getUri());
            // Check for cancel or stop.
            if (cancelExecution) {
                if (!cancelRequestProcessed) {
                    messageStorage.publish(new ExecutionCancelled());
                }
                break;
            }
            if (stopExecution) {
                break;
            }
            final ComponentExecutor componentExecutor = new ComponentExecutor(executorApiContext,
                    definitionStorage, componentInstance, component, dataUnitInstances);
            // Create a componenet thread and run it.
            // We done this as componenet may fail and kill the thread, we don't
            final Thread thread = new Thread(componentExecutor, component.getLabel());
            // Prepare logger.
            componentAppenders.add(LogerUtils.createComponentAppenders(
                    resourceManager.getComponentLogFile(component.getExecutionId()), component,
                    componentInstance));
            // Start execution and wait till it ends.
            thread.start();
            while (thread.isAlive()) {
                try {
                    thread.join(1000);
                } catch (InterruptedException ex) {
                    // This should not happen, as cancel method should be called to cancel the execution.
                    LOG.warn("Interrupt ignored!", ex);
                }
                // Check for cancel.
                if (cancelExecution && !cancelRequestProcessed) {
                    executorApiContext.cancel();
                    messageStorage.publish(new ExecutionCancelled());
                    cancelRequestProcessed = true;
                }
            }
            // Check for component failure.
            if (!componentExecutor.isTerminationFlag()) {
                messageStorage.publish(ExecutionFailed.executionFailed("Component thread was not properly terminated!"));
            }
            // Destory logger.
            LogerUtils.destroyAppenders(componentAppenders);
        }
        messageStorage.publish(new ExecutionEndImpl(pipeline));
    }

    /**
     * Unpack given file to the given directory.
     *
     * @param source
     * @param target
     * @throws IOException
     */
    protected void unzipFile(File source, File target) throws IOException {
        try (ZipFile zipFile = new ZipFile(source)) {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                File entryDestination = new File(target, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    final InputStream in = zipFile.getInputStream(entry);
                    try (OutputStream out = new FileOutputStream(entryDestination)) {
                        IOUtils.copy(in, out);
                        IOUtils.closeQuietly(in);
                    }
                }
            }
        }
    }

    /**
     * Load all components based on
     *
     * @return
     * @throws ExecutionFailedException
     */
    protected Map<String, Component> loadComponents() throws ExecutionFailedException {
        final Map<String, Component> result = new HashMap<>(pipeline.getComponents().size());
        for (PipelineConfiguration.Component component : pipeline.getComponents()) {
            try {
                result.put(component.getUri(),
                        moduleFacade.getComponent(definitionStorage, component.getUri()));
            } catch (ModuleFacade.ModuleException ex) {
                throw reportInitializationFailure(ex, "Can't load component: %s", component.getUri());
            }
        }
        return result;
    }

    /**
     * Load all used data units.
     *
     * @return
     * @throws ExecutionFailedException
     */
    protected Map<String, ManagableDataUnit> loadDataUnits() throws ExecutionFailedException {
        // We use default estimate component size * 3 for number of data units.
        final Map<String, ManagableDataUnit> result = new HashMap<>(pipeline.getComponents().size() * 3);
        for (PipelineConfiguration.Component component : pipeline.getComponents()) {
            for (PipelineConfiguration.DataUnit dataUnit : component.getDataUnits()) {
                // Store information about data units that will be executed.
                if (component.getExecutionType() == ExecutionType.EXECUTE
                        || pipeline.isDataUnitUsed(dataUnit.getUri())) {
                    final DebugStructure.DataUnit dataUnitInfo = new DataUnit();
                    // TODO: Temporary solution. Such information does hot nave to be stored.
                    dataUnitInfo.setIri(dataUnit.getUri());
                    dataUnitInfo.setUriFragment(dataUnit.getUriFragment());
                    dataUnitInfo.setComponentUri(component.getUri());
                    debugStructure.getDataUnits().add(dataUnitInfo);
                }
                //
                try {
                    result.put(dataUnit.getUri(),
                            moduleFacade.getDataUnit(definitionStorage, dataUnit.getUri()));
                } catch (ModuleFacade.ModuleException ex) {
                    throw reportInitializationFailure(ex, "Can't load data unit: %s", dataUnit.getUri());
                }
            }
        }
        return result;
    }

    @Override
    public void onMesssage(Event message) {
        // If we recieve any message then the message sender must be ready to be used.
        if (message instanceof ExecutionFailed) {
            // Broadcast StopExecution.
            messageStorage.publish(new StopExecution());
        } else if (message instanceof StopExecution) {
            stopExecution = true;
        }
    }

    protected ExecutionFailedException reportInitializationFailure(String message, Object... parameters) {
        message = String.format(message, parameters);
        LOG.error(message);
        if (messageStorage != null) {
            messageStorage.publish(ExecutionFailed.initializationFailed(message));
        }
        statusKeeper.initializationFailed(message, parameters);
        return new ExecutionFailedException();
    }

    protected ExecutionFailedException reportInitializationFailure(Throwable exception, String message,
            Object... parameters) {
        message = String.format(message, parameters);
        LOG.error(message, exception);
        if (messageStorage != null) {
            messageStorage.publish(ExecutionFailed.initializationFailed(message, exception));
        }
        statusKeeper.initializationFailed(exception, message, parameters);
        return new ExecutionFailedException();
    }

    /**
     * Write down file with all labels used in pipeline definition.
     *
     * @throws com.linkedpipes.executor.execution.contoller.PipelineExecutor.ExecutionFailedException
     */
    protected void writeLabelsFile() throws ExecutionFailedException {
        final Labels labelsMap = new Labels();
        //
        try {
            for (Map<String, String> item : definitionStorage.executeSelect(QUERY_LABELS)) {
                final String uri = item.get("uri");
                final String label = item.get("label");
                final String lang = item.get("lang");
                if (!labelsMap.getResources().containsKey(uri)) {
                    labelsMap.getResources().put(uri, new Labels.Resource());
                }
                labelsMap.getResources().get(uri).getLabels().put(lang, label);
            }

        } catch (SparqlSelect.QueryException ex) {
            LOG.info("Query:\n{}", QUERY_LABELS);
            throw reportInitializationFailure(ex, "Can't query for labels.");
        }
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(resourceManager.getLabelDumpFile(), labelsMap);
        } catch (IOException ex) {
            messageStorage.publish(ExecutionFailed.initializationFailed("Can't write label file.", ex));
            throw new ExecutionFailedException();
        }
    }

    /**
     * Cancel execution.
     */
    public void cancel() {
        cancelExecution = true;
        //context.getMessageSender().publish(new ExecutionCancelRequest());
    }

    public ExecutionStatus getExecutionStatus() {
        return statusKeeper.getStatus();
    }

    public MessageStorage getMessageStorage() {
        return messageStorage;
    }

}
