package com.linkedpipes.etl.dataunit.system;

import com.linkedpipes.etl.dataunit.system.files.FilesDataUnitConfiguration;
import com.linkedpipes.etl.dataunit.system.files.SystemFilesDataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.utils.core.entity.EntityLoader;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author Petr Škoda
 */
@Component(immediate = true, service = {DataUnitFactory.class})
public class SystemPlugin implements DataUnitFactory {

    @Override
    public ManagableDataUnit create(SparqlSelect definition, String resourceUri, String graph)
            throws CreationFailed {
        // Load configuration.
        final FilesDataUnitConfiguration config = new FilesDataUnitConfiguration(resourceUri);
        try {
            EntityLoader.load(definition, resourceUri, graph, config);
        } catch (EntityLoader.LoadingFailed ex) {
            throw new CreationFailed(String.format("Can't load configuration for: %s", resourceUri), ex);
        }
        // Based on type create instance.
        for (String type : config.getTypes()) {
            switch (type) {
                case "http://linkedpipes.com/ontology/dataUnit/system/1.0/files/DirectoryMirror":
                    return SystemFilesDataUnitFactory.create(config);
                default:
                    break;
            }
        }
        return null;
    }

}
