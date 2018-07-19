package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.storage.rdf.Model;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

class MergeFromBottom {

    private static final Logger LOG =
            LoggerFactory.getLogger(MergeFromBottom.class);

    private final DescriptionLoader descriptionLoader = new DescriptionLoader();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private Description description;

    private Model templateModel;

    private Model.Entity templateEntity;

    private Model instanceModel;

    private Model.Entity instanceEntity;

    private String baseIri;

    private IRI graph;

    /**
     * Designed to be used to merge configuration from instance to templates,
     * thus enabling another merge with other ancestor.
     */
    Collection<Statement> merge(
            Collection<Statement> templateRdf,
            Collection<Statement> instanceRdf,
            Description description,
            String baseIri, IRI graph) {
        this.initialize(description, baseIri, graph);
        //
        this.loadTemplateModel(templateRdf);
        if (this.templateEntity == null) {
            LOG.warn("Missing template configuration entity: {}",
                    this.description.getType());
            return this.collectTemplateModel();
        }
        this.loadInstanceModel(instanceRdf);
        if (this.instanceEntity == null) {
            LOG.warn("Missing instance configuration entity: {}",
                    this.description.getType());
            return this.collectTemplateModel();
        }
        if (description.getControl() == null) {
            return this.mergePerPredicate();
        } else {
            return this.mergeGlobal();
        }
    }

    private void initialize(Description description, String baseIri, IRI graph) {
        this.description = description;
        this.templateModel = null;
        this.templateEntity = null;
        this.instanceModel = null;
        this.baseIri = baseIri;
        this.graph = graph;
    }

    private void loadTemplateModel(Collection<Statement> statements) {
        this.templateModel = Model.create(statements);
        this.templateEntity = this.templateModel.select(
                null, RDF.TYPE, this.description.getType()).single();
        if (this.templateEntity == null) {
            this.templateModel = null;
            LOG.warn("Missing configuration entity for: {}",
                    this.description.getType());
        }
    }

    private void loadInstanceModel(Collection<Statement> statements) {
        this.instanceModel = Model.create(statements);
        this.instanceEntity = this.instanceModel.select(
                null, RDF.TYPE, this.description.getType()).single();
        if (this.instanceEntity == null) {
            this.instanceModel = null;
            LOG.warn("Missing configuration entity for: {}",
                    this.description.getType());
        }
    }

    private Collection<Statement> mergePerPredicate() {
        if (this.description.getMembers().isEmpty()) {
            return this.collectInstanceModel();
        }
        for (Description.Member member : this.description.getMembers()) {
            merge(member);
        }
        return this.collectInstanceModel();
    }

    private Collection<Statement> mergeGlobal() {
        String templateControl = this.templateEntity.getPropertyAsStr(
                this.description.getControl());
        if (LP_OBJECTS.INHERIT.equals(templateControl)) {
            // The configuration of the templateModel is inherited from
            // another level of templateModel. So we skip merging
            // with this level of templateModel.
            return this.collectInstanceModel();
        }
        if (LP_OBJECTS.INHERIT_AND_FORCE.equals(templateControl)) {
            // We need to load configuration from another level of templateModel.
            return Arrays.asList(
                    this.valueFactory.createStatement(
                            this.instanceEntity.getResource(),
                            RDF.TYPE,
                            this.description.getType()),
                    this.valueFactory.createStatement(
                            this.instanceEntity.getResource(),
                            this.description.getControl(),
                            this.valueFactory.createIRI(
                                    LP_OBJECTS.INHERIT_AND_FORCE)));
        }
        if (LP_OBJECTS.FORCE.equals(templateControl)) {
            return this.collectTemplateModel(LP_OBJECTS.FORCED);
        }
        String instanceControl = null;
        if (this.description.getControl() != null) {
            instanceControl = this.instanceEntity.getPropertyAsStr(
                    this.description.getControl());
        }
        if (LP_OBJECTS.INHERIT.equals(instanceControl)) {
            return this.collectTemplateModel();
        }
        if (LP_OBJECTS.INHERIT_AND_FORCE.equals(instanceControl)) {
            return this.collectTemplateModel(LP_OBJECTS.FORCED);
        }
        // We use the child's configuration.
        return this.collectInstanceModel();
    }

    private Collection<Statement> collectTemplateModel() {
        this.templateModel.updateResources(this.baseIri + "/");
        return this.templateModel.asStatements(this.templateEntity, this.graph);
    }

    private Collection<Statement> collectTemplateModel(String control) {
        this.templateModel.updateResources(this.baseIri + "/");
        this.templateEntity.set(
                this.description.getControl(),
                this.valueFactory.createIRI(control));
        return this.templateModel.asStatements(this.templateEntity, this.graph);
    }

    private Collection<Statement> collectInstanceModel() {
        this.instanceModel.updateResources(this.baseIri + "/");
        return this.instanceModel.asStatements(this.instanceEntity, this.graph);
    }

    private void merge(Description.Member member) {
        // First check if templateModel does not force values to instance.
        String templateControl =
                this.templateEntity.getPropertyAsStr(member.getControl());
        Value templateValue =
                this.templateEntity.getProperty(member.getProperty());

        if (LP_OBJECTS.FORCE.equals(templateControl)) {
            this.instanceEntity.replace(
                    member.getProperty(),
                    this.instanceEntity,
                    templateValue,
                    true);
            this.instanceEntity.setIri(member.getControl(), LP_OBJECTS.FORCED);
            return;
        }
        if (LP_OBJECTS.INHERIT_AND_FORCE.equals(templateControl)) {
            // Remove value - the value will be load from next templateModel.
            this.instanceEntity.replace(
                    member.getProperty(),
                    this.instanceEntity,
                    null,
                    false);
            this.instanceEntity.setIri(member.getControl(), LP_OBJECTS.FORCED);
            return;
        }
        // If the value is missing we need to load if from a templateModel.
        // This can happen if the instance has INHERIT_AND_FORCE control.
        Value instanceValue =
                this.instanceEntity.getProperty(member.getProperty());
        if (instanceValue == null) {
            this.instanceEntity.replace(
                    member.getProperty(),
                    this.instanceEntity,
                    templateValue,
                    true);
            return;
        }
        // Instance can also inherit on demand.
        String instanceControl =
                this.instanceEntity.getPropertyAsStr(member.getControl());
        if (LP_OBJECTS.INHERIT.equals(instanceControl)) {
            this.instanceEntity.replace(
                    member.getProperty(),
                    this.instanceEntity,
                    templateValue,
                    true);
            this.instanceEntity.setIri(member.getControl(), LP_OBJECTS.NONE);
        }
        // In every other case we keep value from the instance, so just check
        // the control.
        if (instanceControl == null) {
            this.instanceEntity.setIri(member.getControl(), LP_OBJECTS.NONE);
        }
    }

}
