package com.linkedpipes.etl.executor.monitor.web.servlet;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.executor.ExecutorService;
import com.linkedpipes.etl.library.rdf.Statements;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.web.multipart.MultipartFile;

class PostCreateExecutionHandler {

    public static class Response {

        private String iri;

        Response(Execution execution) {
            this.iri = execution.getIri();
        }

        public String getIri() {
            return iri;
        }

        public void setIri(String iri) {
            this.iri = iri;
        }
    }

    private final ExecutionFacade executionFacade;

    private final ExecutorService executorService;

    public PostCreateExecutionHandler(ExecutionFacade executionFacade, ExecutorService executorService) {
        this.executionFacade = executionFacade;
        this.executorService = executorService;
    }

    public Response handle(MultipartFile pipeline, MultipartFile options, List<MultipartFile> inputs)
            throws MonitorException {
        Statements pipelineRdf = readStatements(pipeline, "pipeline");
        Statements optionsRdf = options == null ? Statements.arrayList() : readStatements(options, "options");
        Execution execution = executionFacade.createExecution(pipelineRdf, optionsRdf, inputs);
        executorService.asyncStartExecutions();
        return new Response(execution);
    }

    private Statements readStatements(MultipartFile file, String label) throws MonitorException {
        if (file.getOriginalFilename() == null) {
            throw new MonitorException("Missing name of the {}.", label);
        }
        Optional<RDFFormat> format = Rio.getWriterFormatForFileName(file.getOriginalFilename());
        if (!format.isPresent()) {
            throw new MonitorException("Can't determined format type.");
        }
        Statements statements = Statements.arrayList();
        try (InputStream stream = file.getInputStream()) {
            statements.file().addAll(stream, format.get());
        } catch (IOException ex) {
            throw new MonitorException("Can't read {}.", label, ex);
        }
        return statements;
    }
}
