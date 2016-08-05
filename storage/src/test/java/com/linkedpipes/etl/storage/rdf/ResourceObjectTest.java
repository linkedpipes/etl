package com.linkedpipes.etl.storage.rdf;

/**
 * Test suite form {@link RdfObject}.
 *
 * TODO Update as test suite for RdfObjects
 *
 * @author Petr Škoda
 */
public class ResourceObjectTest {

//    private final ValueFactory VF = SimpleValueFactory.getInstance();
//
//    /**
//     * Load a simple one level object
//     */
//    @Test
//    public void loadOneLevelObject() {
//        final Resource res = VF.createBNode();
//        // Prepare data.
//        final String label = "Object label";
//        final List<Statement> statements = Arrays.asList(
//                VF.createStatement(res, RDF.TYPE,
//                        VF.createIRI("http://localhost/ontology/TestObject")),
//                VF.createStatement(res, SKOS.PREF_LABEL,
//                        VF.createLiteral(label))
//        );
//        // Load.
//        final RdfObjects obj = new RdfObjects(statements);
//        // Check
//        Assert.assertEquals(1, obj.getReferences(RDF.TYPE).size());
//        Assert.assertEquals(1, obj.getProperties(SKOS.PREF_LABEL).size());
//        Assert.assertEquals(label,
//                obj.getProperties(SKOS.PREF_LABEL).get(0).stringValue());
//        // Check collect.
//        Assert.assertEquals(statements.size(),
//                RdfObject.collect(obj, null, VF).size());
//    }
//
//    /**
//     * Load an object with reference to other object. The other object has
//     * one property.
//     */
//    @Test
//    public void loadTwoLevelObject() {
//        final Resource resOne = VF.createBNode();
//        final Resource resTwo = VF.createBNode();
//        final IRI reference = VF.createIRI(
//                "http://localhost/ontology/reference");
//        // Prepare data.
//        final String label = "Object label";
//        final List<Statement> statements = Arrays.asList(
//                VF.createStatement(resOne, RDF.TYPE,
//                        VF.createIRI("http://localhost/ontology/TestObject")),
//                VF.createStatement(resOne, reference, resTwo),
//                VF.createStatement(resTwo, RDF.TYPE,
//                        VF.createIRI("http://localhost/ontology/TestObject")),
//                VF.createStatement(resTwo, SKOS.PREF_LABEL,
//                        VF.createLiteral(label))
//        );
//        // Load.
//        final RdfObject objOne = RdfObject.create(statements, resOne);
//        // Check
//        Assert.assertEquals(1, objOne.getReferences(RDF.TYPE).size());
//        Assert.assertEquals(1, objOne.getReferences(reference).size());
//        final RdfObject objTwo = objOne.getReferences(reference).get(0);
//        Assert.assertEquals(1, objTwo.getProperties(SKOS.PREF_LABEL).size());
//        Assert.assertEquals(label,
//                objTwo.getProperties(SKOS.PREF_LABEL).get(0).stringValue());
//        // Check collect.
//        Assert.assertEquals(statements.size(),
//                RdfObject.collect(objOne, null, VF).size());
//    }
//
//    /**
//     * Test "rename" of resources.
//     */
//    @Test
//    public void twoLevelRename() {
//        final Resource resOne = VF.createBNode();
//        final Resource resTwo = VF.createBNode();
//        final IRI reference = VF.createIRI(
//                "http://localhost/ontology/reference");
//        // Prepare data.
//        final List<Statement> statements = Arrays.asList(
//                VF.createStatement(resOne, RDF.TYPE,
//                        VF.createIRI("http://localhost/ontology/TestObject")),
//                VF.createStatement(resOne, reference, resTwo),
//                VF.createStatement(resTwo, RDF.TYPE,
//                        VF.createIRI("http://localhost/ontology/TestObject")),
//                VF.createStatement(resTwo, SKOS.PREF_LABEL,
//                        VF.createLiteral("Label"))
//        );
//        // Load.
//        final RdfObject objOne = RdfObject.create(statements, resOne);
//        final String prefix = "http://localhost";
//        RdfObject.updateTypedResources(objOne, prefix);
//        // Check that all resources starts with out prefix.
//        final Collection<Statement>
//                collected = RdfObject.collect(objOne, null, VF);
//        for (Statement statement : collected) {
//            Assert.assertTrue(statement.getSubject().stringValue()
//                    .startsWith(prefix));
//        }
//    }

}
