package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.eclipse.rdf4j.query.TupleQueryResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TaskLoader {

    final List<Task> tasks = new ArrayList<>();

    private final SingleGraphDataUnit dataUnit;

    private final RdfSource source;

    private final ExceptionFactory exceptionFactory;

    public TaskLoader(SingleGraphDataUnit dataUnit,
            ExceptionFactory exceptionFactory) {
        this.dataUnit = dataUnit;
        this.source = Rdf4jSource.wrapRepository(dataUnit.getRepository());
        this.exceptionFactory = exceptionFactory;
    }

    public void initialize() throws LpException {
        List<String> resources = loadTaskResources();
        for (String resource : resources) {
            Task task = new Task(resource);
            loadTask(task);
            tasks.add(task);
        }
    }

    private List<String> loadTaskResources() throws LpException {
        List<String> result = new ArrayList<>();
        dataUnit.execute((connection) -> {
            result.clear();
            TupleQueryResult queryResult = connection.prepareTupleQuery(
                    getResourceQuery()).evaluate();
            while (queryResult.hasNext()) {
                result.add(queryResult.next().getValue("s").stringValue());
            }
        });
        return result;
    }

    private String getResourceQuery() {
        return "SELECT ?s FROM <" + dataUnit.getReadGraph() + "> " +
                "WHERE { ?s a <" + SparqlEndpointChunkedListVocabulary.TASK +
                "> }";
    }

    private void loadTask(Task task) throws LpException {
        try {
            RdfUtils.load(source, task.getIri(),
                    dataUnit.getReadGraph().stringValue(), task,
                    RdfToPojo.descriptorFactory());
        } catch (RdfUtilsException ex) {
            throw exceptionFactory.failure("Can't load task.", ex);
        }
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

}
