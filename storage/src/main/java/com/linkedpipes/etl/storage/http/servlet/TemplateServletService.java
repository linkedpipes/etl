package com.linkedpipes.etl.storage.http.servlet;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.template.configuration.ConfigurationException;
import com.linkedpipes.etl.library.template.configuration.ConfigurationFacade;
import com.linkedpipes.etl.library.template.configuration.adapter.ConfigurationDescriptionToRdf;
import com.linkedpipes.etl.library.template.plugin.PluginException;
import com.linkedpipes.etl.library.template.plugin.PluginTemplateFacade;
import com.linkedpipes.etl.library.template.plugin.adapter.PluginTemplateToRdf;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.adapter.RdfToRawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.adapter.ReferenceTemplateToRdf;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.StorageService;
import com.linkedpipes.etl.storage.assistant.AssistantService;
import com.linkedpipes.etl.storage.assistant.model.PipelineInfo;
import com.linkedpipes.etl.storage.assistant.model.TemplateUseInfo;
import com.linkedpipes.etl.storage.plugin.JavaPluginService;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;

class TemplateServletService {

    private static final String TEMPLATE =
            "http://etl.linkedpipes.com/ontology/Template";

    private static final String PIPELINE =
            "http://linkedpipes.com/ontology/Pipeline";

    private static final String HAS_LABEL =
            "http://www.w3.org/2004/02/skos/core#prefLabel";

    private static final String HAS_USED_IN_PIPELINE =
            "http://etl.linkedpipes.com/ontology/usedIn";

    private static final String HAS_INSTANCE =
            "http://etl.linkedpipes.com/ontology/hasInstance";

    private final AssistantService assistantService;

    private final TemplateFacade templateFacade;

    private final JavaPluginService pluginService;

    public TemplateServletService(StorageService storageService) {
        this.assistantService = storageService.getAssistantService();
        this.templateFacade = storageService.getTemplateFacade();
        this.pluginService = storageService.getJavaPluginService();
    }

    public void handleGetTemplateList(
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        Statements statements;
        try {
            statements = buildTemplateList();
        } catch (StorageException ex) {
            throw new ServerError("Can't prepare response.", ex);
        }
        ServletUtilities.sendResponse(request, response, statements);
    }

    private Statements buildTemplateList() throws StorageException {
        StatementsBuilder result = Statements.arrayList().builder();
        for (PluginTemplate template :
                templateFacade.getPluginTemplates()) {
            result.addAll(PluginTemplateToRdf.definitionAsRdf(template));
        }
        for (ReferenceTemplate template :
                templateFacade.getReferenceTemplates()) {
            result.addAll(ReferenceTemplateToRdf.definitionAsRdf(template));
        }
        return result;
    }

    public void handleGetTemplate(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        try {
            if (templateFacade.isPluginTemplate(resource)) {
                handleGetPluginTemplate(resource, request, response);
            } else {
                handleGetReferenceTemplate(resource, request, response);
            }
        } catch (StorageException ex) {
            throw new ServerError("Can't get template.", ex);
        }
    }

    private void handleGetPluginTemplate(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws StorageException {
        PluginTemplate template =
                templateFacade.getPluginTemplate(resource);
        if (template == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        ServletUtilities.sendResponse(
                request, response,
                PluginTemplateToRdf.definitionAsRdf(template));
    }

    private void handleGetReferenceTemplate(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws StorageException {
        ReferenceTemplate template =
                templateFacade.getReferenceTemplate(resource);
        if (template == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        ServletUtilities.sendResponse(
                request, response,
                ReferenceTemplateToRdf.definitionAsRdf(template));
    }

    public void handleGetConfiguration(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        try {
            if (templateFacade.isPluginTemplate(resource)) {
                handleGetPluginConfiguration(resource, request, response);
            } else {
                handleGetReferenceConfiguration(resource, request, response);
            }
        } catch (StorageException ex) {
            throw new ServerError("Can't get template configuration.", ex);
        }
    }

    private void handleGetPluginConfiguration(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws StorageException {
        PluginTemplate template =
                templateFacade.getPluginTemplate(resource);
        if (template == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        ServletUtilities.sendResponse(
                request, response,
                template.configuration()
                        .withGraph(template.configurationGraph()));
    }

    private void handleGetReferenceConfiguration(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws StorageException {
        ReferenceTemplate template =
                templateFacade.getReferenceTemplate(resource);
        if (template == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        ServletUtilities.sendResponse(
                request, response,
                template.configuration()
                        .withGraph(template.configurationGraph()));
    }

    public void handleGetEffectiveConfiguration(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        try {
            if (templateFacade.isPluginTemplate(resource)) {
                handleGetEffectivePluginConfiguration(
                        resource, request, response);
            } else {
                handleGetEffectiveReferenceConfiguration(
                        resource, request, response);
            }
        } catch (StorageException ex) {
            throw new ServerError(
                    "Can't get template effective configuration.", ex);
        }
    }

    private void handleGetEffectivePluginConfiguration(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws StorageException, ServerError {
        PluginTemplate template =
                templateFacade.getPluginTemplate(resource);
        if (template == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        Statements result = Statements.arrayList();
        try {
            result.addAll(ConfigurationFacade.merge(
                    List.of(template.configuration().asList()),
                    template.configurationDescription(),
                    template.configurationGraph().stringValue(),
                    template.configurationGraph()));
        } catch (ConfigurationException ex) {
            throw new ServerError(
                    "Can't get create effective configuration.", ex);
        }
        ServletUtilities.sendResponse(
                request, response,
                result.withGraph(template.configurationGraph()));
    }

    private void handleGetEffectiveReferenceConfiguration(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws StorageException, ServerError {
        ReferenceTemplate template =
                templateFacade.getReferenceTemplate(resource);
        if (template == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        PluginTemplate plugin =
                templateFacade.getPluginTemplate(template.plugin());
        List<List<Statement>> configurations =
                collectConfigurations(template, plugin);
        //
        Statements result = Statements.arrayList();
        try {
            result.addAll(ConfigurationFacade.merge(
                    configurations,
                    plugin.configurationDescription(),
                    template.configurationGraph().stringValue(),
                    template.configurationGraph()));
        } catch (ConfigurationException ex) {
            throw new ServerError(
                    "Can't get create effective configuration.", ex);
        }
        ServletUtilities.sendResponse(
                request, response,
                result.withGraph(template.configurationGraph()));
    }

    private List<List<Statement>> collectConfigurations(
            ReferenceTemplate template, PluginTemplate plugin)
            throws StorageException {
        List<List<Statement>> result = new ArrayList<>();
        result.add(template.configuration().asList());
        while (!Objects.equal(template.template(), template.plugin())) {
            // There is another reference template.
            ReferenceTemplate nextTemplate =
                    templateFacade.getReferenceTemplate(template.template());
            if (nextTemplate == null) {
                throw new StorageException(
                        "Missing template '{}' parent for '{}'.",
                        template.template(), template.resource());
            }
            template = nextTemplate;
            // Add configuration.
            result.add(template.configuration().asList());
        }
        result.add(plugin.configuration().asList());
        Collections.reverse(result);
        return result;
    }

    public void handleGetTemplateConfiguration(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        try {
            if (templateFacade.isPluginTemplate(resource)) {
                handleGetTemplatePluginConfiguration(
                        resource, request, response);
            } else {
                handleGetTemplateReferenceConfiguration(
                        resource, request, response);
            }
        } catch (StorageException ex) {
            throw new ServerError("Can't get template configuration.", ex);
        }
    }

    private void handleGetTemplatePluginConfiguration(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws StorageException {
        PluginTemplate template =
                templateFacade.getPluginTemplate(resource);
        if (template == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        Statements result = Statements.arrayList();
        result.addAll(ConfigurationFacade.createNewFromJarFile(
                template.configuration().asList(),
                template.configurationDescription(),
                template.configurationGraph().stringValue(),
                template.configurationGraph()));
        ServletUtilities.sendResponse(
                request, response,
                result.withGraph(template.configurationGraph()));
    }

    private void handleGetTemplateReferenceConfiguration(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws StorageException {
        ReferenceTemplate template =
                templateFacade.getReferenceTemplate(resource);
        if (template == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        PluginTemplate plugin =
                templateFacade.getPluginTemplate(template.plugin());
        //
        Statements result = Statements.arrayList();
        result.addAll(ConfigurationFacade.createNewFromTemplate(
                template.configuration().asList(),
                plugin.configurationDescription(),
                template.configurationGraph().stringValue(),
                template.configurationGraph()));
        ServletUtilities.sendResponse(
                request, response,
                result.withGraph(template.configurationGraph()));
    }

    public void handleGetConfigurationDescription(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        PluginTemplate plugin;
        try {
            plugin = templateFacade.findPluginTemplate(resource);
        } catch (StorageException ex) {
            throw new ServerError("Can't retrieve plugin.", ex);
        }
        if (plugin == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        Statements result = ConfigurationDescriptionToRdf.asRdf(
                plugin.configurationDescription(),
                plugin.configurationDescriptionGraph());
        ServletUtilities.sendResponse(request, response, result);
    }

    public void handleGetDialogResource(
            Resource resource,
            String dialogName, String filePath,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        PluginTemplate pluginTemplate;
        try {
            pluginTemplate = templateFacade.findPluginTemplate(resource);
        } catch (StorageException ex) {
            throw new ServerError("Can't retrieve plugin.", ex);
        }
        if (pluginTemplate == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        Map<String, String> files = pluginTemplate.dialogs().get(dialogName);
        if (files == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        String entryKey = files.get(filePath);
        if (entryKey == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        JavaPlugin javaPlugin = pluginService.getPluginForPluginTemplate(
                resource);
        if (javaPlugin == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        JarEntry entry = javaPlugin.entry(entryKey);
        if (entry == null) {
            response.setStatus(ServletUtilities.HTTP_NOT_FOUND);
            return;
        }
        if (filePath.toLowerCase().endsWith(".js")) {
            response.setHeader("content-type", "application/javascript");
        } else {
            response.setHeader("content-type", "text/html");
        }
        try (OutputStream stream = response.getOutputStream()) {
            PluginTemplateFacade.transferFile(javaPlugin, entry, stream);
        } catch (IOException | PluginException ex) {
            throw new ServerError("Can't write response.", ex);
        }
        response.setStatus(ServletUtilities.HTTP_OK);
    }

    public void handleGetTemplateUsage(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response) {
        StatementsBuilder result = Statements.arrayList().builder();
        List<TemplateUseInfo> infos =
                assistantService.getTemplateUseInfo(resource);
        for (TemplateUseInfo info : infos) {
            result.addType(info.resource(), TEMPLATE);
            if (info.template() != null) {
                result.add(info.template(), HAS_INSTANCE, info.resource());
            }
            for (PipelineInfo pipeline : info.pipelines()) {
                result.add(
                        info.resource(),
                        HAS_USED_IN_PIPELINE,
                        pipeline.resource);
                result.addType(pipeline.resource, PIPELINE);
                result.add(pipeline.resource, HAS_LABEL, pipeline.label);
            }
        }
        ServletUtilities.sendResponse(request, response, result);
    }

    /**
     * Update template by changing selected properties.
     * TODO: This should be PATCH method.
     */
    public void handleUpdateReferenceDefinition(
            Resource resource, MultipartFile templateFile,
            HttpServletRequest request, HttpServletResponse response)
            throws InvalidRequest, ServerError {
        Statements statements = ServletUtilities.read(templateFile);
        List<RawReferenceTemplate> candidates =
                RdfToRawReferenceTemplate.asRawReferenceTemplates(
                        statements.selector());
        if (candidates.size() != 1) {
            throw new InvalidRequest(
                    "Unexpected template definition count '{}'.",
                    candidates.size());
        }
        ReferenceTemplate template =
                candidates.get(0).toReferenceTemplate();
        ReferenceTemplate storedTemplate;
        try {
            storedTemplate = templateFacade.getReferenceTemplate(resource);
        } catch (StorageException ex) {
            throw new ServerError("Can't get template.", ex);
        }
        // User can change only some values.
        ReferenceTemplate nextTemplate = new ReferenceTemplate(
                storedTemplate.resource(),
                storedTemplate.version(),
                storedTemplate.template(),
                storedTemplate.plugin(),
                template.label(),
                template.description(),
                template.note(),
                template.color(),
                template.tags(),
                storedTemplate.knownAs(),
                storedTemplate.configuration(),
                storedTemplate.configurationGraph());
        try {
            templateFacade.storeReferenceTemplate(nextTemplate);
        } catch (StorageException ex) {
            throw new ServerError("Can't get template.", ex);
        }
        response.setStatus(ServletUtilities.HTTP_OK);
    }

    public void handleUpdateReferenceConfiguration(
            Resource resource, MultipartFile configurationFile,
            HttpServletRequest request, HttpServletResponse response)
            throws InvalidRequest, ServerError {
        Statements statements = ServletUtilities.read(configurationFile);
        ReferenceTemplate template;
        try {
            template = templateFacade.getReferenceTemplate(resource);
        } catch (StorageException ex) {
            throw new ServerError("Can't get template.", ex);
        }
        ReferenceTemplate nextTemplate = new ReferenceTemplate(
                template.resource(), template.version(),
                template.template(), template.plugin(),
                template.label(), template.description(), template.note(),
                template.color(), template.tags(), template.knownAs(),
                statements, template.configurationGraph());
        try {
            templateFacade.storeReferenceTemplate(nextTemplate);
        } catch (StorageException ex) {
            throw new ServerError("Can't update template.", ex);
        }
        response.setStatus(ServletUtilities.HTTP_OK);
    }

    /**
     * Create new template using only basic information.
     */
    public void handleCreateReference(
            MultipartFile templateFile, MultipartFile configurationFile,
            HttpServletRequest request, HttpServletResponse response)
            throws InvalidRequest, ServerError {
        Statements templateStatements = ServletUtilities.read(templateFile);
        Statements configuration = ServletUtilities.read(configurationFile);
        List<RawReferenceTemplate> candidates =
                RdfToRawReferenceTemplate.asRawReferenceTemplates(
                        templateStatements.selector());
        if (candidates.size() != 1) {
            throw new InvalidRequest(
                    "Unexpected template definition count '{}'.",
                    candidates.size());
        }
        ReferenceTemplate template = candidates.get(0).toReferenceTemplate();
        PluginTemplate plugin;
        try {
            plugin = templateFacade.findPluginTemplate(template.template());
        } catch (StorageException ex) {
            throw new InvalidRequest(
                    "Can't find plugin for template '{}'.",
                    template.template());
        }
        Resource resource = templateFacade.reserveReferenceResource();
        ReferenceTemplate nextTemplate = new ReferenceTemplate(
                resource,
                ReferenceTemplate.VERSION,
                template.template(),
                plugin.resource(),
                template.label(),
                template.description(),
                template.note(),
                template.color(),
                template.tags(),
                template.knownAs(),
                configuration,
                ConfigurationFacade.configurationGraph(resource));
        try {
            templateFacade.storeReferenceTemplate(nextTemplate);
        } catch (StorageException ex) {
            throw new ServerError("Can't update template.", ex);
        }
        ServletUtilities.sendResponse(
                request, response, ReferenceTemplateToRdf.asRdf(nextTemplate));
    }

    public void handleDeleteReference(
            Resource resource,
            HttpServletRequest request, HttpServletResponse response)
            throws ServerError {
        try {
            templateFacade.deleteReferenceTemplate(resource);
        } catch (StorageException ex) {
            throw new ServerError("Can't delete template.", ex);
        }
        response.setStatus(ServletUtilities.HTTP_OK);
    }

}
