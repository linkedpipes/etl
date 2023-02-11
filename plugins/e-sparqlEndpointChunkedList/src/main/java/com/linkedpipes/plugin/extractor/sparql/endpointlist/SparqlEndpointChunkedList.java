package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecution;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecutionConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Takes CSV files on input. The CSV file rows are used as IRIs and mapped
 * to the given SPARQL as the ${VALUES} placeholder.
 *
 * Example query:
 * CONSTRUCT { ?obec ?p ?o } WHERE { ?obec ?p ?o ${VALUES} }
 * where the input CSV file contains column "obec".
 */
public final class SparqlEndpointChunkedList extends TaskExecution<QueryTask> {

    private static final int EXPECTED_FILES_WITH_SAME_NAME = 1;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.InputPort(iri = "ErrorOutputRdf")
    public WritableSingleGraphDataUnit reportRdf;

    @Component.InputPort(iri = "Tasks")
    public SingleGraphDataUnit tasksRdf;

    @Component.Configuration
    public SparqlEndpointChunkedListConfiguration configuration;

    private StatementsConsumer consumer;

    private Map<String, List<File>> inputFilesByName;

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        TaskExecutionConfiguration result = new TaskExecutionConfiguration();
        result.skipFailedTasks = true;
        result.numberOfThreads = configuration.getUsedThreads();
        return result;
    }

    @Override
    protected List<QueryTask> loadTasks() throws LpException {
        RdfSource source = tasksRdf.asRdfSource();
        List<String> resources = source.getByType(
                SparqlEndpointChunkedListVocabulary.TASK);
        List<QueryTask> result = new ArrayList<>(resources.size());
        for (String resource : resources) {
            QueryTask task = new QueryTask();
            RdfToPojoLoader.loadByReflection(source, resource, task);
            result.add(task);
        }
        return result;
    }

    @Override
    protected ReportWriter createReportWriter() {
        return ReportWriter.create(reportRdf.getWriter());
    }

    @Override
    protected TaskConsumer<QueryTask> createConsumer() {
        return new QueryTaskExecutor(
                this.configuration, this.consumer, this.inputFilesByName);
    }

    @Override
    protected void onInitialize(Context context) throws LpException {
        super.onInitialize(context);
        this.consumer = new StatementsConsumer(outputRdf);
        initializeInputFileMap();
    }

    private void initializeInputFileMap() {
        this.inputFilesByName = new HashMap<>();
        for (FilesDataUnit.Entry entry : this.inputFiles) {
            this.inputFilesByName.computeIfAbsent(entry.getFileName(),
                    (name) -> new ArrayList<>(EXPECTED_FILES_WITH_SAME_NAME))
                    .add(entry.toFile());
        }
    }

}
