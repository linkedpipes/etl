package com.linkedpipes.etl.storage.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * A helper class that can be used to build {@link Model}.
 */
public class ModelBuilder {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    public class EntityReference {

        private final Model.Entity entity;

        private EntityReference(Model.Entity entity) {
            this.entity = entity;
        }

        public EntityReference string(String property, String value) {
            return string(VF.createIRI(property), value);
        }

        public EntityReference string(IRI property, String value) {
            entity.add(property, VF.createLiteral(value));
            return this;
        }

        public EntityReference iri(String property, EntityReference ref) {
            return iri(VF.createIRI(property), ref);
        }

        public EntityReference iri(IRI property, EntityReference ref) {
            entity.add(property, ref.entity.getResource());
            return this;
        }

        public EntityReference iri(String property, String value) {
            return iri(VF.createIRI(property), value);
        }

        public EntityReference iri(IRI property, String value) {
            entity.add(property, VF.createIRI(value));
            return this;
        }

        public EntityReference iri(IRI property, IRI value) {
            entity.add(property, value);
            return this;
        }

    }

    private Model model = new Model();

    public EntityReference entity(String iri) {
        final Model.Entity entity = model.getOrCreate(VF.createIRI(iri));
        return new EntityReference(entity);
    }

    public Model asModel() {
        return model;
    }

}
