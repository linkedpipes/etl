package com.linkedpipes.etl.plugin.configuration;

class MergeFromTop {
//
//    private static final Logger LOG =
//            LoggerFactory.getLogger(MergeFromTop.class);
//
//    private Description description;
//
//    private Model templateModel;
//
//    private Model.Entity templateEntity;
//
//    Statements merge(
//            Collection<Statements> configurationsRdf,
//            Description description,
//            String baseIri, IRI graph) throws BaseException {
//        initialize(description);
//        //
//        for (Statements configurationRdf : configurationsRdf) {
//            if (templateModel == null) {
//                this.loadModel(configurationRdf);
//                continue;
//            }
//            // Create instance of current configuration.
//            Model childModel = Model.create(configurationRdf);
//            Model.Entity childEntity = childModel.select(
//                    null, RDF.TYPE, description.getType()).single();
//            if (childEntity == null) {
//                LOG.warn("Missing configuration entity.");
//                continue;
//            }
//            if (this.description.getGlobalControl() == null) {
//                mergePerPredicate(childModel, childEntity);
//            } else {
//                boolean continueEvaluation =
//                        mergerGlobalControl(childModel, childEntity);
//                if (!continueEvaluation) {
//                    break;
//                }
//            }
//        }
//        if (templateModel == null) {
//            LOG.warn("No configuration found.");
//            return Statements.arrayList();
//        }
//        templateModel.updateResources(baseIri + "/");
//        return new Statements(
//                templateModel.asStatements(templateEntity, graph));
//    }
//
//    private void initialize(Description description) {
//        this.description = description;
//        this.templateModel = null;
//        this.templateEntity = null;
//    }
//
//    private void loadModel(Collection<Statement> statements) {
//        templateModel = Model.create(statements);
//        templateEntity = templateModel.select(
//                null, RDF.TYPE, description.getType()).single();
//        if (templateEntity == null) {
//            templateModel = null;
//            LOG.warn("Missing configuration entity for: {}",
//                    description.getType());
//        }
//    }
//
//    private void mergePerPredicate(Model childModel, Model.Entity childEntity)
//            throws BaseException {
//        // Merge from children to templateModel.
//        if (description.getMembers().isEmpty()) {
//            // We should load all properties from children, ald overwrite
//            // those in parent -> this can be done by simply swapping
//            //                    the configurations.
//            templateModel = childModel;
//            templateEntity = childEntity;
//        } else {
//            // Use from definition.
//            for (Description.Member member : description.getMembers()) {
//                mergeEntities(member, templateEntity, childEntity);
//            }
//        }
//    }
//
//    private boolean mergerGlobalControl(
//            Model childModel, Model.Entity childConfiguration) {
//        String control = childConfiguration.getPropertyAsStr(
//                description.getGlobalControl());
//        if (LP_OBJECTS.INHERIT.equals(control)) {
//            // Skip loading this object.
//            return true;
//        }
//        if (LP_OBJECTS.INHERIT_AND_FORCE.equals(control)) {
//            // Do not load anything from this instance, not any further.
//            templateEntity.setIri(
//                    description.getGlobalControl(), LP_OBJECTS.FORCED);
//            return false;
//        }
//        // Merge child to model.
//        templateModel = childModel;
//        templateEntity = childConfiguration;
//        //
//        if (LP_OBJECTS.FORCE.equals(control)) {
//            // Do not load anything in any further instance.
//            templateEntity.setIri(
//                    description.getGlobalControl(), LP_OBJECTS.FORCED);
//            return false;
//        }
//        return true;
//    }
//
//
//    /**
//     * Based on the configuration merge a single property value from
//     * childrenEntity to the parentEntity.
//     */
//    private void mergeEntities(
//            Description.Member member,
//            Model.Entity parent,
//            Model.Entity children) throws BaseException {
//        String parentControl = parent.getPropertyAsStr(member.getControl());
//        if (LP_OBJECTS.FORCED.equals(parentControl)) {
//            return;
//        }
//        String childControl = children.getPropertyAsStr(member.getControl());
//        if (childControl == null) {
//            childControl = LP_OBJECTS.NONE;
//        }
//        Value childValue = children.getProperty(member.getProperty());
//        switch (childControl) {
//            case LP_OBJECTS.INHERIT:
//                parent.setIri(member.getControl(), LP_OBJECTS.NONE);
//                break;
//            case LP_OBJECTS.FORCE:
//                parent.replace(
//                        member.getProperty(), children, childValue, true);
//                parent.setIri(member.getControl(), LP_OBJECTS.FORCED);
//                break;
//            case LP_OBJECTS.INHERIT_AND_FORCE:
//                parent.setIri(member.getControl(), LP_OBJECTS.FORCED);
//                break;
//            case LP_OBJECTS.FORCED:
//                throw new BaseException("Unexpected FORCED property");
//            case LP_OBJECTS.NONE:
//                // If the value is missing then in this case the value
//                // is ignored.
//                if (childValue == null) {
//                    break;
//                }
//                parent.replace(
//                        member.getProperty(), children, childValue, true);
//                parent.setIri(member.getControl(), LP_OBJECTS.NONE);
//                break;
//            default:
//                throw new BaseException(
//                  "unexpected property: " + childControl);
//        }
//    }

}
