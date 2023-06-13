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
@RequestMapping(value = "/pipelines")
public class PipelineServlet {

    private final PipelineServletService service;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Autowired
    public PipelineServlet(StorageService storageService) {
        service = new PipelineServletService(storageService);
    }

    /**
     * Return list with all pipelines.
     */
    @RequestMapping(
            value = "/list",
            method = RequestMethod.GET)
    public void getPipelineList(
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetPipelineList(request, response);
        });
    }

    /**
     * Return definition of pipeline with given IRI. The definition
     * can contain template definitions. In addition, private configuration
     * can be removed.
     */
    @RequestMapping(
            value = "",
            method = RequestMethod.GET)
    public void getPipeline(
            @RequestParam(name = "iri")
                    String iri,
            @RequestParam(name = "templates", defaultValue = "true")
                    boolean includeTemplates,
            @RequestParam(name = "removePrivateConfig", defaultValue = "false")
                    boolean removePrivateConfig,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetPipeline(
                    valueFactory.createIRI(iri),
                    includeTemplates, removePrivateConfig,
                    request, response);
        });
    }

    /**
     * Create new pipeline with new resource. If no pipeline is given
     * an empty pipeline is used. If a pipeline is given, it is used
     * as a template for new pipeline.
     * <p>
     * Similar to store no pipeline migration or template import
     * is performed. In addition, only one pipeline can be given.
     */
    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void createPipeline(
            @RequestParam(value = "pipeline", required = false)
                    MultipartFile pipeline,
            @RequestParam(value = "options") MultipartFile options,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleCreatePipeline(pipeline, options, request, response);
        });
    }

    /**
     * Store given pipeline. Does not perform migration or import of new
     * template.
     */
    @RequestMapping(value = "",
            method = RequestMethod.PUT,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updatePipeline(
            @RequestParam(value = "pipeline") MultipartFile pipeline,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleUpdatePipeline(pipeline, response);
        });
    }

    /**
     * Remove pipeline with given identifier.
     */
    @RequestMapping(
            value = "",
            method = RequestMethod.DELETE)
    public void deletePipeline(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleDeletePipeline(
                    valueFactory.createIRI(iri), response);
        });
    }

}
