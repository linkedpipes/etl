package com.linkedpipes.etl.storage.rdf;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.*;

/**
 * Represent an RDF graph in form of an Java object. Works with triples.
 */
public class RdfObjects {

    public static class Entity implements Comparable {

        private Resource resource;

        private final Map<IRI, List<Value>> properties = new HashMap<>();

        private final Map<IRI, List<Entity>> references = new HashMap<>();

        private final RdfObjects owner;

        protected Entity(RdfObjects owner) {
            this.resource = null;
            this.owner = owner;
        }

        protected Entity(Resource resource, RdfObjects owner) {
            this.resource = resource;
            this.owner = owner;
        }

        public Resource getResource() {
            return resource;
        }

        /**
         * @param property
         * @return All references under given IRI.
         */
        public List<Entity> getReferences(IRI property) {
            List<Entity> objects = references.get(property);
            if (objects == null) {
                return Collections.EMPTY_LIST;
            } else {
                return Collections.unmodifiableList(objects);
            }
        }

        /**
         * Return a single reference, if the references is missing
         * or throw an exception.
         *
         * @param property
         * @return
         */
        public Entity getReference(IRI property) {
            final List<Entity> objects = references.get(property);
            if (objects != null && objects.size() == 1) {
                return objects.get(0);
            } else {
                throw new RuntimeException("Invalid number of references.");
            }
        }

        /**
         * Return a single property, if it is missing ot here is more then one
         * then throw an exception.
         *
         * TODO Add method that return one or none !
         *
         * @param property
         * @return
         */
        public Value getProperty(IRI property) {
            final List<Value> objects = properties.getOrDefault(property,
                    Collections.EMPTY_LIST);
            if (objects.size() == 1) {
                return objects.get(0);
            } else {
                throw new RuntimeException("Invalid number (" + objects.size()
                        + ") of properties "
                        + property.stringValue() + " on "
                        + resource.stringValue());
            }
        }

        /**
         * Delete all references to this object from this object. Preserve
         * the object.
         *
         * @param property
         */
        public void deleteReferences(IRI property) {
            references.remove(property);
        }

        public void delete(IRI property) {
            references.remove(property);
            properties.remove(property);
        }

        public void add(Statement statement) {
            add(statement.getPredicate(), statement.getObject());
        }

        public void add(IRI property, int value) {
            add(property, owner.valueFactory.createLiteral(value));
        }

        public void add(IRI property, Value value) {
            // Check for resources.
            if (value instanceof Resource) {
                add(property, (Resource) value);
                return;
            }
            //
            List<Value> values = properties.get(property);
            if (values == null) {
                values = new LinkedList<>();
                properties.put(property, values);
            }
            values.add(value);
        }

        public void add(IRI property, Resource resource) {
            Entity objectToAdd = owner.resources.get(resource);
            if (objectToAdd == null) {
                objectToAdd = new Entity(resource, owner);
                owner.resources.put(resource, objectToAdd);
            }
            //
            List<Entity> values = references.get(property);
            if (values == null) {
                values = new LinkedList<>();
                references.put(property, values);
            }
            values.add(objectToAdd);
        }

        /**
         * Here we could be merging with entity from another object.
         *
         * @param object
         * @param preserve
         * @param overwrite
         */
        public void add(Entity object, Collection<IRI> preserve,
                Collection<IRI> overwrite) {
            // Merge properties - this is simple, there are no references.
            for (Map.Entry<IRI, List<Value>> entry
                    : object.properties.entrySet()) {
                if (preserve.contains(entry.getKey())) {
                    continue;
                }
                if (overwrite.contains(entry.getKey())) {
                    List<Value> thisValues = properties.get(entry.getKey());
                    if (thisValues != null) {
                        thisValues.clear();
                    }
                }
                for (Value value : entry.getValue()) {
                    // TODO Add should check for duplicities!
                    add(entry.getKey(), value);
                }
            }
            // Merge references.
            for (Map.Entry<IRI, List<Entity>> entry
                    : object.references.entrySet()) {
                // Skip those who should be preserved on target.
                if (preserve.contains(entry.getKey())) {
                    continue;
                }
                // If we should overwrite current values, just delete
                // the old values.
                if (overwrite.contains(entry.getKey())) {
                    List<Entity> thisValues = references.get(entry.getKey());
                    if (thisValues != null) {
                        thisValues.clear();
                    }
                }
                // Add objects.
                for (Entity value : entry.getValue()) {
                    // Create new entity.
                    Entity newValue = (new Builder(owner)).create();
                    newValue.resource = value.resource;
                    // TODO Check for cycle dependencies.
                    newValue.add(value, Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST);
                    // TODO Add should check for duplicities!
                    add(entry.getKey(), newValue);
                }
            }
        }

        /**
         * Add an object as a reference. If the object of same resource
         * already exists the data from given object are merged to existing
         * one. For this reason do not use the given object any further
         *
         * @param property
         * @param object
         */
        public void add(IRI property, Entity object) {
            // Check if the object is new.
            if (owner.resources.containsKey(object.getResource())) {
                // Check if the resources are the same by reference.
                final Entity currentObject =
                        owner.resources.get(object.getResource());
                if (currentObject == object) {
                    // The are the same we can continue.
                } else {
                    // They differ we need to merge them.
                    currentObject.add(object, Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST);
                    object = currentObject;
                }
            } else {
                // Add.
                owner.resources.put(object.getResource(), object);
            }
            //
            List<Entity> values = references.get(property);
            if (values == null) {
                values = new LinkedList<>();
                references.put(property, values);
            }
            values.add(object);
        }

        @Override
        public int compareTo(java.lang.Object value) {
            if (value instanceof Entity) {
                Entity obj = (Entity) value;
                return this.resource.stringValue()
                        .compareTo(obj.resource.stringValue());
            } else {
                return -1;
            }
        }

    }

    /**
     * Warning any object created during the object creation is
     * add to the given {@link RdfObjects} even if the created object
     * it self is not added.
     */
    public static class Builder {

        private final ValueFactory valueFactory;

        private final Entity object;

        public Builder(RdfObjects graph) {
            this.object = new Entity(graph);
            this.valueFactory = graph.valueFactory;
        }

        public Builder add(String property, Value value) {
            return add(valueFactory.createIRI(property), value);
        }

        public Builder add(IRI property, Value value) {
            object.add(property, value);
            return this;
        }

        public Builder addResource(IRI property, String value) {
            return addResource(property, valueFactory.createIRI(value));
        }

        public Builder addResource(String property, Resource value) {
            return addResource(valueFactory.createIRI(property), value);
        }

        public Builder addResource(IRI property, Resource value) {
            object.add(property, value);
            return this;
        }

        public Entity create() {
            if (object.resource == null) {
                object.resource = valueFactory.createBNode();
            }
            return object;
        }

    }

    private static int counter = 0;

    private int id = ++counter;

    /**
     * List of all stored resources.
     */
    private final Map<Resource, Entity> resources = new HashMap<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public RdfObjects(Collection<Statement> statements) {
        for (Statement statement : statements) {
            Entity object = resources.get(statement.getSubject());
            if (object == null) {
                object = new Entity(statement.getSubject(), this);
                resources.put(statement.getSubject(), object);
            }
            // Check for resources referred as object.
            if (statement.getObject() instanceof Resource) {
                final Resource resource = (Resource) statement.getObject();
                Entity reference = resources.get(resource);
                if (reference == null) {
                    reference = new Entity(resource, this);
                    resources.put(resource, reference);
                }
                // Add a reference to the resource.
                object.add(statement.getPredicate(), reference);
            } else {
                // Add as a value.
                object.add(statement);
            }
        }
    }

    public void add(Statement statement) {
        Entity entity = resources.get(statement.getSubject());
        if (entity == null) {
            entity = new Entity(statement.getSubject(), this);
            resources.put(statement.getSubject(), entity);
        }
        entity.add(statement);
    }

    public void addAll(Collection<Statement> statements) {
        for (Statement statement : statements) {
            add(statement);
        }
    }

    /**
     * Collection of resources if given types.
     *
     * @param types
     * @return Never null.
     */
    public Collection<Entity> getTyped(Resource... types) {
        final Collection<Entity> result = new HashSet<>();
        for (Entity object : resources.values()) {
            // TODO Use for loop and break.
            object.getReferences(RDF.TYPE).forEach((type) -> {
                for (Resource requiredType : types) {
                    if (requiredType.equals(type.getResource())) {
                        result.add(object);
                    }
                }
            });
        }
        return result;
    }

    public Entity getByIri(String iri) {
        return resources.get(valueFactory.createIRI(iri));
    }

    /**
     * Return first entity with given type.
     * TODO Check all entities
     *
     * @param requiredType
     * @return Single entity with given type.
     */
    public Entity getTypeSingle(Resource requiredType) {
        for (Entity object : resources.values()) {
            for (Entity entity : object.getReferences(RDF.TYPE)) {
                if (entity.getResource().equals(requiredType)) {
                    return object;
                }
            }
        }
        // TODO Use custom exception !
        throw new RuntimeException(
                "Missing resource:" + requiredType.stringValue());
    }

    public Collection<Statement> asStatements(Resource graph) {
        final Collection<Statement> result = new LinkedList<>();
        final ValueFactory vf = SimpleValueFactory.getInstance();
        for (Entity object : resources.values()) {
            // Properties.
            for (Map.Entry<IRI, List<Value>> entry :
                    object.properties.entrySet()) {
                for (Value value : entry.getValue()) {
                    result.add(vf.createStatement(
                            object.getResource(),
                            entry.getKey(),
                            value,
                            graph
                    ));
                }
            }
            // References.
            for (Map.Entry<IRI, List<Entity>> entry :
                    object.references.entrySet()) {
                for (Entity value : entry.getValue()) {
                    result.add(vf.createStatement(
                            object.getResource(),
                            entry.getKey(),
                            value.getResource(),
                            graph
                    ));
                }
            }
        }
        return result;
    }

    /**
     * Generate a new resource for givne object.
     *
     * @param object
     */
    public void changeResource(Entity object) {
        final Resource newResource = valueFactory.createBNode();
        resources.remove(object.getResource());
        resources.put(newResource, object);
        // TODO Replace with setter!
        object.resource = newResource;
    }

    /**
     * Update blank nodes to full IRI.
     *
     * @param baseIri
     */
    public void updateBlankNodes(String baseIri) {
        final Collection<Resource> blankNodes = new LinkedList<>();
        for (Resource resource : resources.keySet()) {
            if (resource instanceof IRI) {
                // OK pass.
            } else {
                blankNodes.add(resource);
            }
        }
        Integer counter = 0;
        for (Resource node : blankNodes) {
            final Resource newResource
                    = valueFactory.createIRI(baseIri + ++counter);
            // TODO Use setter, store object, not call get twice.
            resources.get(node).resource = newResource;
            resources.put(newResource, resources.get(node));
            resources.remove(node);
        }
    }

    public void updateTypedResources(String baseIri) {
        // Change resource for all entities with types.
        Integer counter = 0;
        Map<Resource, Entity> newResources = new HashMap<>();
        for (Entity object : resources.values()) {
            if (!object.getReferences(RDF.TYPE).isEmpty()) {
                final IRI iri = valueFactory.createIRI(
                        baseIri + "/" + ++counter);
                newResources.put(iri, object);
                object.resource = iri;
            }
        }
        //
        resources.clear();
        resources.putAll(newResources);
    }

    /**
     * Remove object and all references to it.
     *
     * @param object
     */
    public void remove(Entity object) {
        // TODO Search for object in all objects.
        resources.remove(object.getResource());
    }

}
