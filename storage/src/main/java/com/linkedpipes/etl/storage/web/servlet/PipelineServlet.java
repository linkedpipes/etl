package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.pipeline.info.InfoFacade;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.unpacker.UnpackerFacade;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

@RestController
@RequestMapping(value = "/pipelines")
public class PipelineServlet {

    @Autowired
    private PipelineFacade pipelines;

    @Autowired
    private UnpackerFacade unpacker;

    @Autowired
    private InfoFacade infoFacade;

    /**
     * Return a list of all pipelines references.
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public void getPipelines(HttpServletRequest request,
            HttpServletResponse response) throws BaseException {
        RdfUtils.write(request, response, pipelines.getReferenceAsRdf());
    }

    /**
     * Return a definition of a single pipeline.
     *
     * @param iri
     * @param includeTemplates If true include definitions of the templates.
     * @param includeMapping If true include template mapping.
     * @param request
     * @param response
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public void getPipeline(@RequestParam(name = "iri") String iri,
            @RequestParam(name = "templates", defaultValue = "true")
                    boolean includeTemplates,
            @RequestParam(name = "mappings", defaultValue = "true")
                    boolean includeMapping,
            @RequestParam(name = "removePrivateConfig", defaultValue = "false")
                        boolean removePrivateConfig,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        final Pipeline pipeline = pipelines.getPipeline(iri);
        if (pipeline == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        RdfUtils.write(request, response, pipelines.getPipelineRdf(pipeline,
                includeTemplates, includeMapping, removePrivateConfig));
    }

    /**
     * Create a new pipeline and return a reference to it.
     *
     * @param options
     * @param pipeline
     * @param request
     * @param response
     */
    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void createPipeline(
            @RequestParam(value = "options", required = false)
                    MultipartFile options,
            @RequestParam(value = "pipeline", required = false)
                    MultipartFile pipeline,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        final Collection<Statement> optionsRdf = RdfUtils.read(options);
        final Collection<Statement> pipelineRdf = RdfUtils.read(pipeline);
        //
        final Pipeline pipelineObject = pipelines.createPipeline(
                pipelineRdf, optionsRdf);
        RdfUtils.write(request, response,
                pipelines.getReferenceAsRdf(pipelineObject));
    }

    /**
     * Update existing pipeline.
     *
     * @param iri
     * @param pipeline
     * @param request
     * @param response
     */
    @RequestMapping(value = "", method = RequestMethod.PUT,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void updatePipeline(@RequestParam(name = "iri") String iri,
            @RequestParam(value = "pipeline") MultipartFile pipeline,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        final Collection<Statement> pipelineRdf = RdfUtils.read(pipeline);
        //
        final Pipeline pipelineObject = pipelines.getPipeline(iri);
        if (pipeline == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        pipelines.updatePipeline(pipelineObject, pipelineRdf);
    }

    /**
     * Delete existing pipeline.
     *
     * @param iri
     * @param request
     * @param response
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @ResponseBody
    public void deletePipeline(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        final Pipeline pipeline = pipelines.getPipeline(iri);
        if (pipeline == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        pipelines.deletePipeline(pipeline);
    }

    /**
     * Return unpacked pipeline that can be used for execution.
     *
     * @param iri
     * @param options
     * @param request
     * @param response
     */
    @RequestMapping(value = "/unpack", method = RequestMethod.POST)
    @ResponseBody
    public void unpackPipeline(
            @RequestParam(name = "iri", required = false) String iri,
            @RequestParam(value = "options", required = false)
                    MultipartFile options,
            @RequestParam(value = "pipeline", required = false)
                    MultipartFile pipeline,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        //
        final Collection<Statement> optionsRdf = RdfUtils.read(options);
        final Collection<Statement> pipelineRdf = RdfUtils.read(pipeline);
        //
        final Collection<Statement> statements;
        if (iri == null) {
            statements = unpacker.unpack(pipelineRdf, optionsRdf);
        } else {
            final Pipeline pipelineInstance = pipelines.getPipeline(iri);
            if (pipelineInstance == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            statements = unpacker.unpack(pipelineInstance, optionsRdf);
        }
        //
        RdfUtils.write(request, response, statements);
    }

    /**
     * Given a pipeline update it so it corresponds to a pipeline of local
     * instance. The given pipeline is also migrated to current version.
     *
     * @param options
     * @param pipeline
     * @param request
     * @param response
     */
    @RequestMapping(value = "/localize", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void localizePipeline(
            @RequestParam(value = "options", required = false)
                    MultipartFile options,
            @RequestParam(value = "pipeline") MultipartFile pipeline,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        //
        final Collection<Statement> optionsRdf = RdfUtils.read(options);
        Collection<Statement> pipelineRdf = RdfUtils.read(pipeline);
        //
        pipelineRdf = pipelines.localizePipeline(pipelineRdf, optionsRdf);
        RdfUtils.write(request, response, pipelineRdf);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public void getDesignInformation(HttpServletRequest request,
            HttpServletResponse response) throws BaseException {
        RdfUtils.write(request, response, infoFacade.getInformation());
    }

}
