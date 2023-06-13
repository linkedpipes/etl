package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.storage.StorageService;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/components")
public class TemplateServlet {

    private final TemplateServletService service;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Autowired
    public TemplateServlet(StorageService storageService) {
        service = new TemplateServletService(storageService);
    }

    @RequestMapping(
            value = "/list",
            method = RequestMethod.GET)
    public void getPipelineList(
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetTemplateList(request, response);
        });
    }

    @RequestMapping(
            value = "/",
            method = RequestMethod.GET)
    public void getComponentInterface(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request,
            HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetTemplate(
                    valueFactory.createIRI(iri), request, response);
        });
    }

    @RequestMapping(
            value = "/configuration",
            method = RequestMethod.GET)
    public void getConfiguration(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetConfiguration(
                    valueFactory.createIRI(iri), request, response);
        });
    }

    @RequestMapping(
            value = "/effective-configuration",
            method = RequestMethod.GET)
    public void getEffectiveConfiguration(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetEffectiveConfiguration(
                    valueFactory.createIRI(iri), request, response);
        });
    }

    @RequestMapping(
            value = "/configuration-template",
            method = RequestMethod.GET)
    public void getConfigTemplate(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetTemplateConfiguration(
                    valueFactory.createIRI(iri), request, response);
        });
    }

    @RequestMapping(
            value = "/configuration-description",
            method = RequestMethod.GET)
    public void getConfigDescription(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetConfigurationDescription(
                    valueFactory.createIRI(iri), request, response);
        });
    }

    @RequestMapping(
            value = "/dialog",
            method = RequestMethod.GET)
    public void getDialogResource(
            @RequestParam(name = "iri") String iri,
            @RequestParam(name = "name") String dialogName,
            @RequestParam(name = "file") String filePath,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetDialogResource(
                    valueFactory.createIRI(iri), dialogName, filePath,
                    request, response);
        });
    }

    @RequestMapping(
            value = "/usage",
            method = RequestMethod.GET)
    public void getUsage(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetTemplateUsage(
                    valueFactory.createIRI(iri), request, response);
        });
    }

    @RequestMapping(
            value = "/component",
            method = RequestMethod.PUT)
    public void updateComponent(
            @RequestParam(name = "iri") String iri,
            @RequestParam(name = "component") MultipartFile component,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleUpdateReferenceDefinition(
                    valueFactory.createIRI(iri), component, request, response);
        });
    }

    @RequestMapping(
            value = "/configuration",
            method = RequestMethod.PUT)
    public void updateConfig(
            @RequestParam(name = "iri") String iri,
            @RequestParam(name = "configuration") MultipartFile configuration,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleUpdateReferenceConfiguration(
                    valueFactory.createIRI(iri), configuration,
                    request, response);
        });
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void createComponent(
            @RequestParam(name = "component") MultipartFile component,
            @RequestParam(name = "configuration") MultipartFile configuration,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleCreateReference(
                    component, configuration, request, response);
        });
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.DELETE)
    public void remove(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleDeleteReference(
                    valueFactory.createIRI(iri), request, response);
        });
    }

}
