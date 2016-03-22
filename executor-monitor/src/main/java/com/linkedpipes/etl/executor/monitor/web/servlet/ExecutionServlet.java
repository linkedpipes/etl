package com.linkedpipes.etl.executor.monitor.web.servlet;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.executor.ExecutorFacade;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Petr Škoda
 */
@RestController
@RequestMapping(value = "/executions")
public class ExecutionServlet {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutionServlet.class);

    @Autowired
    private ExecutionFacade executionFacade;

    @Autowired
    private ExecutorFacade executorFacade;

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.GET)
    public void getExecutions(
            @RequestParam(value = "changedSince", required = false)
                    Long changedSince,
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
                for (Statement statement : execution.getPipelineStatements()) {
                    writer.handleStatement(statement);
                }
            }
            // Add metadata.
            final SimpleValueFactory valueFactory
                    = SimpleValueFactory.getInstance();
            writer.handleStatement(valueFactory.createStatement(
                    valueFactory.createIRI("http://etl.linkedpipes.com/metadata"),
                    RDF.TYPE,
                    valueFactory.createIRI("http://etl.linkedpipes.com/ontology/Metadata"),
                    valueFactory.createIRI("http://etl.linkedpipes.com/metadata")));
            writer.handleStatement(valueFactory.createStatement(
                    valueFactory.createIRI("http://etl.linkedpipes.com/metadata"),
                    valueFactory.createIRI("http://etl.linkedpipes.com/ontology/serverTime"),
                    valueFactory.createLiteral((new Date()).getTime()),
                    valueFactory.createIRI("http://etl.linkedpipes.com/metadata")));
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


    @RequestMapping(value = "/{id}/pipeline", method = RequestMethod.GET)
    @ResponseBody
    public void getPipeline(@PathVariable String id,
            HttpServletRequest request, HttpServletResponse response)
            throws ExecutionFacade.OperationFailed,
            ExecutionFacade.UnknownExecution, IOException {
        final RDFFormat format = Rio.getParserFormatForMIMEType(
                request.getHeader("Accept")).orElse(RDFFormat.JSONLD);
        executionFacade.writePipeline(executionFacade.getExecution(id), format,
                response.getOutputStream());
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void acceptMultipart(
            @RequestParam("file") MultipartFile multipart,
            @RequestParam(value = "format") String format,
            HttpServletResponse response)
            throws ExecutionFacade.OperationFailed, IOException {
        startExecution(response, multipart.getInputStream(), format);
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void acceptJsonLd(@RequestBody String body,
            HttpServletRequest request, HttpServletResponse response)
            throws ExecutionFacade.OperationFailed, IOException {
        final InputStream inputStream = new ByteArrayInputStream(
                body.getBytes("UTF-8"));
        startExecution(response, inputStream, request.getHeader("Content-Type"));
    }

    /**
     * Start execution.
     *
     * @param response
     * @param inputStream
     * @param type
     */
    private void startExecution(HttpServletResponse response,
            InputStream inputStream, String type)
            throws ExecutionFacade.OperationFailed {
        final RDFFormat format = Rio.getParserFormatForMIMEType(type).
                orElse(null);
        if (format == null) {
            LOG.info("Invalid format: {}", type);
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }
        executionFacade.createExecution(inputStream, format);
        // Prompt start of executions.
        executorFacade.startExecutions();
    }

}
