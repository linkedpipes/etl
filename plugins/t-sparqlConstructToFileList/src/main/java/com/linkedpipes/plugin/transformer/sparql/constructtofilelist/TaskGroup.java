package com.linkedpipes.plugin.transformer.sparql.constructtofilelist;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(iri = SparqlConstructToFileListVocabulary.TASK)
public class TaskGroup {

    @RdfToPojo.Property(iri = SparqlConstructToFileListVocabulary.HAS_FILE_NAME)
    private String fileName;

    @RdfToPojo.Property(iri = SparqlConstructToFileListVocabulary.HAS_FILE_FORMAT)
    private String format;

    @RdfToPojo.Property(iri = SparqlConstructToFileListVocabulary.HAS_TASK_QUERY)
    private List<QueryTask> tasks = new LinkedList<>();

    public TaskGroup() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<QueryTask> getTasks() {
        return tasks;
    }

    public void setTasks(
            List<QueryTask> tasks) {
        this.tasks = tasks;
    }
}
