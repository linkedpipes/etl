package com.linkedpipes.etl.executor.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;
import com.linkedpipes.etl.executor.module.ModuleFacade;
import com.linkedpipes.etl.executor.pipeline.PipelineExecutor;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping(value = "/v1/executions")
class ExecutionServlet {

    private final ModuleFacade modules;

    private final TaskExecutor taskExecutor;

    private PipelineExecutor executor = null;

    private final Object lock = new Object();

    @Autowired
    public ExecutionServlet(ModuleFacade modules, TaskExecutor taskExecutor) {
        this.modules = modules;
        this.taskExecutor = taskExecutor;
    }

    @ResponseBody
    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void execute(
            @RequestBody AcceptRequest task,
            HttpServletResponse response) {
        if (execute(new File(task.directory), task.iri)) {
            response.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    public boolean execute(File executionDirectory, String iri) {
        synchronized (lock) {
            if (executor != null) {
                // Already executing.
                return false;
            }
            PipelineExecutor newExecutor = new PipelineExecutor(
                    executionDirectory, iri, modules);
            executor = newExecutor;
            taskExecutor.execute(() -> {
                executor.execute();
                // Detach execution object once execution is finished.
                 executor = null;
            });
        }
        return true;
    }


    @ResponseBody
    @RequestMapping(
            value = "/cancel",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void cancel(HttpServletResponse response) {
        PipelineExecutor executorSnp = executor;
        synchronized (lock) {
            if (executorSnp == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            executorSnp.cancelExecution();
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @ResponseBody
    @RequestMapping(
            value = "",
            method = RequestMethod.GET)
    public void getExecution(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        PipelineExecutor executorSnp = executor;
        if (executorSnp == null) {
            // We send no content here as there is just no execution.
            // This must be 2?? response otherwise executor-monitor
            // consider the check for execution (done by this call)
            // as failure.
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        //
        Statements statements =
                executorSnp.getExecution().getInformation().getStatements();
        writeRdfResponse(request, response, statements);
    }

    private void writeRdfResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            Statements statements) throws IOException {
        RDFFormat format = Rio.getParserFormatForMIMEType(
                request.getHeader("Accept")).orElse(RDFFormat.JSONLD);
        response.setHeader("Content-Type", format.getDefaultMIMEType());
        OutputStream stream = response.getOutputStream();
        Rio.write(statements, stream, format);
    }

    @ResponseBody
    @RequestMapping(
            value = "/overview",
            method = RequestMethod.GET)
    public void getOverview(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ExecutorException {
        PipelineExecutor executorSnp = executor;
        if (executorSnp == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        response.setHeader("Content-Type", "application/ld+json");
        writeStatusOverview(response.getOutputStream(), executorSnp);
    }

    private static void writeStatusOverview(
            OutputStream stream, PipelineExecutor executor)
            throws IOException {
        if (executor.getExecution() == null) {
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonRoot =
                executor.getExecution().getExecutionOverviewModel()
                        .toJsonLd(objectMapper);
        objectMapper.writeValue(stream, jsonRoot);
    }


    @ResponseBody
    @RequestMapping(
            value = "/messages",
            method = RequestMethod.GET)
    public void getPipelineMessages(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        PipelineExecutor executorSnp = executor;
        if (executorSnp == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        Statements statements = new Statements(
                executorSnp.getExecution().getPipelineMessages().getStatements());
        writeRdfResponse(request, response, statements);
    }

    @ResponseBody
    @RequestMapping(
            value = "/messages/component",
            method = RequestMethod.GET)
    public void getComponentMessages(
            @RequestParam(value = "iri") String iri,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        PipelineExecutor executorSnp = executor;
        if (executorSnp == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //
        ExecutionModel model = executorSnp.getExecution().getModel();
        ExecutionComponent component = model.getComponent(iri);
        Statements statements =
                executorSnp.getExecution().getComponentMessages(component);
        writeRdfResponse(request, response, statements);
    }


}
