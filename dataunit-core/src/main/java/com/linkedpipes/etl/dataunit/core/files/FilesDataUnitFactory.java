package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.dataunit.core.pipeline.PipelineModel;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.net.URI;

@Component(
        immediate = true,
        service = {DataUnitFactory.class, PipelineExecutionObserver.class})
public class FilesDataUnitFactory
        implements DataUnitFactory, PipelineExecutionObserver {

    private Configuration dataUnitConfiguration;

    // TODO Share PipelineModel instance with RdfDataUnitFactory.
    private PipelineModel pipelineModel = new PipelineModel();

    @Override
    public ManageableDataUnit create(
            String dataUnit, String graph, RdfSource definition)
            throws LpException {
        loadConfiguration(dataUnit, graph, definition);
        ManageableDataUnit result = createDataUnit(dataUnit);
        dataUnitConfiguration = null;
        return result;
    }

    private void loadConfiguration(
            String dataUnit, String graph, RdfSource definition)
            throws LpException {
        dataUnitConfiguration = new Configuration(dataUnit, graph);
        RdfToPojoLoader.load(definition, dataUnit, dataUnitConfiguration);
    }

    private ManageableDataUnit createDataUnit(String dataUnit)
            throws LpException {
        for (String type : dataUnitConfiguration.getTypes()) {
            if (LP_PIPELINE.FILE_DATA_UNIT.equals(type)) {
                return createFilesDataUnit(dataUnit);
            }
        }
        return null;
    }

    private ManageableDataUnit createFilesDataUnit(String dataUnit)
            throws LpException {
        return new DefaultFilesDataUnit(
                dataUnitConfiguration.getBinding(),
                dataUnit, getWorkingDirectory(),
                pipelineModel.getSourcesFor(dataUnit));
    }

    private File getWorkingDirectory() {
        String uri = dataUnitConfiguration.getWorkingDirectory();
        return new File(URI.create(uri));
    }

    @Override
    public void onPipelineBegin(String pipeline,
            RdfSource definition) throws LpException {
        pipelineModel.load(pipeline, definition);
    }

    @Override
    public void onPipelineEnd() {
        pipelineModel.clear();
    }

}
