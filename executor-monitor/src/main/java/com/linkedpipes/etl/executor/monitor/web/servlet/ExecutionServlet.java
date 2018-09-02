package com.linkedpipes.etl.executor.monitor.web.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.executor.ExecutorService;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
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

    private final ExecutionFacade executionFacade;

    private final ExecutorService executorService;

    @Autowired
    public ExecutionServlet(
            ExecutionFacade executionFacade, ExecutorService executorService) {
        this.executionFacade = executionFacade;
        this.executorService = executorService;
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
        //
        try (OutputStream stream = response.getOutputStream()) {
            RDFWriter writer = Rio.createWriter(format, stream);
            writer.startRDF();
            GetExecutionsHandler handler = new GetExecutionsHandler(
                    this.executionFacade);
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
        Execution execution = executionFacade.getLivingExecution(id);
        if (execution == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        RDFFormat format = this.getFormat(request);
        response.setHeader("Content-Type", format.getDefaultMIMEType());
        try (OutputStream stream = response.getOutputStream()) {
            RDFWriter writer = Rio.createWriter(format, stream);
            writer.startRDF();
            Statements statements =
                    this.executionFacade.getExecutionStatements(execution);
            for (Statement statement : statements) {
                writer.handleStatement(statement);
            }
            writer.endRDF();
            stream.flush();
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteExecution(
            @PathVariable String id,
            HttpServletRequest request,
            HttpServletResponse response) {
        Execution execution = this.executionFacade.getLivingExecution(id);
        if (execution == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        this.executionFacade.deleteExecution(execution);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(value = "/{id}/cancel", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void cancelExecution(
            @PathVariable String id,
            @RequestBody String body,
            HttpServletRequest request,
            HttpServletResponse response)
            throws MonitorException {
        Execution execution = executionFacade.getLivingExecution(id);
        if (execution == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        executorService.cancelExecution(execution, body);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(value = "/{id}/logs", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public FileSystemResource getExecutionLogs(
            @PathVariable String id,
            HttpServletResponse response) {
        Execution execution = this.executionFacade.getLivingExecution(id);
        if (execution == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        File file = this.executionFacade.getExecutionLogFile(execution);
        if (file == null) {
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
            HttpServletResponse response) throws IOException {

        Execution execution = this.executionFacade.getLivingExecution(id);
        if (execution == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        File file = this.executionFacade.getExecutionLogFile(execution);
        if (file == null) {
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
            throws IOException {
        Execution execution = this.executionFacade.getLivingExecution(id);
        if (execution == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Statements statements = this.executionFacade.getComponentMessages(
                execution, component);
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
            @RequestParam("input") List<MultipartFile> inputs,
            HttpServletResponse response)
            throws MonitorException {
        PostCreateExecutionHandler handler = new PostCreateExecutionHandler(
                this.executionFacade, this.executorService);
        return handler.handle(pipeline, inputs);
    }

    @RequestMapping(value = "/{id}/overview", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void getExecutionOverview(
            @PathVariable String id,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        Execution execution = this.executionFacade.getLivingExecution(id);
        if (execution == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        response.setHeader("Content-Type", "application/ld+json");
        JsonNode overview = this.executionFacade.getOverview(execution);
        ObjectMapper objectMapper = new ObjectMapper();
        try (OutputStream stream = response.getOutputStream()) {
            objectMapper.writeValue(stream, overview);
        }
    }

}
