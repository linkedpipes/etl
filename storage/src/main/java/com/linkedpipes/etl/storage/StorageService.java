package com.linkedpipes.etl.storage;

import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.assistant.AssistantService;
import com.linkedpipes.etl.storage.pipeline.PipelineEvents;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.pipeline.PipelineRepository;
import com.linkedpipes.etl.storage.pipeline.PipelineService;
import com.linkedpipes.etl.storage.pipeline.repository.PipelineRepositoryFactory;
import com.linkedpipes.etl.storage.plugin.JavaPluginService;
import com.linkedpipes.etl.storage.template.PluginTemplateService;
import com.linkedpipes.etl.storage.template.ReferenceTemplateService;
import com.linkedpipes.etl.storage.template.TemplateEvents;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.storage.template.TemplateRepository;
import com.linkedpipes.etl.storage.template.repository.TemplateRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent main service that wraps all storage functionality.
 */
@Service
public class StorageService {

    private static final Logger LOG =
            LoggerFactory.getLogger(StorageService.class);

    private static Configuration configuration;

    private final PipelineEvents pipelineEvents = new PipelineEvents();

    private final TemplateEvents templateEvents = new TemplateEvents();

    private JavaPluginService javaPluginService;

    private AssistantService assistantService;

    private TemplateRepository templateRepository;

    private PluginTemplateService pluginTemplateService;

    private ReferenceTemplateService referenceTemplateService;

    private TemplateFacade templateFacade;

    private PipelineRepository pipelineRepository;

    private PipelineService pipelineService;

    private PipelineFacade pipelineFacade;

    public StorageService() {
        // No operation here.
    }

    public static void setConfiguration(Configuration configuration) {
        StorageService.configuration = configuration;
    }

    @PostConstruct
    public void initialize() throws StorageException {
        LocalDateTime start = LocalDateTime.now();
        initializeJavaPluginService();
        initializeAssistantService();
        initializeTemplateRepository();
        initializeTemplateFacade();
        initializePipelineRepository();
        initializePipelineFacade();

        long seconds = Duration.between(start, LocalDateTime.now()).toSeconds();
        LOG.debug("Initialization done in {}s", seconds);
    }

    private void initializeJavaPluginService() {
        javaPluginService = new JavaPluginService(configuration);
        javaPluginService.initialize();
    }

    private void initializeAssistantService() {
        assistantService = new AssistantService();
        pipelineEvents.register(assistantService);
        templateEvents.register(assistantService);
    }

    private void initializeTemplateRepository() throws StorageException {
        List<PluginTemplate> templates = new ArrayList<>();
        for (JavaPlugin javaPlugin : javaPluginService.getJavaPlugins()) {
            templates.addAll(javaPlugin.templates());
        }

        TemplateRepositoryFactory factory =
                new TemplateRepositoryFactory(templates);

        File directory = configuration.getStorageDirectory();
        templateRepository = factory.create(directory);
    }

    private void initializeTemplateFacade() throws StorageException {
        pluginTemplateService = new PluginTemplateService(
                templateEvents, templateRepository);
        pluginTemplateService.initialize();
        //
        referenceTemplateService = new ReferenceTemplateService(
                templateEvents, templateRepository);
        referenceTemplateService.initialize();
        //
        templateFacade = new TemplateFacade(
                configuration,
                referenceTemplateService,
                pluginTemplateService,
                templateRepository);
    }

    private void initializePipelineRepository() throws StorageException {
        PipelineRepositoryFactory factory = new PipelineRepositoryFactory();
        File directory = new File(
                configuration.getStorageDirectory(), "pipelines");
        pipelineRepository = factory.create(
                directory, () -> templateFacade.getTemplateToPluginMap());
    }

    private void initializePipelineFacade() throws StorageException {
        pipelineService = new PipelineService(
                pipelineEvents, pipelineRepository, templateFacade);
        pipelineService.initialize();

        pipelineFacade = new PipelineFacade(
                configuration,
                pipelineService,
                pipelineRepository);
    }

    public AssistantService getAssistantService() {
        return assistantService;
    }

    public TemplateFacade getTemplateFacade() {
        return templateFacade;
    }

    public PipelineFacade getPipelineFacade() {
        return pipelineFacade;
    }

    public JavaPluginService getJavaPluginService() {
        return javaPluginService;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
