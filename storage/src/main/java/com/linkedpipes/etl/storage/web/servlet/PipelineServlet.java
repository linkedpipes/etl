package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.component.pipeline.Pipeline;
import com.linkedpipes.etl.storage.component.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.unpacker.UnpackerFacade;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * @author Petr Škoda
 */
@RestController
@RequestMapping(value = "/pipelines")
public class PipelineServlet {

    @Autowired
    private PipelineFacade pipelines;

    @Autowired
    private UnpackerFacade unpacker;

    /**
     * Return a list of all pipelines references.
     *
     * @param request
     * @param response
     * @throws BaseException
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public void getPipelines(HttpServletRequest request,
            HttpServletResponse response) throws BaseException {
        try (OutputStream stream = response.getOutputStream()) {
            RdfUtils.write(stream,
                    RdfUtils.getFormat(request, RDFFormat.TRIG),
                    pipelines.getReferenceRdf());
        } catch (IOException ex) {
            throw new BaseException(ex);
        }
    }

    /**
     * Return a definition of a single pipeline.
     *
     * @param iri
     * @param request
     * @param response
     * @throws BaseException
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public void getPipeline(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        final Pipeline pipeline = pipelines.getPipeline(iri);
        if (pipeline == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        try (OutputStream stream = response.getOutputStream()) {
            RdfUtils.write(stream,
                    RdfUtils.getFormat(request, RDFFormat.TRIG),
                    pipelines.getPipelineRdf(pipeline));
        } catch (IOException ex) {
            throw new BaseException(ex);
        }
    }

    /**
     * Create a new pipeline and return a reference to it.
     *
     * @param data
     * @param options
     * @param request
     * @param response
     * @throws BaseException
     */
    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void createPipeline(
            @RequestParam(name = "pipeline", required = false)
                    MultipartFile data,
            @RequestParam(name = "options", required = false)
                    MultipartFile options,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        final Collection<Statement> dataRdf = RdfUtils.read(data);
        final Collection<Statement> optionsRdf = RdfUtils.read(options);
        //
        final Pipeline pipeline = pipelines.createPipeline(dataRdf, optionsRdf);
        try (OutputStream stream = response.getOutputStream()) {
            RdfUtils.write(stream,
                    RdfUtils.getFormat(request, RDFFormat.TRIG),
                    pipelines.getReferenceRdf(pipeline));
        } catch (IOException ex) {
            throw new BaseException(ex);
        }
    }

    /**
     * Update existing pipeline.
     *
     * @param iri
     * @param data
     * @param request
     * @param response
     * @throws BaseException
     */
    @RequestMapping(value = "", method = RequestMethod.PUT,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void updatePipeline(@RequestParam(name = "iri") String iri,
            @RequestParam("pipeline") MultipartFile data,
            HttpServletRequest request, HttpServletResponse response)
            throws BaseException {
        final Collection<Statement> dataRdf = RdfUtils.read(data);
        //
        final Pipeline pipeline = pipelines.getPipeline(iri);
        if (pipeline == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        pipelines.updatePipeline(pipeline, dataRdf);
    }

    /**
     * Delete existing pipeline.
     *
     * @param iri
     * @param request
     * @param response
     * @throws BaseException
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
     * @throws BaseException
     */
    @RequestMapping(value = "/unpack", method = RequestMethod.POST)
    @ResponseBody
    public void unpackPipeline(
            @RequestParam(name = "iri", required= false) String iri,
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
        try (OutputStream stream = response.getOutputStream()) {
            RdfUtils.write(stream,
                    RdfUtils.getFormat(request, RDFFormat.TRIG),
                    statements);
        } catch (IOException ex) {
            throw new BaseException(ex);
        }
    }


}
