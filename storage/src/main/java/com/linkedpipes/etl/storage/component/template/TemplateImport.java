package com.linkedpipes.etl.storage.component.template;

/**
 * @author Petr Škoda
 */
class TemplateImport {

//    private static final ValueFactory VF = SimpleValueFactory.getInstance();
//
//    private final Resource iri;
//
//    private final File directory;
//
//    public TemplateImport(String iri, File directory) {
//        this.iri = VF.createIRI(iri);
//        this.directory = directory;
//    }
//
//    public ReferenceTemplate createTemplate(Collection<Statement> source,
//            Collection<Statement> sourceConfig) throws BaseException {
//
//        final Collection<Statement> interfaceRdf = new ArrayList<>(4);
//
//        final Collection<Statement> definitionRdf = new ArrayList<>(4);
//        definitionRdf.addAll(VF.createStatement(this.iri,
//                RDF.TYPE, ReferenceTemplate.TYPE, this.iri));
//
//        final Collection<Statement> configurationRdf = new ArrayList<>(
//                sourceConfig.size());
//
//        final Resource configIri = VF.createIRI(this.iri.stringValue() +
//                "/configuration");
//        // Convert configuration - removeProperties context.
//        for (Statement statement : sourceConfig) {
//            configurationRdf.add(VF.createStatement(
//                    statement.getSubject(),
//                    statement.getPredicate(),
//                    statement.getObject(),
//                    configIri
//            ));
//        }
//
//        // Add reference to the configuration.
//        interfaceRdf.add(VF.createStatement(this.iri,
//                VF.createIRI("http://linkedpipes.com/ontology/configurationGraph"),
//                configIri, this.iri));
//
//        String referenceTemplate = null;
//        // Create definition and interface.
//        for (Statement statement : source) {
//            interfaceRdf.add(VF.createStatement(this.iri,
//                    statement.getPredicate(),
//                    statement.getObject(),
//                    this.iri
//            ));
//            // Add some predicates to the definition.
//            switch (statement.getPredicate().stringValue()) {
//                case "http://linkedpipes.com/ontology/configurationGraph":
//                    break;
//                case "http://linkedpipes.com/ontology/template":
//                    definitionRdf.add(VF.createStatement(this.iri,
//                            statement.getPredicate(),
//                            statement.getObject(),
//                            this.iri
//                    ));
//                    referenceTemplate = statement.getObject().stringValue();
//                    break;
//            }
//        }
//
//        if (referenceTemplate == null) {
//            // Invalid template.
//            throw new BaseException("Missing template reference!");
//        }
//
//        this.directory.mkdirs();
//
//        final File interfaceFile = new File(this.directory,
//                "interface.jsonld");
//        RdfUtils.write(interfaceFile, RDFFormat.JSONLD, interfaceRdf);
//
//        final File definitionFile = new File(this.directory,
//                "definition.jsonld");
//        RdfUtils.write(definitionFile, RDFFormat.JSONLD, definitionRdf);
//
//        final File configurationFile = new File(this.directory,
//                "configuration.jsonld");
//        RdfUtils.write(configurationFile, RDFFormat.JSONLD, configurationRdf);
//
//        return null;
//    }

}
