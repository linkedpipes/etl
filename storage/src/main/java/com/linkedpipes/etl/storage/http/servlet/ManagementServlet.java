package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This API should not be exposed to public as it provides service and
 * management capabilities.
 */
@RestController
@RequestMapping(value = "/management")
public class ManagementServlet {

    private final ManagementServletService service;

    @Autowired
    public ManagementServlet(StorageService storageService) {
        this.service = new ManagementServletService(storageService);
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ResponseBody
    public void export(
            @RequestParam(name = "templates")
                    String exportTemplatesFilter,
            @RequestParam(name = "pipelines")
                    String exportPipelinesFilter,
            @RequestParam(name = "removePrivateConfig", defaultValue = "false")
                    boolean removePrivateConfig,
            @RequestParam(name = "exportType",
                    defaultValue = ManagementServletService.EXPORT_TYPE_FILE)
                    String exportType,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleExport(
                    exportTemplatesFilter, exportPipelinesFilter,
                    removePrivateConfig, exportType.toUpperCase(),
                    request, response);
        });
    }

    /**
     * Import content and return list of imported objects, pipelines
     * and templates.
     */
    @RequestMapping(
            value = "/import",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void importContent(
            @RequestParam(value = "content")
                    MultipartFile content,
            @RequestParam(value = "options")
                    MultipartFile options,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleImport(content, options, request, response);
        });
    }

    /**
     * Taking a single pipeline and templates localize the pipeline,
     * by changing templates to local. This can also import the templates.
     */
    @RequestMapping(
            value = "localize",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void localizeContent(
            @RequestParam(value = "content")
                    MultipartFile content,
            @RequestParam(value = "options", required = false)
                    MultipartFile options,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleLocalize(content, options, request, response);
        });
    }

    /**
     * Reload data from secondary/external memory.
     */
    @RequestMapping(
            value = "/reload",
            method = RequestMethod.POST)
    public void reload(
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleReload(request, response);
        });
    }

    @RequestMapping(
            value = "/assistant",
            method = RequestMethod.GET)
    @ResponseBody
    public void getDesignInformation(
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleGetDesignInformation(request, response);
        });
    }

    /**
     * This should be removed after unpacker is moved to executor.
     */
    @RequestMapping(
            value = "/unpack",
            method = RequestMethod.POST)
    @ResponseBody
    public void unpackPipeline(
            @RequestParam(value = "pipeline")
                    MultipartFile pipeline,
            @RequestParam(value = "options", required = false)
                    MultipartFile options,
            HttpServletRequest request, HttpServletResponse response) {
        ServletUtilities.wrap(request, response, () -> {
            service.handleUnpack(pipeline, options, request, response);
        });
    }
}
