package com.linkedpipes.etl.executor.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.module.ModuleFacade;
import com.linkedpipes.etl.executor.pipeline.PipelineExecutor;
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

    /**
     * Data transfer object for incoming task definition.
     */
    static class AcceptRequest {

        /**
         * Execution IRI.
         */
        public String iri;

        /**
         * Directory with execution.
         */
        public String directory;

    }

    @Autowired
    private ModuleFacade modules;

    @Autowired
    private TaskExecutor taskExecutor;

    private PipelineExecutor executor = null;

    private final Object lock = new Object();

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void execute(@RequestBody AcceptRequest task,
            HttpServletResponse response) {
        if (execute(new File(task.directory), task.iri)) {
            response.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/cancel", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void cancel(HttpServletResponse response) {
        final PipelineExecutor executorSnp = executor;
        synchronized (lock) {
            if (executorSnp == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            executorSnp.cancelExecution();
            response.setStatus(HttpServletResponse.SC_CREATED);
        }
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.GET)
    public void status(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ExecutorException {
        final PipelineExecutor executorSnp = executor;
        if (executorSnp == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            final RDFFormat format = Rio.getParserFormatForMIMEType(
                    request.getHeader("Accept")).orElse(RDFFormat.JSONLD);
            writeStatus(response.getOutputStream(), format, executorSnp);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    public void statusOverview(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ExecutorException {
        final PipelineExecutor executorSnp = executor;
        if (executorSnp == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            // TODO Check for format
            writeStatusOverview(response.getOutputStream(), executorSnp);
        }
    }

    /**
     * Run execution in given directory.
     *
     * @param executionDirectory
     * @param iri
     * @return False if there is running pipeline.
     */
    public boolean execute(File executionDirectory, String iri) {
        synchronized (lock) {
            if (executor != null) {
                // Already executing.
                return false;
            }
            final PipelineExecutor newExecutor = new PipelineExecutor(
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

    /**
     * Write status of currently executing pipeline into the given stream.
     *
     * @param stream
     * @param format
     */
    private static void writeStatus(OutputStream stream, RDFFormat format,
            PipelineExecutor executor) throws ExecutorException {
        if (executor.getExecution() == null) {
            return;
        }
        executor.getExecution().writeV1Execution(stream, format);
    }

    private static void writeStatusOverview(OutputStream stream,
            PipelineExecutor executor) throws IOException {
        if (executor.getExecution() == null) {
            return;
        }
        final ObjectMapper objectMapper = new ObjectMapper();
        final ObjectNode jsonRoot = executor.getExecution()
                .getExecutionOverviewModel().toJsonLd(objectMapper);
        objectMapper.writeValue(stream, jsonRoot);
    }

}
