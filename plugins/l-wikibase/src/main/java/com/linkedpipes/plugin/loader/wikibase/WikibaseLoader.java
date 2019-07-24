package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecution;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecutionConfiguration;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskSource;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;

import java.util.ArrayList;
import java.util.List;

public class WikibaseLoader extends TaskExecution<WikibaseTask> {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.InputPort(iri = "ReportRdf")
    public WritableSingleGraphDataUnit reportRdf;

    @Component.Configuration
    public WikibaseLoaderConfiguration configuration;

    private List<WikibaseWorker> workers = new ArrayList<>();

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        return new TaskExecutionConfiguration() {

            @Override
            public int getThreadsNumber() {
                return 1;
            }

            @Override
            public boolean isSkipOnError() {
                return false;
            }

        };
    }

    @Override
    protected TaskSource<WikibaseTask> createTaskSource() throws LpException {
        return TaskSource.defaultTaskSource(loadDocumentReferences());
    }

    private List<WikibaseTask> loadDocumentReferences() throws RdfException {
        RdfSource source = inputRdf.asRdfSource();
        List<WikibaseTask> result = new ArrayList<>();
        source.getByType(WikibaseLoaderVocabulary.WIKIDATA_ENTITY)
                .stream()
                .map((iri) -> new WikibaseTask(iri))
                .forEach((task) -> result.add(task));
        source.getByType(WikibaseLoaderVocabulary.WIKIDATA_NEW_ENTITY)
                .stream()
                .map((iri) -> new WikibaseTask(iri))
                .forEach((task) -> result.add(task));
        return result;
    }

    @Override
    protected TaskConsumer<WikibaseTask> createConsumer() {
        WikibaseWorker worker = new WikibaseWorker(
                configuration, exceptionFactory,
                outputRdf, inputRdf.asRdfSource());
        workers.add(worker);
        return worker;
    }

    @Override
    protected void beforeExecution() throws LpException {
        super.beforeExecution();
        for (WikibaseWorker worker : workers) {
            worker.onBeforeExecution();
        }
    }

    @Override
    protected ReportWriter createReportWriter() {
        return ReportWriter.create(reportRdf.getWriter());
    }

    @Override
    protected void afterExecution() throws LpException {
        super.afterExecution();
        for (WikibaseWorker worker : workers) {
            worker.onAfterExecution();
        }
    }

}
