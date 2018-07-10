package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.Model;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

class MergeHierarchy {

    private static final Logger LOG =
            LoggerFactory.getLogger(MergeHierarchy.class);

    private final DescriptionLoader descriptionLoader = new DescriptionLoader();

    private Description description;

    private Model templateModel;

    private Model.Entity templateEntity;

    Collection<Statement> merge(
            Collection<Collection<Statement>> configurationsRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph) throws BaseException {
        this.description = this.descriptionLoader.load(descriptionRdf);
        this.templateModel = null;
        this.templateEntity = null;
        //
        for (Collection<Statement> configurationRdf : configurationsRdf) {
            if (this.templateModel == null) {
                this.loadModel(configurationRdf);
                continue;
            }
            // Create instance of current configuration.
            Model childModel = Model.create(configurationRdf);
            Model.Entity childEntity = childModel.select(
                    null, RDF.TYPE, this.description.getType()).single();
            if (childEntity == null) {
                LOG.warn("Missing configuration entity.");
                continue;
            }
            if (this.description.getControl() == null) {
                mergePerPredicate(childModel, childEntity);
            } else {
                boolean continueEvaluation =
                        mergerGlobalControl(childModel,childEntity);
                if (!continueEvaluation) {
                    break;
                }
            }
        }
        if (this.templateModel == null) {
            LOG.warn("No configuration found.");
            return Collections.EMPTY_LIST;
        }
        this.templateModel.updateResources(baseIri + "/");
        return this.templateModel.asStatements(this.templateEntity, graph);
    }

    private void loadModel(Collection<Statement> statements) {
        this.templateModel = Model.create(statements);
        this.templateEntity = this.templateModel.select(
                null, RDF.TYPE, this.description.getType()).single();
        if (this.templateEntity == null) {
            this.templateModel = null;
            LOG.warn("Missing configuration entity for: {}",
                    this.description.getType());
        }
    }

    private void mergePerPredicate(Model childModel, Model.Entity childEntity)
            throws BaseException {
        // Merge from children to templateModel.
        if (this.description.getMembers().isEmpty()) {
            // We should load all properties from children, ald overwrite
            // those in parent -> this can be done by simply swapping
            //                    the configurations.
            this.templateModel = childModel;
            this.templateEntity = childEntity;
        } else {
            // Use from definition.
            for (Description.Member member : this.description.getMembers()) {
                mergeEntities(member, this.templateEntity, childEntity);
            }
        }
    }

    private boolean mergerGlobalControl(
            Model childModel, Model.Entity childConfiguration) {
        String control = childConfiguration.getPropertyAsStr(
                this.description.getControl());
        if (LP_OBJECTS.INHERIT.equals(control)) {
            // Skip loading this object.
            return true;
        }
        if (LP_OBJECTS.INHERIT_AND_FORCE.equals(control)) {
            // Do not load anything from this instance, not any further.
            this.templateEntity.setIri(
                    this.description.getControl(), LP_OBJECTS.FORCED);
            return false;
        }
        // Merge child to model.
        this.templateModel = childModel;
        this.templateEntity = childConfiguration;
        //
        if (LP_OBJECTS.FORCE.equals(control)) {
            // Do not load anything in any further instance.
            this.templateEntity.setIri(
                    this.description.getControl(), LP_OBJECTS.FORCED);
            return false;
        }
        return true;
    }


    /**
     * Based on the configuration merge a single property value from
     * childrenEntity to the parentEntity.
     *
     * @param member
     * @param parent
     * @param children
     */
    private void mergeEntities(
            Description.Member member,
            Model.Entity parent,
            Model.Entity children) throws BaseException {
        String parentControl = parent.getPropertyAsStr(member.getControl());
        if (LP_OBJECTS.FORCED.equals(parentControl)) {
            return;
        }
        String childControl = children.getPropertyAsStr(member.getControl());
        if (childControl == null) {
            childControl = LP_OBJECTS.NONE;
        }
        Value childValue = children.getProperty(member.getProperty());
        if (childValue == null) {
            return;
        }
        //
        switch (childControl) {
            case LP_OBJECTS.INHERIT:
                parent.setIri(member.getControl(), LP_OBJECTS.NONE);
                break;
            case LP_OBJECTS.FORCE:
                parent.replace(
                        member.getProperty(), children, childValue, true);
                parent.setIri(member.getControl(), LP_OBJECTS.FORCED);
                break;
            case LP_OBJECTS.INHERIT_AND_FORCE:
                parent.setIri(member.getControl(), LP_OBJECTS.FORCED);
                break;
            case LP_OBJECTS.FORCED:
                throw new BaseException("Unexpected FORCED property");
            case LP_OBJECTS.NONE:
                parent.replace(
                        member.getProperty(), children, childValue, true);
                parent.setIri(member.getControl(), LP_OBJECTS.NONE);
                break;
            default:
                throw new BaseException("unexpected property: " + childControl);
        }
    }

}
