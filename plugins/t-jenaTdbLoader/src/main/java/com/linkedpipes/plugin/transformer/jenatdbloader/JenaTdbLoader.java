package com.linkedpipes.plugin.transformer.jenatdbloader;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.loader.DataLoader;
import org.apache.jena.tdb2.loader.LoaderFactory;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class JenaTdbLoader implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(JenaTdbLoader.class);

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public JenaTdbLoaderConfiguration configuration;

    @Override
    public void execute() throws LpException {
        checkConfiguration();

        Dataset dataset = TDB2Factory.connectDataset(getOutputPath());
        DataLoader loader = createLoader(dataset.asDatasetGraph(), LOG::info);

        List<String> filesToLoad = collectInputFiles();

        long elapsed = Timer.time(() -> {
            loader.startBulk();
            loader.load(filesToLoad);
            loader.finishBulk();
        });

        LOG.info("Loading time: {} s", elapsed / 1000);

    }

    private void checkConfiguration() throws LpException {
        if (configuration.getLoader() == null) {
            throw new LpException("Loader is not set.");
        }
        if (configuration.getLocation() == null) {
            throw new LpException("Location is not set.");
        }
    }

    private String getOutputPath() {
        File root = outputFiles.getWriteDirectory();
        String relative = configuration.getLocation();
        if (relative == null || relative.isBlank() || relative.isEmpty()) {
            return root.getAbsolutePath();
        }
        return (new File(root, relative)).getAbsolutePath();
    }

    private List<String> collectInputFiles() {
        List<String> result = new ArrayList<>();
        Iterator<FilesDataUnit.Entry> iterator = inputFiles.iterator();
        while(iterator.hasNext()) {
            FilesDataUnit.Entry entry = iterator.next();
            String absolutePath = entry.toFile().getAbsolutePath();
            result.add(absolutePath);
        }
        return result;
    }

    private DataLoader createLoader(
            DatasetGraph dataset, MonitorOutput monitor) throws LpException {
        switch (configuration.getLoader()) {
            case "basic":
                return LoaderFactory.basicLoader(dataset, monitor);
            case "sequential":
                return LoaderFactory.sequentialLoader(dataset, monitor);
            case "phased":
                return LoaderFactory.phasedLoader(dataset, monitor);
            case "parallel":
                return LoaderFactory.parallelLoader(dataset, monitor);
            default:
                throw new LpException("Unknown loader type '{}'",
                        configuration.getLoader());
        }
    }

}
