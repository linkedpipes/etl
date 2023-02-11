package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecution;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecutionConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;

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

    private StatementsConsumer consumer;

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        TaskExecutionConfiguration result = new TaskExecutionConfiguration();
        result.numberOfThreads = configuration.getThreadsNumber();
        result.skipFailedTasks = true;
        return result;
    }

    @Override
    protected List<QueryTask> loadTasks() throws LpException {
        RdfSource source = tasksRdf.asRdfSource();
        List<String> resources = source.getByType(
                SparqlEndpointListVocabulary.TASK);
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
        return new QueryTaskExecutor(configuration, consumer);
    }

    @Override
    protected void onInitialize(Context context) throws LpException {
        super.onInitialize(context);
        this.consumer = new StatementsConsumer(outputRdf);
    }

}
