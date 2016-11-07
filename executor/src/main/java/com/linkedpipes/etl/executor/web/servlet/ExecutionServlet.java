package com.linkedpipes.etl.executor.web.servlet;

import com.linkedpipes.etl.executor.module.ModuleFacade;
import com.linkedpipes.etl.executor.pipeline.PipelineExecutor;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
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
@RequestMapping(value = "/executions")
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

    ;

    @Autowired
    private ModuleFacade modules;

    @Autowired
    private TaskExecutor taskExecutor;

    private PipelineExecutor executor = null;

    private final Object lock = new Object();

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void accept(@RequestBody AcceptRequest task,
            HttpServletResponse response) {
        if (execute(new File(task.directory), task.iri)) {
            response.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.GET)
    public void status(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if (executor == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            final RDFFormat format = Rio.getParserFormatForMIMEType(
                    request.getHeader("Accept")).orElse(RDFFormat.JSONLD);
            writeStatus(response.getOutputStream(), format);
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
            final PipelineExecutor newExecutor
                    = new PipelineExecutor(executionDirectory, modules, iri);
            executor = newExecutor;
            taskExecutor.execute(() -> {
                executor.initialize();
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
    public void writeStatus(OutputStream stream, RDFFormat format) {
        final PipelineExecutor executorSnapshot = executor;
        if (executorSnapshot != null) {
            executorSnapshot.writeStatus(stream, format);
        }
    }

}
