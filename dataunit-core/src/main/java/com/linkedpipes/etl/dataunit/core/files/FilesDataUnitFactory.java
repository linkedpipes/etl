package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.net.URI;

@Component(immediate = true, service = {DataUnitFactory.class})
public class FilesDataUnitFactory implements DataUnitFactory {

    @Override
    public ManageableDataUnit create(String dataUnit, String graph,
            RdfSource definition) throws LpException {
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
                case LP_PIPELINE.FILE_DATA_UNIT:
                    return new DefaultFilesDataUnit(
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

}
