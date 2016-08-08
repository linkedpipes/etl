package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.component.template.Template;
import com.linkedpipes.etl.storage.component.template.TemplateFacade;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * A servlet responsible for handling request regards components.
 *
 * @author Petr Škoda
 */
@RestController
@RequestMapping(value = "/components")
public class ComponentServlet {

    @Autowired
    private TemplateFacade templates;

    /**
     * Return list of interfaces of all components.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public void getAll(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, BaseException {
        RdfUtils.write(request, response, templates.getInterface());
    }

    /**
     * Return interface for given component.
     *
     * @param iri
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/interface", method = RequestMethod.GET)
    @ResponseBody
    public void getComponentInterface(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templates.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response, templates.getInterface(template));
    }

    /**
     * Return definition of given component.
     *
     * @param iri
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/definition", method = RequestMethod.GET)
    @ResponseBody
    public void getDefinition(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templates.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response, templates.getDefinition(template));
    }

    @RequestMapping(value = "/config",
            method = RequestMethod.GET)
    @ResponseBody
    public void getConfig(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templates.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response, templates.getConfig(template));
    }

    @RequestMapping(value = "/configTemplate",
            method = RequestMethod.GET)
    @ResponseBody
    public void getConfigTemplate(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templates.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response,
                templates.getConfigForInstance(template));
    }

    @RequestMapping(value = "/configDescription", method = RequestMethod.GET)
    @ResponseBody
    public void getConfigDescription(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templates.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response, templates.getConfigDesc(template));
    }

    @RequestMapping(value = "/dialog",
            method = RequestMethod.GET)
    @ResponseBody
    public void getDialogResource(@RequestParam(name = "iri") String iri,
            @RequestParam(name = "name") String dialogName,
            @RequestParam(name = "file") String filePath,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Get component.
        final Template template = templates.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        final File file = templates.getDialogResource(template,
                dialogName, filePath);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try (OutputStream stream = response.getOutputStream()) {
            FileUtils.copyFile(file, stream);
        }
    }

    @RequestMapping(value = "/static",
            method = RequestMethod.GET)
    @ResponseBody
    public void getStaticResource(@RequestParam(name = "iri") String iri,
            @RequestParam(name = "file") String filePath,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Get component.
        final Template template = templates.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        final File file = templates.getStaticResource(template, filePath);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try (OutputStream stream = response.getOutputStream()) {
            FileUtils.copyFile(file, stream);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void createComponent(
            @RequestParam(name = "component") MultipartFile component,
            @RequestParam(name = "configuration") MultipartFile configuration,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException, IOException {
        final Collection<Statement> componentRdf
                = RdfUtils.read(component);
        final Collection<Statement> configurationRdf
                = RdfUtils.read(configuration);
        // Create template and stream interface as a response.
        final Template template = templates.createTemplate(componentRdf,
                configurationRdf);
        try (OutputStream stream = response.getOutputStream()) {
            RdfUtils.write(stream, getFormat(request),
                    templates.getInterface(template));
        }
    }

    /**
     * @param request
     * @return Format based on the request headers.
     */
    private static RDFFormat getFormat(HttpServletRequest request) {
        return Rio.getParserFormatForMIMEType(
                request.getHeader("Accept")).orElse(RDFFormat.TRIG);
    }

}
