package com.linkedpipes.plugin.loader.coachdb;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CoachDbLoader implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.Configuration
    public CoachDbLoaderConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    private CouchDb couchDb;

    @Override
    public void execute() throws LpException {
        initializeCouchDb();
        if (configuration.isRecreateDatabase()) {
            recreateDatabase();
        }
        uploadData();
    }

    private void initializeCouchDb() {
        couchDb = new CouchDb(configuration.getUrl(), exceptionFactory);
    }

    private void recreateDatabase() throws LpException {
        couchDb.deleteDatabase(configuration.getDatabase());
        couchDb.createDatabase(configuration.getDatabase());
    }

    private void uploadData() throws LpException {
        int batchSize = configuration.getBatchSize();
        int numberOfBatches = getNumberOfBatches();
        progressReport.start(numberOfBatches);
        List<File> filesToUpload = new ArrayList<>(batchSize);
        for (FilesDataUnit.Entry entry : inputFiles) {
            filesToUpload.add(entry.toFile());
            if (filesToUpload.size() >= batchSize) {
                uploadBatch(filesToUpload);
                filesToUpload.clear();
                progressReport.entryProcessed();
            }
        }
        uploadBatch(filesToUpload);
        progressReport.done();
    }

    private int getNumberOfBatches() {
        int batchSize = configuration.getBatchSize();
        return (int)Math.ceil((double)inputFiles.size() / batchSize);
    }

    private void uploadBatch(Collection<File> files) throws LpException {
        if (files.isEmpty()) {
            return;
        }
        couchDb.uploadDocuments(configuration.getDatabase(), files);
    }

}
