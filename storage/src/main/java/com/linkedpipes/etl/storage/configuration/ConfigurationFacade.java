package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.Model;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

// TODO Add test where the instance configuration does not contains Configuration class.
// TODO Replace with function from RdfUtils module (EntityMerger).
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
     * @param inheritAll If true all control properties are set to inherit.
     * @return
     */
    public static Collection<Statement> createNewConfiguration(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph, boolean inheritAll)
            throws BaseException {
        final ConfigDescription description = loadDescription(descriptionRdf);
        // Load configuration.
        final RdfObjects config = new RdfObjects(configurationRdf);
        // Update configuration.
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final IRI inheritType;
        if (inheritAll) {
            inheritType = vf.createIRI(INHERIT);
        } else {
            inheritType = vf.createIRI(NONE);
        }
        config.getTyped(description.getType()).forEach((instance) -> {
            for (ConfigDescription.Member member : description.getMembers()) {
                // Set all to inherit.
                instance.deleteReferences(member.getControl());
                instance.add(member.getControl(), inheritType);
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
                // First configuration - just load, the template
                // configuration does not contains control.
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
            // Create instance of current configuration.
            final Model childModel = Model.create(configurationRdf);
            final Model.Entity childConfiguration = childModel.select(null,
                    RDF.TYPE, description.getType()).single();
            if (childConfiguration == null) {
                LOG.warn("Skipping configuration due to missing " +
                        "configuration entity.");
                continue;
            }
            // Read global control.
            final String control;
            if (description.getControl() != null) {
                control = childConfiguration.getPropertyAsStr(
                        description.getControl());
            } else {
                control = null;
            }
            // Check control.
            if (INHERIT.equals(control)) {
                // Do not load anything.
                continue;
            }
            if (INHERIT_AND_FORCE.equals(control)) {
                // Do not load anything from this instance, not any further.
                modelConfiguration.setIri(description.getControl(), FORCED);
                break;
            }
            // Merge from children to model.
            if (description.getMembers().isEmpty()) {
                // We should load all properties from children, ald overwrite
                // those in parent -> this can be done by simply swapping
                //                    the configurations.
                modelConfiguration = childConfiguration;
            } else {
                // Use from definition.
                for (ConfigDescription.Member member :
                        description.getMembers()) {
                    merge(member, modelConfiguration, childConfiguration);
                }
            }
            if (FORCE.equals(control)) {
                // Do not load anything in any further instance.
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
        ConfigDescription description = new ConfigDescription();
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
        String childrenControl = children.getPropertyAsStr(member.getControl());
        if (childrenControl == null) {
            childrenControl = NONE;
        }
        final Value childrenValue =
                children.getProperty(member.getProperty());
        if (childrenValue == null) {
            // In case of missing value ignore.
            return;
        }
        switch (childrenControl) {
            case INHERIT:
                // Preserve value from parent.
                parent.setIri(member.getControl(), NONE);
                break;
            case FORCE:
                // Use children's value and force it to grandchildren.
                parent.replace(member.getProperty(), children,
                        childrenValue, true);
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
                        childrenValue, true);
                parent.setIri(member.getControl(), NONE);
                break;
        }
    }

    /**
     * Designed to be used to merge configuration from instance to templates.
     */
    public static Collection<Statement> mergeFromBottom(
            Collection<Statement> templateRdf,
            Collection<Statement> instanceRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph) throws BaseException {
        ConfigDescription description = loadDescription(descriptionRdf);
        Model templateModel = Model.create(templateRdf);
        Model.Entity templateConfiguration = templateModel.select(
                null, RDF.TYPE, description.getType()).single();
        if (templateConfiguration == null) {
            LOG.warn("Skipping configuration due to missing " +
                            "configuration entity for: {}",
                    description.getType());
            return createCopy(instanceRdf, graph);
        }
        // Create instance of current configuration.
        Model instanceModel = Model.create(instanceRdf);
        Model.Entity instanceConfiguration = instanceModel.select(null,
                RDF.TYPE, description.getType()).single();
        if (instanceConfiguration == null) {
            LOG.warn("Skipping configuration due to missing " +
                    "configuration entity.");
            return createCopy(templateRdf, graph);
        }
        // Handle global control.
        {
            String templateControl = null;
            if (description.getControl() != null) {
                templateControl = templateConfiguration.getPropertyAsStr(
                        description.getControl());
            }
            // Check control.
            if (INHERIT.equals(templateControl)) {
                // The configuration of the template is inherited from
                // another level of template. So we skip merging
                // with this level of template.
                return createCopy(instanceRdf, graph);
            }
            if (INHERIT_AND_FORCE.equals(templateControl)) {
                // We need to load configuration from another level of template.
                return Collections.emptyList();
            }
            if (FORCE.equals(templateControl)) {
                return createCopy(templateRdf, graph);
            }
            String instanceControl = null;
            if (description.getControl() != null) {
                instanceControl = instanceConfiguration.getPropertyAsStr(
                        description.getControl());
            }
            if (INHERIT.equals(instanceControl) ||
                    INHERIT_AND_FORCE.equals(instanceControl)) {
                // Instance inherit from parent.
                return createCopy(templateRdf, graph);
            }
        }
        // Merge child and template.
        if (description.getMembers().isEmpty()) {
            // We use the child's configuration.
            return createCopy(instanceRdf, graph);
        } else {
            for (ConfigDescription.Member member : description.getMembers()) {
                mergeFromBottom(member, templateConfiguration,
                        instanceConfiguration);
            }
        }
        //
        instanceModel.updateResources(baseIri);
        return instanceModel.asStatements(instanceConfiguration, graph);
    }

    private static Collection<Statement> createCopy(Collection<Statement> data,
            IRI graph) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Collection<Statement> output = new ArrayList<>(data.size());
        for (Statement statement : data) {
            output.add(valueFactory.createStatement(
                    statement.getSubject(),
                    statement.getPredicate(),
                    statement.getObject(),
                    graph
            ));
        }
        return output;
    }

    /**
     * Merge member into !instance!.
     */
    private static void mergeFromBottom(ConfigDescription.Member member,
            Model.Entity template, Model.Entity instance) {
        // First check if template does not force values to instance.
        String templateControl = template.getPropertyAsStr(member.getControl());
        Value templateValue = template.getProperty(member.getProperty());
        if (FORCE.equals(templateControl)) {
            instance.replace(member.getProperty(), instance, templateValue,
                    true);
            instance.setIri(member.getControl(), FORCED);
            return;
        }
        if (INHERIT_AND_FORCE.equals(templateControl)) {
            // Remove value - the value will be load from next level template.
            template.replace(member.getProperty(), instance,  null, false);
            template.setIri(member.getControl(), FORCED);
            return;
        }
        // If the value is missing we need to load if from a template.
        // This can happen if the instance has INHERIT_AND_FORCE control.
        Value instanceValue = instance.getProperty(member.getProperty());
        if (instanceValue == null) {
            instance.replace(member.getProperty(), instance, templateValue,
                    true);
            return;
        }
        // Instance can also inherit on demand.
        String instanceControl = instance.getPropertyAsStr(member.getControl());
        if (INHERIT.equals(instanceControl)) {
            instance.replace(member.getProperty(), instance, templateValue,
                    true);
            instance.setIri(member.getControl(), NONE);
        }
        // In every other case we keep value from the instance, so just check
        // the control.
        if (instanceControl == null) {
            instance.setIri(member.getControl(), NONE);
        }
    }

}
