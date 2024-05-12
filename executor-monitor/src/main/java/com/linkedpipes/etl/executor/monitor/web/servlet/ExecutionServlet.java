package com.linkedpipes.etl.executor.monitor.web.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.executor.ExecutorService;
import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping(value = "/executions")
public class ExecutionServlet {

    private final ExecutionFacade executions;

    private final ExecutorService executor;

    @Autowired
    public ExecutionServlet(
            ExecutionFacade executionFacade,
            ExecutorService executorService) {
        this.executions = executionFacade;
        this.executor = executorService;
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.GET)
    public void getExecutions(
            @RequestParam(value = "changedSince", required = false)
                    Long changedSince,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        RDFFormat format = this.getFormat(request);
        response.setHeader("Content-Type", format.getDefaultMIMEType());
        try (OutputStream stream = response.getOutputStream()) {
            RDFWriter writer = Rio.createWriter(format, stream);
            writer.startRDF();
            GetExecutionsHandler handler =
                    new GetExecutionsHandler(this.executions);
            handler.handle(changedSince, writer);
            writer.endRDF();
            stream.flush();
        }
    }

    private RDFFormat getFormat(HttpServletRequest request) {
        return Rio.getParserFormatForMIMEType(
                request.getHeader("Content-Type")).orElse(RDFFormat.JSONLD);
    }

    @RequestMapping(
            value = "/",
            method = RequestMethod.GET)
    public void getExecution(
            @RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws MonitorException, IOException {
        Execution execution = getLivingExecution(iri);
        //
        RDFFormat format = this.getFormat(request);
        response.setHeader("Content-Type", format.getDefaultMIMEType());
        try (OutputStream stream = response.getOutputStream()) {
            RDFWriter writer = Rio.createWriter(format, stream);
            writer.startRDF();
            this.executions.getExecutionStatements(execution).forEach(
                    st -> writer.handleStatement(st));
            writer.endRDF();
            stream.flush();
        }
    }

    private Execution getLivingExecution(String iri) throws MissingResource {
        Execution execution = executions.getLivingExecutionByIri(iri);
        if (execution == null) {
            throw new MissingResource("Missing execution: {}", iri);
        }
        return execution;
    }

    @RequestMapping(
            value = "/",
            method = RequestMethod.DELETE)
    public void deleteExecution(
            @RequestParam(name = "iri") String iri,
            HttpServletResponse response) throws MissingResource {
        Execution execution = getLivingExecution(iri);
        this.executions.deleteExecution(execution);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(
            value = "/cancel",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void cancelExecution(
            @RequestParam(name = "iri") String iri,
            @RequestBody String body,
            HttpServletResponse response)
            throws MonitorException {
        Execution execution = executions.getLivingExecutionByIri(iri);
        executor.cancelExecution(execution, body);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(
            value = "/logs",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public FileSystemResource getExecutionLogs(
            @RequestParam(name = "iri") String iri,
            HttpServletResponse response) throws MissingResource {
        Execution execution = getLivingExecution(iri);
        File file = this.executions.getExecutionLogFile(execution);
        if (file == null || !file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        response.setHeader("Content-Type", "text/plain");
        return new FileSystemResource(file);
    }

    @RequestMapping(
            value = "/logs-tail",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public void getExecutionLogsTail(
            @RequestParam(name = "iri") String iri,
            @RequestParam(value = "n", defaultValue = "32") int count,
            HttpServletResponse response) throws IOException, MissingResource {
        Execution execution = getLivingExecution(iri);
        File file = this.executions.getExecutionLogFile(execution);
        if (file == null || !file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        GetExecutionLogTailHandler handler = new GetExecutionLogTailHandler();
        handler.handle(response, file, count);
    }

    @RequestMapping(
            value = "/messages",
            method = RequestMethod.GET)
    public void getComponentMessages(
            @RequestParam(name = "iri") String iri,
            @RequestParam(name = "component") String component,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, MissingResource {
        // Make sure components are decoded, for some reason the
        // second one is sometimes not.
        iri = URLDecoder.decode(iri, StandardCharsets.UTF_8);
        component = URLDecoder.decode(component, StandardCharsets.UTF_8);
        //
        Execution execution = getLivingExecution(iri);
        Statements statements =
                this.executions.getMessages(execution, component);
        RDFFormat format = this.getFormat(request);
        response.setHeader("Content-Type", format.getDefaultMIMEType());
        //
        try (OutputStream stream = response.getOutputStream()) {
            RDFWriter writer = Rio.createWriter(format, stream);
            writer.startRDF();
            statements.stream().forEach(writer::handleStatement);
            writer.endRDF();
            stream.flush();
        }
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.POST)
    public PostCreateExecutionHandler.Response createExecution(
            @RequestParam(name = "pipeline")
                    MultipartFile pipeline,
            @RequestParam(value = "input", required = false)
                    List<MultipartFile> inputs)
            throws MonitorException {
        PostCreateExecutionHandler handler =
                new PostCreateExecutionHandler(this.executions, this.executor);
        return handler.handle(pipeline, inputs);
    }

    @RequestMapping(
            value = "/overview",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void getExecutionOverview(
            @RequestParam(name = "iri") String iri,
            HttpServletResponse response)
            throws IOException, MissingResource {
        Execution execution = getLivingExecution(iri);
        //
        response.setHeader("Content-Type", "application/ld+json");
        JsonNode overview = this.executions.getOverview(execution);
        ObjectMapper objectMapper = new ObjectMapper();
        try (OutputStream stream = response.getOutputStream()) {
            objectMapper.writeValue(stream, overview);
        }
    }

}
