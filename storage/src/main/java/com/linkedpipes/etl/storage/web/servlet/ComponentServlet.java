package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.plugin.configuration.InvalidConfiguration;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;

@RestController
@RequestMapping(value = "/components")
public class ComponentServlet {

    @Autowired
    private TemplateFacade templateFacade;

    @Autowired
    private InfoFacade infoFacade;

    @Autowired
    private PipelineFacade pipelineFacade;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public void getAll(
            HttpServletRequest request,
            HttpServletResponse response)
            throws BaseException {
        RdfUtils.write(request, response, templateFacade.getInterfaces());
    }

    @RequestMapping(value = "/interface", method = RequestMethod.GET)
    @ResponseBody
    public void getComponentInterface(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request,
            HttpServletResponse response)
            throws BaseException {
        Template template = getTemplate(iri);
        RdfUtils.write(
                request, response, templateFacade.getInterface(template));
    }

    private Template getTemplate(String iri) throws MissingResource {
        Template template = templateFacade.getTemplate(iri);
        if (template == null) {
            throw new MissingResource("Missing template: {}", iri);
        }
        return template;
    }

    @RequestMapping(value = "/definition", method = RequestMethod.GET)
    @ResponseBody
    public void getDefinition(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request,
            HttpServletResponse response)
            throws BaseException {
        Template template = getTemplate(iri);
        RdfUtils.write(
                request, response, templateFacade.getDefinition(template));
    }

    @RequestMapping(value = "/component", method = RequestMethod.POST)
    @ResponseBody
    public void updateComponent(
            @RequestParam(name = "iri") String iri,
            @RequestParam(name = "component") MultipartFile componentRdf)
            throws BaseException {
        Template template = getTemplate(iri);
        templateFacade.updateInterface(template, RdfUtils.read(componentRdf));
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    @ResponseBody
    public void getConfig(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        Template template = getTemplate(iri);
        RdfUtils.write(request, response, templateFacade.getConfig(template));
    }

    @RequestMapping(value = "/config", method = RequestMethod.POST)
    @ResponseBody
    public void updateConfig(
            @RequestParam(name = "iri") String iri,
            @RequestParam(name = "configuration") MultipartFile configRdf)
            throws BaseException {
        Template template = getTemplate(iri);
        templateFacade.updateConfig(template, RdfUtils.read(configRdf));
    }

    @RequestMapping(value = "/configEffective", method = RequestMethod.GET)
    @ResponseBody
    public void getEffectiveConfig(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException, InvalidConfiguration {
        Template template = getTemplate(iri);
        RdfUtils.write(
                request, response, templateFacade.getConfigEffective(template));
    }

    @RequestMapping(value = "/configTemplate", method = RequestMethod.GET)
    @ResponseBody
    public void getConfigTemplate(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException, InvalidConfiguration {
        Template template = getTemplate(iri);
        RdfUtils.write(
                request, response, templateFacade.getConfigInstance(template));
    }

    @RequestMapping(value = "/configDescription", method = RequestMethod.GET)
    @ResponseBody
    public void getConfigDescription(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        Template template = getTemplate(iri);
        RdfUtils.write(
                request, response,
                templateFacade.getConfigDescription(template));
    }

    @RequestMapping(value = "/dialog",
            method = RequestMethod.GET)
    @ResponseBody
    public void getDialogResource(
            @RequestParam(name = "iri") String iri,
            @RequestParam(name = "name") String dialogName,
            @RequestParam(name = "file") String filePath,
            HttpServletResponse response)
            throws IOException, MissingResource {
        Template template = getTemplate(iri);
        File file = templateFacade.getDialogResource(
                template, dialogName, filePath);
        if (file == null) {
            throw new MissingResource(
                    "Missing dialog file: {}/{}", dialogName, filePath);
        }
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
    public void getStaticResource(
            @RequestParam(name = "iri") String iri,
            @RequestParam(name = "file") String filePath,
            HttpServletResponse response)
            throws IOException, MissingResource {
        Template template = getTemplate(iri);
        File file = templateFacade.getStaticResource(template, filePath);
        if (file == null) {
            throw new MissingResource(
                    "Missing dialog resource: {}", filePath);
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
        Collection<Statement> componentRdf = RdfUtils.read(component);
        Collection<Statement> configurationRdf = RdfUtils.read(configuration);
        // Create template and stream interface as a response.
        Template template = templateFacade.createTemplate(
                componentRdf, configurationRdf);
        try (OutputStream stream = response.getOutputStream()) {
            RdfUtils.write(stream, getFormat(request),
                    templateFacade.getInterface(template));
        }
    }

    @RequestMapping(value = "/usage", method = RequestMethod.GET)
    public void getUsage(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException, IOException {
        // TODO Move to pipeline/dpu facade (hide pipeline.info to pipeline).
        Template template = getTemplate(iri);
        // Get components.
        Collection<Template> templates = templateFacade.getSuccessors(template);
        templates.add(template);
        // Get pipelines and construct the response.
        Collection<Statement> responseRdf = new LinkedList<>();
        ValueFactory vf = SimpleValueFactory.getInstance();
        // TODO Add component interface
        IRI root = vf.createIRI(iri);
        for (Template item : templates) {
            IRI templateIri = vf.createIRI(item.getIri());
            responseRdf.add(vf.createStatement(root, vf.createIRI(
                    "http://etl.linkedpipes.com/ontology/hasInstance"),
                    templateIri));
            responseRdf.add(vf.createStatement(templateIri, RDF.TYPE,
                    vf.createIRI(
                            "http://etl.linkedpipes.com/ontology/Template")));
            Collection<String> usage = infoFacade.getUsage(item.getIri());
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
    public void remove(
            @RequestParam(name = "iri") String iri,
            HttpServletResponse response)
            throws BaseException {
        Template template = getTemplate(iri);
        templateFacade.remove(template);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private static RDFFormat getFormat(HttpServletRequest request) {
        return Rio.getParserFormatForMIMEType(
                request.getHeader("Accept")).orElse(RDFFormat.TRIG);
    }

}
