package com.linkedpipes.etl.executor.monitor.web.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.executor.ExecutorService;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.GET)
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

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void getExecution(
            @PathVariable String id,
            HttpServletRequest request, HttpServletResponse response)
            throws MonitorException, IOException {
        Execution execution = getLivingExecution(id);
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

    private Execution getLivingExecution(String id) throws MissingResource {
        Execution execution = executions.getLivingExecution(id);
        if (execution == null) {
            throw new MissingResource("Missing execution: {}", id);
        }
        return execution;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteExecution(
            @PathVariable String id,
            HttpServletResponse response) throws MissingResource {
        Execution execution = getLivingExecution(id);
        this.executions.deleteExecution(execution);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(value = "/{id}/cancel", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void cancelExecution(
            @PathVariable String id,
            @RequestBody String body,
            HttpServletResponse response)
            throws MonitorException {
        Execution execution = executions.getLivingExecution(id);
        executor.cancelExecution(execution, body);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(value = "/{id}/logs", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public FileSystemResource getExecutionLogs(
            @PathVariable String id,
            HttpServletResponse response) throws MissingResource {
        Execution execution = getLivingExecution(id);
        File file = this.executions.getExecutionDebugLogFile(execution);
        if (file == null || !file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        response.setHeader("Content-Type", "text/plain");
        return new FileSystemResource(file);
    }

    @RequestMapping(value = "/{id}/logs-tail", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public void getExecutionLogsTail(
            @PathVariable String id,
            @RequestParam(value = "n", defaultValue = "32") int count,
            HttpServletResponse response) throws IOException, MissingResource {
        Execution execution = getLivingExecution(id);
        File file = this.executions.getExecutionDebugLogFile(execution);
        if (file == null || !file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        GetExecutionLogTailHandler handler = new GetExecutionLogTailHandler();
        handler.handle(response, file, count);
    }

    @RequestMapping(
            value = "/{id}/messages/component",
            method = RequestMethod.GET)
    public void getComponentMessages(
            @PathVariable String id,
            @RequestParam(value = "iri") String component,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, MissingResource {
        Execution execution = getLivingExecution(id);
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

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostCreateExecutionHandler.Response createExecution(
            @RequestParam("pipeline") MultipartFile pipeline,
            @RequestParam("input") List<MultipartFile> inputs)
            throws MonitorException {
        PostCreateExecutionHandler handler = new PostCreateExecutionHandler(
                this.executions, this.executor);
        return handler.handle(pipeline, inputs);
    }

    @RequestMapping(value = "/{id}/overview", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void getExecutionOverview(
            @PathVariable String id,
            HttpServletResponse response)
            throws IOException, MissingResource {
        Execution execution = getLivingExecution(id);
        //
        response.setHeader("Content-Type", "application/ld+json");
        JsonNode overview = this.executions.getOverview(execution);
        ObjectMapper objectMapper = new ObjectMapper();
        try (OutputStream stream = response.getOutputStream()) {
            objectMapper.writeValue(stream, overview);
        }
    }

}
