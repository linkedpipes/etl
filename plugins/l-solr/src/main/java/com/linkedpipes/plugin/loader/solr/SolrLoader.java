package com.linkedpipes.plugin.loader.solr;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

public final class SolrLoader implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.Configuration
    public SolrLoaderConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    private SolrCore solr;

    @Override
    public void execute() throws LpException {
        initializeSolr();
        if (configuration.isDeleteBeforeLoading()) {
            solr.deleteData();
        }
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            solr.uploadFile(entry.toFile());
            progressReport.entryProcessed();
        }
        solr.commit();
        progressReport.done();
    }

    private void initializeSolr() {
        solr = new SolrCore(
                configuration.getServer(), configuration.getCore());

        if (configuration.isUseAuthentication()) {
            solr.setCredentials(
                    configuration.getUserName(), configuration.getPassword());
        }


    }

}
