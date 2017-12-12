package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.pipeline.PipelineModel;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import org.eclipse.rdf4j.repository.Repository;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

/**
 * The factory is also used to store and hold shared repository.
 */
@Component(
        immediate = true,
        service = {DataUnitFactory.class, PipelineExecutionObserver.class})
public class RdfDataUnitFactory
        implements DataUnitFactory, PipelineExecutionObserver {

    private static final Logger LOG =
            LoggerFactory.getLogger(RdfDataUnitFactory.class);

    private FactoryConfiguration factoryConfiguration;

    private RepositoryManager repositoryManager;

    private PipelineModel pipelineModel = new PipelineModel();

    @Override
    public ManageableDataUnit create(
            String dataUnit, String graph, RdfSource definition)
            throws LpException {
        if (factoryConfiguration == null) {
            return null;
        }
        // Load factoryConfiguration.
        Configuration configuration = loadDataUnitConfiguration(
                dataUnit, graph, definition);
        // Search for known types.
        for (String type : configuration.getTypes()) {
            switch (type) {
                case LP_PIPELINE.SINGLE_GRAPH_DATA_UNIT:
                    return new DefaultSingleGraphDataUnit(
                            configuration.getBinding(),
                            dataUnit,
                            getRepository(configuration),
                            pipelineModel.getSourcesFor(dataUnit),
                            dataUnit
                    );
                case LP_PIPELINE.GRAPH_LIST_DATA_UNIT:
                    return new DefaultGraphListDataUnit(
                            configuration.getBinding(),
                            dataUnit,
                            getRepository(configuration),
                            pipelineModel.getSourcesFor(dataUnit),
                            dataUnit
                    );
                case LP_PIPELINE.CHUNKED_TRIPLES_DATA_UNIT:
                    return new DefaultChunkedTriples(
                            configuration.getBinding(),
                            dataUnit,
                            new File(URI.create(
                                    configuration.getWorkingDirectory())),
                            pipelineModel.getSourcesFor(dataUnit));
                default:
                    break;
            }
        }
        return null;
    }

    private Configuration loadDataUnitConfiguration(
            String dataUnit, String graph, RdfSource definition)
            throws LpException {
        Configuration configuration = new Configuration(dataUnit, graph);
        RdfToPojoLoader.load(definition, dataUnit, configuration);
        return configuration;
    }

    private Repository getRepository(Configuration configuration)
            throws LpException {
        return repositoryManager.getRepository(configuration);
    }

    @Override
    public void onPipelineBegin(String pipeline,
            RdfSource definition) throws LpException {
        pipelineModel.load(pipeline, definition);
        if (pipelineModel.getRdfRepository() == null) {
            LOG.info("RDF repository not detected.");
            return;
        }
        loadFactoryConfiguration(definition);
        initializeRepositoryManager();
    }

    private void loadFactoryConfiguration(RdfSource definition)
            throws RdfException {
        factoryConfiguration = new FactoryConfiguration();
        RdfToPojoLoader.load(
                definition, pipelineModel.getRdfRepository(),
                factoryConfiguration);
    }

    private void initializeRepositoryManager() throws LpException {
        repositoryManager = new RepositoryManager(
                pipelineModel.getRdfRepositoryPolicy(),
                pipelineModel.getRdfRepositoryType(),
                factoryConfiguration.getDirectory());
    }

    @Override
    public void onPipelineEnd() {
        pipelineModel.clear();
        if (repositoryManager == null) {
            return;
        }
        repositoryManager.close();
        repositoryManager = null;
    }

}
