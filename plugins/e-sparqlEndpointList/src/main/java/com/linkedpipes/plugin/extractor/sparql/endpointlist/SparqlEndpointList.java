package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
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

import java.util.ArrayList;
import java.util.List;

public final class SparqlEndpointList extends TaskExecution<QueryTask> {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    // TODO Add report handling.
    @Component.InputPort(iri = "ErrorOutputRdf")
    public WritableSingleGraphDataUnit reportRdf;

    @Component.InputPort(iri = "Tasks")
    public SingleGraphDataUnit tasksRdf;

    @Component.Configuration
    public SparqlEndpointListConfiguration configuration;

    // TODO Add to report handler.
    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private StatementsConsumer consumer;

    private List<QueryTask> tasks;

    @Override
    protected TaskSource<QueryTask> createTaskSource() throws LpException {
        loadTasks();
        if (configuration.getTaskPerGroupLimit() == 0) {
            return TaskSource.defaultTaskSource(this.tasks);
        } else {
            return TaskSource.groupTaskSource(
                    this.tasks,
                    configuration.getTaskPerGroupLimit());
        }
    }

    private void loadTasks() throws LpException {
        RdfSource source = tasksRdf.asRdfSource();
        List<String> resources = source.getByType(
                SparqlEndpointListVocabulary.TASK);
        tasks = new ArrayList<>(resources.size());
        for (String resource : resources) {
            QueryTask task = new QueryTask();
            RdfToPojoLoader.loadByReflection(source, resource, task);
            tasks.add(task);
        }
    }

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        return this.configuration;
    }

    @Override
    protected TaskConsumer<QueryTask> createConsumer() {
        return new QueryTaskExecutor(configuration, consumer, progressReport);
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
    }

    @Override
    protected void afterExecution() throws LpException {
        super.afterExecution();
        this.progressReport.done();
    }
}
