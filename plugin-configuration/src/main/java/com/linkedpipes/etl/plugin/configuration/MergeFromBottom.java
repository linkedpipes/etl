package com.linkedpipes.etl.plugin.configuration;

class MergeFromBottom {

//    private static final Logger LOG =
//            LoggerFactory.getLogger(MergeFromBottom.class);
//
//    private final ValueFactory valueFactory =
//      SimpleValueFactory.getInstance();
//
//    private Description description;
//
//    private Model templateModel;
//
//    private Model.Entity templateEntity;
//
//    private Model instanceModel;
//
//    private Model.Entity instanceEntity;
//
//    private String baseIri;
//
//    private IRI graph;
//
//    /**
//     * Designed to be used to merge configuration from instance to templates,
//     * thus enabling another merge with other ancestor.
//     */
//    Statements merge(
//            Statements templateRdf,
//            Statements instanceRdf,
//            Description description,
//            String baseIri, IRI graph) {
//        initialize(description, baseIri, graph);
//        //
//        loadTemplateModel(templateRdf);
//        if (templateEntity == null) {
//            LOG.warn("Missing template configuration entity: {}",
//                    description.getType());
//            return new Statements(collectTemplateModel());
//        }
//        loadInstanceModel(instanceRdf);
//        if (instanceEntity == null) {
//            LOG.warn("Missing instance configuration entity: {}",
//                    description.getType());
//            return new Statements(collectTemplateModel());
//        }
//        if (description.getGlobalControl() == null) {
//            return new Statements(mergePerPredicate());
//        } else {
//            return new Statements(mergeGlobal());
//        }
//    }
//
//    private void initialize(
//            Description description, String baseIri, IRI graph) {
//        this.description = description;
//        this.templateModel = null;
//        this.templateEntity = null;
//        this.instanceModel = null;
//        this.baseIri = baseIri;
//        this.graph = graph;
//    }
//
//    private void loadTemplateModel(Collection<Statement> statements) {
//        templateModel = Model.create(statements);
//        templateEntity = templateModel.select(
//                null, RDF.TYPE, description.getType()).single();
//        if (templateEntity == null) {
//            templateModel = null;
//        }
//    }
//
//    private void loadInstanceModel(Collection<Statement> statements) {
//        instanceModel = Model.create(statements);
//        instanceEntity = instanceModel.select(
//                null, RDF.TYPE, description.getType()).single();
//        if (instanceEntity == null) {
//            instanceModel = null;
//        }
//    }
//
//    private Collection<Statement> mergePerPredicate() {
//        if (description.getMembers().isEmpty()) {
//            return collectInstanceModel();
//        }
//        for (Description.Member member : description.getMembers()) {
//            mergeDescriptionMember(member);
//        }
//        return collectInstanceModel();
//    }
//
//    private Collection<Statement> mergeGlobal() {
//        String templateControl = templateEntity.getPropertyAsStr(
//                description.getGlobalControl());
//        if (LP_OBJECTS.INHERIT.equals(templateControl)) {
//            // The configuration of the templateModel is inherited from
//            // another level of templateModel. So we skip merging
//            // with this level of template.
//            return collectInstanceModel();
//        }
//        if (LP_OBJECTS.INHERIT_AND_FORCE.equals(templateControl)) {
//            // We need to load configuration from another level of template.
//            return Arrays.asList(
//                    valueFactory.createStatement(
//                            instanceEntity.getResource(),
//                            RDF.TYPE,
//                            description.getType()),
//                    valueFactory.createStatement(
//                            instanceEntity.getResource(),
//                            description.getGlobalControl(),
//                            valueFactory.createIRI(
//                                    LP_OBJECTS.INHERIT_AND_FORCE)));
//        }
//        if (LP_OBJECTS.FORCE.equals(templateControl)) {
//            return collectTemplateModel(LP_OBJECTS.FORCED);
//        }
//        String instanceControl = null;
//        if (description.getGlobalControl() != null) {
//            instanceControl = instanceEntity.getPropertyAsStr(
//                    description.getGlobalControl());
//        }
//        if (LP_OBJECTS.INHERIT.equals(instanceControl)) {
//            return collectTemplateModel();
//        }
//        if (LP_OBJECTS.INHERIT_AND_FORCE.equals(instanceControl)) {
//            return collectTemplateModel(LP_OBJECTS.FORCED);
//        }
//        // We use the child's configuration.
//        return collectInstanceModel();
//    }
//
//    private Collection<Statement> collectTemplateModel() {
//        templateModel.updateResources(baseIri + "/");
//        return templateModel.asStatements(templateEntity, graph);
//    }
//
//    private Collection<Statement> collectTemplateModel(String control) {
//        templateModel.updateResources(baseIri + "/");
//        templateEntity.set(
//                description.getGlobalControl(),
//                valueFactory.createIRI(control));
//        return templateModel.asStatements(templateEntity, graph);
//    }
//
//    private Collection<Statement> collectInstanceModel() {
//        instanceModel.updateResources(baseIri + "/");
//        return instanceModel.asStatements(instanceEntity, graph);
//    }
//
//    private void mergeDescriptionMember(Description.Member member) {
//        // First check if templateModel does not force values to instance.
//        String templateControl =
//                templateEntity.getPropertyAsStr(member.getControl());
//        Value templateValue =
//                templateEntity.getProperty(member.getProperty());
//
//        if (LP_OBJECTS.FORCE.equals(templateControl)) {
//            instanceEntity.replace(
//                    member.getProperty(),
//                    instanceEntity,
//                    templateValue,
//                    true);
//            instanceEntity.setIri(member.getControl(), LP_OBJECTS.FORCED);
//            return;
//        }
//        if (LP_OBJECTS.INHERIT_AND_FORCE.equals(templateControl)) {
//            // Remove value - the value will be load from next templateModel.
//            instanceEntity.replace(
//                    member.getProperty(),
//                    instanceEntity,
//                    null,
//                    false);
//            instanceEntity.setIri(member.getControl(), LP_OBJECTS.FORCED);
//            return;
//        }
//        // If the value is missing we need to load if from a templateModel.
//        // This can happen if the instance has INHERIT_AND_FORCE control.
//        Value instanceValue =
//                instanceEntity.getProperty(member.getProperty());
//        if (instanceValue == null) {
//            instanceEntity.replace(
//                    member.getProperty(),
//                    instanceEntity,
//                    templateValue,
//                    true);
//            return;
//        }
//        // Instance can also inherit on demand.
//        String instanceControl =
//                instanceEntity.getPropertyAsStr(member.getControl());
//        if (LP_OBJECTS.INHERIT.equals(instanceControl)) {
//            instanceEntity.replace(
//                    member.getProperty(),
//                    instanceEntity,
//                    templateValue,
//                    true);
//            instanceEntity.setIri(member.getControl(), LP_OBJECTS.NONE);
//        }
//        // In every other case we keep value from the instance, so just check
//        // the control.
//        if (instanceControl == null) {
//            instanceEntity.setIri(member.getControl(), LP_OBJECTS.NONE);
//        }
//    }
//
//    public Statements finalize(Statements configurationRdf) {
//        // Solve situation where there is no value in the core component
//        // however the instance choose to INHERIT the value.
//        // There might be also same issue with INHERIT_AND_FORCE
//        // but in that case it make sense to fail, as the given value
//        // should not be used.
//
//        List<Statement> statements = configurationRdf.stream()
//                .filter(st -> {
//                    String value = st.getObject().stringValue();
//                    return !LP_OBJECTS.INHERIT.equals(value);
//                })
//                .collect(Collectors.toList());
//
//        return new Statements(statements);
//    }

}
