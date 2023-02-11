package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.executor.api.v1.component.task.Task;

public class WikibaseTask implements Task {

    private final String iri;

    public WikibaseTask(String iri) {
        this.iri = iri;
    }

    @Override
    public String getIri() {
        return iri;
    }

    @Override
    public String getGroup() {
        return "default";
    }

}
