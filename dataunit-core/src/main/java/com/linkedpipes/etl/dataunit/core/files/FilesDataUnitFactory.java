package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.dataunit.core.DataUnitConfiguration;
import com.linkedpipes.etl.dataunit.core.pipeline.PipelineModel;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import org.osgi.service.component.annotations.Component;

@Component(
        immediate = true,
        service = {DataUnitFactory.class, PipelineExecutionObserver.class})
public class FilesDataUnitFactory
        implements DataUnitFactory, PipelineExecutionObserver {

    private PipelineModel pipelineModel = new PipelineModel();

    @Override
    public ManageableDataUnit create(
            String dataUnit, String graph, RdfSource definition)
            throws LpException {
        DataUnitConfiguration configuration =
                loadConfiguration(dataUnit, definition);

        for (String type : configuration.getTypes()) {
            if (LP_PIPELINE.FILE_DATA_UNIT.equals(type)) {
                return new DefaultFilesDataUnit(
                        configuration,
                        pipelineModel.getSourcesFor(dataUnit));
            }
        }

        return null;
    }

    private DataUnitConfiguration loadConfiguration(
            String dataUnit, RdfSource definition)
            throws LpException {
        DataUnitConfiguration configuration =
                new DataUnitConfiguration(dataUnit);
        RdfToPojoLoader.load(definition, dataUnit, configuration);
        return configuration;
    }

    @Override
    public void onPipelineBegin(String pipeline,
            RdfSource definition) throws LpException {
        this.pipelineModel.load(pipeline, definition);
    }

    @Override
    public void onPipelineEnd() {
        this.pipelineModel.clear();
    }

}
