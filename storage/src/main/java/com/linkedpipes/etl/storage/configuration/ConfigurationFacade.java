package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.Model;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Petr Škoda
 */
public class ConfigurationFacade {

    static final String NONE =
            "http://plugins.linkedpipes.com/resource/configuration/None";

    static final String INHERIT =
            "http://plugins.linkedpipes.com/resource/configuration/Inherit";

    static final String FORCE =
            "http://plugins.linkedpipes.com/resource/configuration/Force";

    static final String INHERIT_AND_FORCE =
            "http://plugins.linkedpipes.com/resource/configuration/InheritAndForce";

    static final String FORCED =
            "http://plugins.linkedpipes.com/resource/configuration/Forced";

    private static final Logger LOG =
            LoggerFactory.getLogger(ConfigurationFacade.class);

    private ConfigurationFacade() {

    }

    /**
     * Create a configuration that should be used by the children.
     *
     * @param configurationRdf Configuration.
     * @param descriptionRdf Description of the configuration.
     * @param baseIri Resource used for generated configuration.
     * @param graph Graph used for generated configuration.
     * @return
     */
    public static Collection<Statement> createTemplateConfiguration(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph) throws BaseException {
        final ConfigDescription description = loadDescription(descriptionRdf);
        // Load configuration.
        final RdfObjects config = new RdfObjects(configurationRdf);
        // Update configuration.
        final ValueFactory vf = SimpleValueFactory.getInstance();
        config.getTyped(description.getType()).forEach((instance) -> {
            for (ConfigDescription.Member member : description.getMembers()) {
                // Set all to inherit.
                instance.deleteReferences(member.getControl());
                instance.add(member.getControl(), vf.createIRI(INHERIT));
            }
        });
        // Update resources and return.
        config.updateTypedResources(baseIri);
        return config.asStatements(graph);
    }

    /**
     * Compute and return effective configuration for the given list of the
     * configuration.
     *
     * The configuration must be given in natural order - ie. parent before
     * children. So the instance configuration is the last one.
     *
     * @param configurationsRdf
     * @param descriptionRdf
     * @param baseIri Resource used for generated configuration.
     * @param graph Graph used for generated configuration.
     * @return
     */
    public static Collection<Statement> merge(
            Collection<Collection<Statement>> configurationsRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph) throws BaseException {
        final ConfigDescription description = loadDescription(descriptionRdf);
        Model model = null;
        Model.Entity modelConfiguration = null;
        for (Collection<Statement> configurationRdf : configurationsRdf) {
            if (model == null) {
                // First configuration - just load.
                model = Model.create(configurationRdf);
                modelConfiguration = model.select(null, RDF.TYPE,
                        description.getType()).single();
                if (modelConfiguration == null) {
                    model = null;
                    LOG.warn("Skipping configuration due to missing " +
                            "configuration entity for: {}",
                            description.getType());
                }
                continue;
            }
            // Read global control.
            if (description.getControl() != null) {
                final String control = modelConfiguration.getPropertyAsStr(
                        description.getControl());
                if (control.equals(FORCE) || control.equals(INHERIT_AND_FORCE)) {
                    // We force all - there is no need to load anything
                    // else.
                    break;
                }
            }
            // Create instance of current configuration.
            final Model childModel = Model.create(configurationRdf);
            final Model.Entity childConfiguration = childModel.select(null,
                    RDF.TYPE, description.getType()).single();
            if (childConfiguration == null) {
                LOG.warn("Skipping configuration due to missing " +
                        "configuration entity.");
                continue;
            }
            String control = null;
            if (description.getControl() != null) {
                control = childConfiguration.getPropertyAsStr(
                        description.getControl());
            }
            if (FORCE.equals(control)) {
                // We inherit all -> do not load anything from this object.
                continue;
            } else if (INHERIT_AND_FORCE.equals(control)) {
                // We do not load anything not (inherit) or any further (force).
                modelConfiguration.setIri(description.getControl(), FORCED);
                break;
            }
            // Merge from children to model.
            for (ConfigDescription.Member member : description.getMembers()) {
                merge(member, modelConfiguration, childConfiguration);
            }
            if (FORCE.equals(control)) {
                // We force -> do not load anything. We just need to set
                // inheritance of all to forced.
                modelConfiguration.setIri(description.getControl(), FORCED);
                break;
            }
        }
        //
        if (model == null) {
            LOG.warn("Configuration model is empty.");
            return Collections.EMPTY_LIST;
        }
        //
        model.updateResources(baseIri);
        return model.asStatements(modelConfiguration, graph);
    }

    /**
     * Load and return configuration description.
     *
     * @param descriptionRdf
     * @return
     */
    private static ConfigDescription loadDescription(
            Collection<Statement> descriptionRdf) throws BaseException {
        final ConfigDescription description = new ConfigDescription();
        PojoLoader.loadOfType(descriptionRdf, ConfigDescription.TYPE,
                description);
        if (description.getType() == null) {
            throw new BaseException("Missing configuration type.");
        }
        return description;
    }

    /**
     * Based on the configuration merge a single property value from
     * childrenEntity to the parentEntity.
     *
     * @param member
     * @param parent
     * @param children
     */
    private static void merge(ConfigDescription.Member member,
            Model.Entity parent, Model.Entity children) {
        // Check parent options.
        // The parent can only FORCE(D) option, in such case
        // the value remains unchanged.
        final String parentControl = parent.getPropertyAsStr(
                member.getControl());
        if (FORCED.equals(parentControl)) {
            return;
        }
        // Check children options.
        String childrenControl = children.getPropertyAsStr(
                member.getControl());
        if (childrenControl == null) {
            childrenControl = NONE;
        }
        switch (childrenControl) {
            case INHERIT:
                // Preserve value from parent.
                parent.setIri(member.getControl(), NONE);
                break;
            case FORCE:
                // Use children's value and force it to grandchildren.
                parent.replace(member.getProperty(), children,
                        children.getProperty(member.getProperty()), true);
                parent.setIri(member.getControl(), FORCED);
                break;
            case INHERIT_AND_FORCE:
                // Use parent value and force it to children.
                parent.setIri(member.getControl(), FORCED);
                break;
            case FORCED:
                LOG.error("Unexpected forced property.");
                parent.setIri(member.getControl(), NONE);
                break;
            case NONE:
            default:
                // Use children's value.
                parent.replace(member.getProperty(), children,
                        children.getProperty(member.getProperty()), true);
                parent.setIri(member.getControl(), NONE);
                break;
        }
    }

}
