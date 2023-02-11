package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.task.Task;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecution;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecutionConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class WikibaseLoader extends TaskExecution<WikibaseTask> {

    private static final String WIKIBASE_ITEM =
            "http://wikiba.se/ontology#Item";

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

    private final List<WikibaseWorker> workers = new ArrayList<>();

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        TaskExecutionConfiguration result = new TaskExecutionConfiguration();
        result.skipFailedTasks = configuration.isSkipOnError();
        return result;
    }

    @Override
    protected List<WikibaseTask> loadTasks() throws LpException {
        RdfSource source = inputRdf.asRdfSource();
        return source.getByType(WIKIBASE_ITEM)
                .stream()
                .map(WikibaseTask::new)
                .collect(Collectors.toList());
    }

    @Override
    protected ReportWriter createReportWriter() {
        ReportWriter writer = ReportWriter.create(reportRdf.getWriter());
        return new ReportWriter() {

            @Override
            public void onTaskFinished(Task task, Date start, Date end) {
                progressReport.entryProcessed();
                writer.onTaskFinished(task, start, end);
            }

            @Override
            public void onTaskFailed(
                    Task task, Date start, Date end, Throwable throwable) {
                progressReport.entryProcessed();
                writer.onTaskFailed(task, start, end, throwable);
            }

            @Override
            public void onTaskFinishedInPreviousRun(Task task) {
                progressReport.entryProcessed();
                writer.onTaskFinishedInPreviousRun(task);
            }

            @Override
            public String getIriForReport(Task task) {
                return writer.getIriForReport(task);
            }
        };
    }

    @Override
    protected TaskConsumer<WikibaseTask> createConsumer() throws LpException {
        WikibaseWorker worker = new WikibaseWorker(
                configuration, outputRdf, collectStatements());
        workers.add(worker);
        return worker;
    }

    private List<Statement> collectStatements() throws LpException {
        List<Statement> result = new ArrayList<>();
        inputRdf.execute((connection) -> {
            connection.exportStatements(
                    null, null, null, false, new AbstractRDFHandler() {
                        @Override
                        public void handleStatement(Statement st) {
                            result.add(st);
                        }
                    }, inputRdf.getReadGraph());
        });
        return result;
    }

    @Override
    protected void onInitialize(Context context) throws LpException {
        super.onInitialize(context);
    }

    @Override
    protected void onExecutionWillBegin(List<WikibaseTask> tasks)
            throws LpException {
        super.onExecutionWillBegin(tasks);
        for (WikibaseWorker worker : workers) {
            worker.onBeforeExecution();
        }
    }

    @Override
    protected void onExecutionDidFinished() throws LpException {
        super.onExecutionDidFinished();
        for (WikibaseWorker worker : workers) {
            worker.onAfterExecution();
        }
    }

}
