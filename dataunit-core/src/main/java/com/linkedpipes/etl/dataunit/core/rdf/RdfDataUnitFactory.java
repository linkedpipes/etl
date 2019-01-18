package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.DataUnitConfiguration;
import com.linkedpipes.etl.dataunit.core.pipeline.PipelineModel;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import org.osgi.service.component.annotations.Component;

@Component(
        immediate = true,
        service = {DataUnitFactory.class, PipelineExecutionObserver.class})
public class RdfDataUnitFactory
        implements DataUnitFactory, PipelineExecutionObserver {

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
        DataUnitConfiguration configuration =
                loadDataUnitConfiguration(dataUnit, definition);

        for (String type : configuration.getTypes()) {
            switch (type) {
                case LP_PIPELINE.SINGLE_GRAPH_DATA_UNIT:
                    return new DefaultSingleGraphDataUnit(
                            configuration,
                            repositoryManager,
                            pipelineModel.getSourcesFor(dataUnit)
                    );
                case LP_PIPELINE.GRAPH_LIST_DATA_UNIT:
                    return new DefaultGraphListDataUnit(
                            configuration,
                            repositoryManager,
                            pipelineModel.getSourcesFor(dataUnit));
                case LP_PIPELINE.CHUNKED_TRIPLES_DATA_UNIT:
                    return new DefaultChunkedTriples(
                            configuration,
                            pipelineModel.getSourcesFor(dataUnit));
                default:
                    break;
            }
        }
        return null;
    }

    private DataUnitConfiguration loadDataUnitConfiguration(
            String dataUnit, RdfSource definition)
            throws LpException {
        DataUnitConfiguration configuration =
                new DataUnitConfiguration(dataUnit);
        RdfToPojoLoader.load(definition, dataUnit, configuration);
        return configuration;
    }

    @Override
    public void onPipelineBegin(
            String pipeline, RdfSource definition) throws LpException {
        pipelineModel.load(pipeline, definition);
        if (pipelineModel.getRdfRepository() == null) {
            return;
        }
        loadFactoryConfiguration(definition);
        initializeRepositoryManager();
    }

    private void loadFactoryConfiguration(RdfSource definition)
            throws RdfException {
        factoryConfiguration = new FactoryConfiguration();
        RdfToPojoLoader.load(
                definition,
                pipelineModel.getRdfRepository(),
                factoryConfiguration);
    }

    private void initializeRepositoryManager() {
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
        repositoryManager.closeAll();
        repositoryManager = null;
    }

}
