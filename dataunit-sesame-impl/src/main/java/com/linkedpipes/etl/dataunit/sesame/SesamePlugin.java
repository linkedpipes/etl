package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.executor.api.v1.Plugin;
import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnitFactory;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.utils.core.entity.EntityLoader;
import java.util.List;
import java.util.Map;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
@Component(immediate = true,
        service = {DataUnitFactory.class, Plugin.ExecutionListener.class})
public class SesamePlugin implements DataUnitFactory, Plugin.ExecutionListener {

    private static final Logger LOG
            = LoggerFactory.getLogger(SesamePlugin.class);

    /**
     * Select used to get resource IRI of the configuration class.
     */
    private final static String QUERY_SELECT_CONFIGURATION_RESOURCE = ""
            + "prefix skos: <http://www.w3.org/2004/02/skos/core#>\n"
            + "prefix lp: <http://linkedpipes.com/ontology/>\n"
            + "SELECT ?s WHERE {\n"
            + " ?s a <" + LINKEDPIPES.REPOSITORY + "> ;\n"
            + "    a <http://linkedpipes.com/ontology/dataUnit/sesame/1.0/Repository> .\n"
            + "}";

    private boolean initialized = false;

    /**
     * Configuration for the sesame repository. This configuration is loaded
     * for every execution.
     */
    private FactoryConfiguration configuration = null;

    /**
     * Repository for RDF data used by the execution.
     */
    private Repository sharedRepository = null;

    public SesamePlugin() {
    }

    @Override
    public ManagableDataUnit create(SparqlSelect definition, String resourceIri,
            String graph) throws RdfException {
        if (!initialized) {
            return null;
        }
        // Load configurattion.
        final RdfDataUnitConfiguration dataUnitConfiguration
                = new RdfDataUnitConfiguration(resourceIri);
        try {
            EntityLoader.load(definition, resourceIri, graph,
                    dataUnitConfiguration);
        } catch (RdfException ex) {
            throw ExceptionFactory.wrap(ex, "Can't load configuration for: {}",
                    resourceIri);
        }
        // Create data unit.
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        for (String type : dataUnitConfiguration.getTypes()) {
            switch (type) {
                case "http://linkedpipes.com/ontology/dataUnit/sesame/1.0/rdf/SingleGraph":
                    return new SingleGraphDataUnitImpl(
                            valueFactory.createIRI(resourceIri),
                            sharedRepository,
                            dataUnitConfiguration);
                case "http://linkedpipes.com/ontology/dataUnit/sesame/1.0/rdf/GraphList":
                    return new GraphListDataUnitImpl(
                            valueFactory.createIRI(resourceIri),
                            sharedRepository,
                            dataUnitConfiguration);
                default:
                    break;
            }
        }
        return null;
    }

    @Override
    public void onExecutionBegin(SparqlSelect definition, String resourceIri,
            String graph) throws RdfException {
        final List<Map<String, String>> resourceList;
        try {
            resourceList = definition.executeSelect(
                    QUERY_SELECT_CONFIGURATION_RESOURCE);
        } catch (RdfException ex) {
            throw ExceptionFactory.wrap(ex, "Can't query definition.");
        }
        if (resourceList.isEmpty()) {
            // This factory is not used in this pipeline execution.
            LOG.info("SesameDataUnit is not used in this execution.");
            return;
        } else if (resourceList.size() != 1) {
            // Everything else than just one result is a problem,
            // as we do not known what to load.
            throw ExceptionFactory.initializationFailed(
                    "Invalid number of results: {}", resourceList.size());
        }
        // Load configuration - defensive style (load and then set).
        final FactoryConfiguration newConfiguration = new FactoryConfiguration();
        try {
            EntityLoader.load(definition, resourceList.get(0).get("s"), graph,
                    newConfiguration);
        } catch (RdfException ex) {
            throw ExceptionFactory.wrap(ex, "Can't load configuration for: {}",
                    resourceList.get(0).get("s"), ex);
        }
        configuration = newConfiguration;
        // Create shared repository.
        sharedRepository = new SailRepository(
                new NativeStore(configuration.getRepositoryDirectory()));
        try {
            sharedRepository.initialize();
        } catch (RepositoryException ex) {
            sharedRepository = null;
            throw ExceptionFactory.initializationFailed(
                    "Can't create shared repository.", ex);
        }
        initialized = true;
    }

    @Override
    public void onExecutionEnd() {
        // Destroy shared repository.
        if (sharedRepository != null) {
            try {
                sharedRepository.shutDown();
            } catch (RepositoryException ex) {
                LOG.error("Can't close repository.", ex);
            }
            sharedRepository = null;
        }
        initialized = false;
    }

}
