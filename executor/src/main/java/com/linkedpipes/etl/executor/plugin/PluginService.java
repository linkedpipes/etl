package com.linkedpipes.etl.executor.plugin;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.plugin.osgi.OsgiPluginService;

import java.io.File;
import java.util.List;

public interface PluginService {

    void startService(File storageDirectory) throws ExecutorException;

    void stopService();

    void loadLibraries(File directory) throws ExecutorException;

    void loadPlugins(File directory) throws ExecutorException;

    List<PipelineExecutionObserver> getPipelineListeners()
            throws ExecutorException;

    List<DataUnitFactory> getDataUnitFactories()
            throws ExecutorException;

    PluginHolder getPlugin(String iri) throws ExecutorException;

    static PluginService osgi() {
        return new OsgiPluginService();
    }

}
