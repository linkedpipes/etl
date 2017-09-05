package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.pipeline.info.InfoFacade;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
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
import java.util.LinkedList;

/**
 * A servlet responsible for handling request regards components.
 */
@RestController
@RequestMapping(value = "/components")
public class ComponentServlet {

    @Autowired
    private TemplateFacade templateFacade;

    @Autowired
    private InfoFacade infoFacade;

    @Autowired
    private PipelineFacade pipelineFacade;

    /**
     * Return list of interfaces of all components.
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public void getAll(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, BaseException {
        RdfUtils.write(request, response, templateFacade.getInterfaces());
    }

    /**
     * Return interface for given component.
     *
     * @param iri
     * @param request
     * @param response
     */
    @RequestMapping(value = "/interface", method = RequestMethod.GET)
    @ResponseBody
    public void getComponentInterface(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response,
                templateFacade.getInterface(template));
    }

    /**
     * Return definition of given component.
     *
     * @param iri
     * @param request
     * @param response
     */
    @RequestMapping(value = "/definition", method = RequestMethod.GET)
    @ResponseBody
    public void getDefinition(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response,
                templateFacade.getDefinition(template));
    }

    @RequestMapping(value = "/component",
            method = RequestMethod.POST)
    @ResponseBody
    public void updateComponent(@RequestParam(name = "iri") String iri,
            @RequestParam(name = "component") MultipartFile componentRdf,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final Collection<Statement> component = RdfUtils.read(componentRdf);
        templateFacade.updateTemplate(template, component);
    }

    @RequestMapping(value = "/config",
            method = RequestMethod.GET)
    @ResponseBody
    public void getConfig(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response,
                templateFacade.getConfigurationTemplate(template));
    }

    @RequestMapping(value = "/config",
            method = RequestMethod.POST)
    @ResponseBody
    public void updateConfig(@RequestParam(name = "iri") String iri,
            @RequestParam(name = "configuration") MultipartFile configRdf,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final Collection<Statement> config = RdfUtils.read(configRdf);
        templateFacade.updateConfig(template, config);
    }

    @RequestMapping(value = "/configEffective",
            method = RequestMethod.GET)
    @ResponseBody
    public void getEffectiveConfig(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response,
                templateFacade.getEffectiveConfiguration(template));
    }

    /**
     * Return configuration for instance based on given template.
     */
    @RequestMapping(value = "/configTemplate",
            method = RequestMethod.GET)
    @ResponseBody
    public void getConfigTemplate(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response,
                templateFacade.getConfigurationInstance(template));
    }

    @RequestMapping(value = "/configDescription", method = RequestMethod.GET)
    @ResponseBody
    public void getConfigDescription(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RdfUtils.write(request, response,
                templateFacade.getConfigurationDescription(template));
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
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        final File file = templateFacade.getDialogResource(template,
                dialogName, filePath);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Set headers.
        if (filePath.toLowerCase().endsWith(".js")) {
            response.setHeader("Content-Type", "application/javascript");
        } else if (filePath.toLowerCase().endsWith(".html")) {
            response.setHeader("Content-Type", "text/html; charset=UTF-8");
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
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        final File file = templateFacade.getStaticResource(template, filePath);
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
        final Template template = templateFacade.createTemplate(componentRdf,
                configurationRdf);
        try (OutputStream stream = response.getOutputStream()) {
            RdfUtils.write(stream, getFormat(request),
                    templateFacade.getInterface(template));
        }
    }

    @RequestMapping(value = "/usage",
            method = RequestMethod.GET)
    public void getUsage(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException, IOException {
        // TODO Move to pipeline/dpu facade (hide pipeline.info to pipeline).
        // Get component.
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Get components.
        final Collection<Template> templates =
                templateFacade.getTemplateSuccessors(template);
        templates.add(template);
        // Get pipelines and construct the response.
        final Collection<Statement> responseRdf = new LinkedList<>();
        final ValueFactory vf = SimpleValueFactory.getInstance();
        // TODO Add component interface
        final IRI root = vf.createIRI(iri);
        for (Template item : templates) {
            final IRI templateIri = vf.createIRI(item.getIri());
            responseRdf.add(vf.createStatement(root, vf.createIRI(
                    "http://etl.linkedpipes.com/ontology/hasInstance"),
                    templateIri));
            responseRdf.add(vf.createStatement(templateIri, RDF.TYPE,
                    vf.createIRI(
                            "http://etl.linkedpipes.com/ontology/Template")));
            final Collection<String> usage = infoFacade.getUsage(item.getIri());
            for (String pipelineIri : usage) {
                final Pipeline pipeline =
                        pipelineFacade.getPipeline(pipelineIri);
                if (pipeline == null) {
                    continue;
                }
                responseRdf.addAll(pipeline.getReferenceRdf());
                responseRdf.add(vf.createStatement(templateIri, vf.createIRI(
                        "http://etl.linkedpipes.com/ontology/usedIn"),
                        vf.createIRI(pipeline.getIri())));
            }
        }
        //
        try (OutputStream stream = response.getOutputStream()) {
            RdfUtils.write(stream, getFormat(request), responseRdf);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @ResponseBody
    public void remove(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, BaseException {
        // Get component.
        final Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        templateFacade.remove(template);
        response.setStatus(HttpServletResponse.SC_OK);
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
