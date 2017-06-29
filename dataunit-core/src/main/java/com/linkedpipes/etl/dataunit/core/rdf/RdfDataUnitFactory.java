package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.pipeline.PipelineInfo;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import org.eclipse.rdf4j.repository.Repository;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

import static com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE.RDF_REPOSITORY;

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

    private RepositoryManager repositoryManager;

    @Override
    public ManageableDataUnit create(String dataUnit, String graph,
            RdfSource definition) throws LpException {
        if (configuration == null) {
            return null;
        }
        // Load configuration.
        final Configuration configuration = new Configuration(dataUnit, graph);
        try {
            RdfUtils.load(definition, dataUnit, graph, configuration);
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
                            getRepository(configuration),
                            configuration.getSources(),
                            dataUnit
                    );
                case LP_PIPELINE.GRAPH_LIST_DATA_UNIT:
                    return new DefaultGraphListDataUnit(
                            configuration.getBinding(),
                            dataUnit,
                            getRepository(configuration),
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

    private Repository getRepository(Configuration configuration)
            throws LpException {
        return repositoryManager.getRepository(configuration);
    }

    @Override
    public void onPipelineBegin(String pipeline, String graph,
            RdfSource definition) throws LpException {
        repositoryManager = null;
        try {
            final String resource = RdfUtils.sparqlSelectSingle(definition,
                    getConfigurationQuery(pipeline, graph), "r");
            final FactoryConfiguration config = new FactoryConfiguration();
            RdfUtils.load(definition, resource, graph, config);
            // Save at the end.
            this.configuration = config;
        } catch (RdfUtilsException ex) {
            LOG.debug("RDF not detected for this execution.", ex);
            return;
        }
        //
        initializeRepositoryManager(definition, pipeline, graph);
    }

    private void initializeRepositoryManager(RdfSource source, String pipeline,
            String graph) throws LpException {
        PipelineInfo pipelineInfo = new PipelineInfo(pipeline, graph, source);
        String rdfRepositoryPolicy;
        try {
            rdfRepositoryPolicy = pipelineInfo.getRdfRepositoryPolicy();
        } catch (RdfUtilsException ex) {
            throw new LpException("Can't query pipeline model.", ex);
        }
        repositoryManager = new RepositoryManager(
                rdfRepositoryPolicy, configuration.getDirectory());
    }

    @Override
    public void onPipelineEnd() {
        if (repositoryManager == null) {
            return;
        }
        repositoryManager.close();
    }

    private String getConfigurationQuery(String pipeline, String graph) {
        return "SELECT ?r WHERE { GRAPH <" + graph + "> { " +
                " <" + pipeline + "> <" + LP_PIPELINE.HAS_REPOSITORY +
                "> ?r ." +
                " ?r a <" + RDF_REPOSITORY + "> . } }";
    }

}
