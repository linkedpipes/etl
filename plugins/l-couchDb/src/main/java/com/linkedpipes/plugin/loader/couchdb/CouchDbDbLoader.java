package com.linkedpipes.plugin.loader.couchdb;

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

public final class CouchDbDbLoader implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.Configuration
    public CouchDbLoaderConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    private CouchDb couchDb;

    private List<File> filesToUpload;

    private long maxSizeInBytes;

    @Override
    public void execute() throws LpException {
        initializeCouchDb();
        initializeComponent();
        if (configuration.isRecreateDatabase()) {
            recreateDatabase();
        }
        uploadData();
    }

    private void initializeCouchDb() {
        couchDb = new CouchDb(configuration.getUrl(), exceptionFactory);
    }

    private void initializeComponent() {
        filesToUpload = new ArrayList<>();
        maxSizeInBytes = configuration.getBatchSize() * (1024 * 1024);
    }

    private void recreateDatabase() throws LpException {
        couchDb.deleteDatabase(configuration.getDatabase());
        couchDb.createDatabase(configuration.getDatabase());
    }

    private void uploadData() throws LpException {
        long currentBathSize = 0;
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            long entrySize = entry.toFile().length();
            if (isChunkTooBig(currentBathSize, entrySize)) {
                uploadFiles();
                filesToUpload.clear();
                currentBathSize = 0;
            }
            filesToUpload.add(entry.toFile());
            currentBathSize += entrySize;
        }
        uploadBatch(filesToUpload);
        progressReport.done();
    }

    private boolean isChunkTooBig(long currentBathSize, long entrySize) {
        return (entrySize + currentBathSize) > maxSizeInBytes;
    }

    private void uploadBatch(Collection<File> files) throws LpException {
        if (files.isEmpty()) {
            return;
        }
        couchDb.uploadDocuments(configuration.getDatabase(), files);
    }

    private void uploadFiles() throws LpException {
        uploadBatch(filesToUpload);
        reportProgress(filesToUpload.size());
    }

    private void reportProgress(int entries) {
        for (int i = 0; i < entries; ++i) {
            progressReport.entryProcessed();
        }
    }

}
