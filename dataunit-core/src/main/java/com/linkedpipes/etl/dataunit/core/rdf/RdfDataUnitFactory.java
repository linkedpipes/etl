package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

/**
 * The factory is also used to store and hold shared repository.
 */
@Component(immediate = true,
        service = {DataUnitFactory.class, PipelineExecutionObserver.class})
public class RdfDataUnitFactory
        implements DataUnitFactory, PipelineExecutionObserver {

    private static final Logger LOG =
            LoggerFactory.getLogger(RdfDataUnitFactory.class);

    private FactoryConfiguration configuration;

    private Repository repository;

    @Override
    public ManageableDataUnit create(String dataUnit, String graph,
            RdfSource definition) throws LpException {
        if (configuration == null) {
            return null;
        }
        // Load configuration.
        final Configuration configuration = new Configuration(dataUnit, graph);
        try {
            RdfUtils.load(definition, configuration, dataUnit, graph,
                    String.class);
            configuration.loadSources(definition);
        } catch (RdfUtilsException ex) {
            throw new LpException("Can't load configuration for {} in {}",
                    dataUnit, graph, ex);
        }
        // Search for known types.
        for (String type : configuration.getTypes()) {
            switch (type) {
                case LP_PIPELINE.SINGLE_GRAPH_DATA_UNIT:
                    return new DefaultSingleGraphDataUnit(
                            configuration.getBinding(),
                            dataUnit,
                            repository,
                            configuration.getSources(),
                            dataUnit
                    );
                case LP_PIPELINE.GRAPH_LIST_DATA_UNIT:
                    return new DefaultGraphListDataUnit(
                            configuration.getBinding(),
                            dataUnit,
                            repository,
                            configuration.getSources(),
                            dataUnit
                    );
                case LP_PIPELINE.CHUNKED_TRIPLES_DATA_UNIT:
                    return new DefaultChunkedTriples(
                            configuration.getBinding(),
                            dataUnit,
                            new File(URI.create(
                                    configuration.getWorkingDirectory())),
                            configuration.getSources());
                default:
                    break;
            }
        }
        return null;
    }

    @Override
    public void onPipelineBegin(String pipeline, String graph,
            RdfSource definition) throws LpException {
        try {
            final String resource = RdfUtils.sparqlSelectSingle(definition,
                    getConfigurationQuery(pipeline, graph), "r");
            final FactoryConfiguration config = new FactoryConfiguration();
            RdfLoader.load(definition, configuration,
                    resource, graph, String.class);
            // Save at the end.
            this.configuration = config;
        } catch (RdfUtilsException ex) {
            LOG.debug("RDF not detected for this execution.", ex);
            return;
        }
        //
        repository = new SailRepository(
                new NativeStore(configuration.getDirectory()));
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            repository = null;
            throw new LpException("Can't create RDF repository.", ex);
        }
    }

    @Override
    public void onPipelineEnd() {
        configuration = null;
        //
        if (repository == null) {
            return;
        }
        try {
            LOG.info("Saving repository ... ");
            repository.shutDown();
            LOG.info("Saving repository ... done");
        } catch (RepositoryException ex) {
            LOG.error("Can't close repository.", ex);
            repository = null;
            return;
        }
        // Delete the directory.
        final File workingDirectory = configuration.getDirectory();
        FileUtils.deleteQuietly(workingDirectory);
        // It may take some time before the file is released.
        while (workingDirectory.exists()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
            }
            FileUtils.deleteQuietly(workingDirectory);
        }
        LOG.info("Working directory deleted.");
    }

    private String getConfigurationQuery(String pipeline, String graph) {
        return "SELECT ?r WHERE { GRAPH <" + graph + "> { " +
                " <" + pipeline + ">" + LP_PIPELINE.HAS_REPOSITORY + "> ?r ." +
                "} }";
    }

}
