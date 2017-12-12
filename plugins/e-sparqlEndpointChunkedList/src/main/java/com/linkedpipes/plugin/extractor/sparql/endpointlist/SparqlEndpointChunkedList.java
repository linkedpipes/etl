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
import com.linkedpipes.etl.executor.api.v1.component.task.TaskSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.repository.Repository;

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

    private static final int THREADS_PER_GROUP = 1;

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

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Configuration
    public SparqlEndpointChunkedListConfiguration configuration;

    private StatementsConsumer consumer;

    private List<QueryTask> tasks;

    private Map<String, List<File>> inputFilesByName;

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        return this.configuration;
    }

    @Override
    protected TaskSource<QueryTask> createTaskSource() throws LpException {
        loadTasks();
        return TaskSource.groupTaskSource(this.tasks, THREADS_PER_GROUP);
    }

    private void loadTasks() throws LpException {
        RdfSource source = tasksRdf.asRdfSource();
        String graph = tasksRdf.getReadGraph().stringValue();
        List<String> resources = source.getByType(
                graph, SparqlEndpointChunkedListVocabulary.TASK);
        tasks = new ArrayList<>(resources.size());
        for (String resource : resources) {
            QueryTask task = new QueryTask();
            RdfToPojoLoader.loadByReflection(
                    source, resource, graph, task);
            tasks.add(task);
        }
    }

    @Override
    protected TaskConsumer<QueryTask> createConsumer() {
        return new QueryTaskExecutor(
                this.configuration, this.consumer, this.progressReport,
                this.exceptionFactory, this.inputFilesByName
        );
    }

    @Override
    protected ReportWriter createReportWriter() {
        String graph = reportRdf.getWriteGraph().stringValue();
        Repository repository = reportRdf.getRepository();
        return ReportWriter.create(reportRdf.getWriter());
    }

    @Override
    protected void beforeExecution() throws LpException {
        super.beforeExecution();
        this.consumer = new StatementsConsumer(outputRdf);
        this.progressReport.start(tasks);
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

    @Override
    protected void afterExecution() throws LpException {
        super.afterExecution();
        this.progressReport.done();
    }

}
