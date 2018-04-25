package com.linkedpipes.etl.executor.monitor.web.servlet;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.executor.ExecutorFacade;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/executions")
public class ExecutionServlet {

    public static class CreateExecution {

        private String iri;

        public CreateExecution(Execution execution) {
            this.iri = execution.getIri();
        }

        public String getIri() {
            return iri;
        }

        public void setIri(String iri) {
            this.iri = iri;
        }
    }

    private static final Logger LOG =
            LoggerFactory.getLogger(ExecutionServlet.class);

    @Autowired
    private ExecutionFacade executionFacade;

    @Autowired
    private ExecutorFacade executorFacade;

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.GET)
    public void getExecutions(
            @RequestParam(value = "changedSince",
                    required = false) Long changedSince,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        //
        final Collection<Execution> executions;
        if (changedSince == null) {
            executions = executionFacade.getExecutions();
        } else {
            executions = executionFacade.getExecutions(new Date(changedSince));
        }
        //
        final RDFFormat format = Rio.getParserFormatForMIMEType(
                request.getHeader("Content-Type")).orElse(RDFFormat.JSONLD);
        response.setHeader("Content-Type", format.getDefaultMIMEType());
        try (OutputStream stream = response.getOutputStream()) {
            final RDFWriter writer = Rio.createWriter(format, stream);
            writer.startRDF();
            for (Execution execution : executions) {
                for (Statement statement : execution.getExecutionStatements()) {
                    writer.handleStatement(statement);
                }
                for (Statement statement :
                        execution.getExecutionStatementsGenerated()) {
                    writer.handleStatement(statement);
                }
                for (Statement statement : execution.getPipelineStatements()) {
                    writer.handleStatement(statement);
                }
            }
            // Add metadata.
            final SimpleValueFactory valueFactory
                    = SimpleValueFactory.getInstance();
            writer.handleStatement(valueFactory.createStatement(
                    valueFactory
                            .createIRI("http://etl.linkedpipes.com/metadata"),
                    RDF.TYPE,
                    valueFactory.createIRI(
                            "http://etl.linkedpipes.com/ontology/Metadata"),
                    valueFactory
                            .createIRI("http://etl.linkedpipes.com/metadata")));
            writer.handleStatement(valueFactory.createStatement(
                    valueFactory
                            .createIRI("http://etl.linkedpipes.com/metadata"),
                    valueFactory.createIRI(
                            "http://etl.linkedpipes.com/ontology/serverTime"),
                    valueFactory.createLiteral((new Date()).getTime()),
                    valueFactory
                            .createIRI("http://etl.linkedpipes.com/metadata")));
            //
            writer.endRDF();
            stream.flush();
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void getExecution(@PathVariable String id,
            HttpServletRequest request, HttpServletResponse response)
            throws ExecutionFacade.OperationFailed,
            ExecutionFacade.UnknownExecution, IOException {
        final RDFFormat format = Rio.getParserFormatForMIMEType(
                request.getHeader("Accept")).orElse(RDFFormat.JSONLD);
        response.setHeader("Content-Type", format.getDefaultMIMEType());
        executionFacade.writeExecution(executionFacade.getExecution(id), format,
                response.getOutputStream());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteExecution(@PathVariable String id,
            HttpServletRequest request, HttpServletResponse response) {
        final Execution execution = executionFacade.getExecution(id);
        executionFacade.deleteExecution(execution);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(value = "/{id}/cancel", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void cancelExecution(@PathVariable String id,
            @RequestBody String body,
            HttpServletRequest request, HttpServletResponse response) {
        final Execution execution = executionFacade.getExecution(id);
        executorFacade.cancelExecution(execution, body);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(value = "/{id}/logs", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public FileSystemResource getExecutionLogs(@PathVariable String id,
            HttpServletResponse response) {
        final File file = executionFacade.getExecutionLogFile(
                executionFacade.getExecution(id));
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
    public void getExecutionLogsTail(@PathVariable String id,
            @RequestParam(value = "n", defaultValue = "32") int count,
            HttpServletResponse response) throws IOException {
        File file = executionFacade.getExecutionLogFile(
                executionFacade.getExecution(id));
        if (file == null || !file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return ;
        }
        String[] lines = readLogTail(file, count);
        response.setHeader("Content-Type", "text/plain");
        writeLinesToResponse(lines, response);
    }

    private String[] readLogTail(File file, int count) throws IOException {
        ReversedLinesFileReader reader = new ReversedLinesFileReader(file);
        String[] lines = new String[count];
        for (int i = count - 1; i >= 0; --i) {
            String line;
            try {
                line = reader.readLine();
            } catch (Exception ex) {
                LOG.error("Can't read log file, i: {}", i, ex);
                break;
            }
            if (line == null) {
                break;
            } else{
                lines[i] = line;
            }
        }
        reader.close();
        return lines;
    }

    private void writeLinesToResponse(
            String[] lines, HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = response.getWriter();
        for (int i = 0; i < lines.length; ++i) {
            if (lines[i] != null) {
                writer.write(lines[i]);
                writer.write("\n");
            }
        }
        writer.flush();
    }

    @RequestMapping(value = "/{id}/pipeline", method = RequestMethod.GET)
    @ResponseBody
    public void getPipeline(@PathVariable String id,
            HttpServletRequest request, HttpServletResponse response)
            throws ExecutionFacade.OperationFailed,
            ExecutionFacade.UnknownExecution, IOException {
        final RDFFormat format = Rio.getParserFormatForMIMEType(
                request.getHeader("Accept")).orElse(RDFFormat.JSONLD);
        response.setHeader("Content-Type", format.getDefaultMIMEType());
        executionFacade.writePipeline(executionFacade.getExecution(id), format,
                response.getOutputStream());
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CreateExecution acceptMultipart(
            @RequestParam("pipeline") MultipartFile pipeline,
            @RequestParam("input") List<MultipartFile> inputs,
            HttpServletResponse response)
            throws ExecutionFacade.OperationFailed, IOException {
        final Execution execution = executionFacade.createExecution(pipeline,
                inputs);
        // TODO Execution in other thread !
        executorFacade.startExecutions();
        return new CreateExecution(execution);
    }

    @RequestMapping(value = "/{id}/overview", method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void getExecutionOverview(@PathVariable String id,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        final Execution execution = executionFacade.getExecution(id);
        if (execution == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setHeader("Content-Type", "application/json");
        executionFacade.writeOverview(execution,
                response.getOutputStream());
    }

}
