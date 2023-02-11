package com.linkedpipes.etl.executor.api.v1.component.task;

class TaskMock implements Task{

    final String iri;

    final String group;

    int failCounter;

    public TaskMock(String iri, String group) {
        this.iri = iri;
        this.group = group;
        this.failCounter = 0;
    }

    public TaskMock(String iri, String group, int failCounter) {
        this.iri = iri;
        this.group = group;
        this.failCounter = failCounter;
    }

    @Override
    public String getIri() {
        return iri;
    }

    @Override
    public String getGroup() {
        return group;
    }

}


