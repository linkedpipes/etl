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
     * Create a configuration that should be used by the children o
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
                RdfObjects.Entity control;
                try {
                    control = instance.getReference(member.getControl());
                } catch (Exception ex) {
                    // The configuration is missing -> do nothing.
                    continue;
                }
                switch (control.getResource().stringValue()) {
                    case NONE:
                    case INHERIT:
                        // Do nothing.
                        break;
                    case FORCE:
                    case INHERIT_AND_FORCE:
                        // We force value to children.
                        instance.delete(member.getControl());
                        instance.delete(member.getProperty());
                        instance.add(member.getControl(), vf.createIRI(FORCED));
                        break;
                    case FORCED:
                        // The value is forced by parent, do nothing with it.
                        break;
                }
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
        Model.Entity configuration = null;
        for (Collection<Statement> configurationRdf : configurationsRdf) {
            if (model == null) {
                // Create model.
                model = Model.create(configurationRdf);
                configuration = model.select(null, RDF.TYPE,
                        description.getType()).single();
                if (configuration == null) {
                    model = null;
                    LOG.warn("Skipping configuration due to missing " +
                            "configuration entity for: {}",
                            description.getType());
                }
                continue;
            }
            // Create children model.
            final Model childModel = Model.create(configurationRdf);
            final Model.Entity childConfiguration = childModel.select(null,
                    RDF.TYPE, description.getType()).single();
            if (childConfiguration == null) {
                LOG.warn("Skipping configuration due to missing " +
                        "configuration entity.");
                continue;
            }
            // Merge.
            for (ConfigDescription.Member member : description.getMembers()) {
                merge(member, configuration, childConfiguration);
            }
        }
        //
        if (model == null) {
            LOG.warn("Configuration model is empty.");
            return Collections.EMPTY_LIST;
        }
        //
        model.updateResources(baseIri);
        return model.asStatements(configuration, graph);
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
