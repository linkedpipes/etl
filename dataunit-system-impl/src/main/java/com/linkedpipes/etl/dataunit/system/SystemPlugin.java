package com.linkedpipes.etl.dataunit.system;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.rdf.PojoLoader;
import org.osgi.service.component.annotations.Component;

/**
 * @author Petr Škoda
 */
@Component(immediate = true, service = {DataUnitFactory.class})
public class SystemPlugin implements DataUnitFactory {

    @Override
    public ManageableDataUnit create(SparqlSelect definition,
            String resourceIri, String graph) throws RdfException {
        // Load configuration.
        final FilesDataUnitConfiguration config
                = new FilesDataUnitConfiguration(resourceIri);
        PojoLoader.load(definition, resourceIri, graph, config);
        // Based on type create instance.
        for (String type : config.getTypes()) {
            switch (type) {
                case "http://linkedpipes.com/ontology/dataUnit/system/1.0/files/DirectoryMirror":
                    return new FilesDataUnitImpl(config);
                default:
                    break;
            }
        }
        return null;
    }

}
