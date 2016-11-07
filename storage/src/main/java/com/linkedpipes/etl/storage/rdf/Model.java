package com.linkedpipes.etl.storage.rdf;

import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;

import java.util.*;

public class Model {

    public class Entity {

        private Resource resource;

        private Map<IRI, List<Value>> properties = new HashMap<>();

        public Resource getResource() {
            return resource;
        }

        public Entity(Resource resource) {
            this.resource = resource;
        }

        /**
         * Return value of given property. If there are multiple values
         * return only one.
         *
         * @param property
         * @return Null if the value is missing.
         */
        public Value getProperty(IRI property) {
            final List<Value> values = properties.get(property);
            if (values == null) {
                return null;
            }
            if (values.isEmpty()) {
                return null;
            }
            return values.get(0);
        }

        /**
         * Return value of given property as a string. If there are multiple
         * values only one is returned.
         *
         * @param property
         * @return Null if the value is missing.
         */
        public String getPropertyAsStr(IRI property) {
            final Value value = getProperty(property);
            if (value == null) {
                return null;
            } else {
                return value.stringValue();
            }
        }

        /**
         * Set value of given property to point to given entity. In
         * recursive mode also copy the entity. Any previous values
         * are deleted.
         *
         * This does not delete any referenced entities.
         *
         * @param property
         * @param entity
         * @param recursive
         */
        public void replace(IRI property, Entity entity, boolean recursive) {
            set(property, entity.resource);
            if (!recursive) {
                return;
            }
            // We need to add whole sub-tree.
            entity.getModel().getSubTree(entity).forEach((toAdd) -> {
                Model.this.add(toAdd);
            });
        }

        /**
         * Replace value of given property with given value from given entity.
         *
         * @param property
         * @param entity
         * @param value
         * @param recursive Used only if it's a referece to another object.
         */
        public void replace(IRI property, Entity entity, Value value,
                boolean recursive) {
            if (value instanceof Resource) {
                Entity refEntity = entity.getModel().entities.get(value);
                if (refEntity == null) {
                    set(property, value);
                } else {
                    replace(property, refEntity, recursive);
                }
            } else {
                // Just replace as a value.
                set(property, value);
            }
        }

        /**
         * Add value under given property to this entity.
         *
         * @param property
         * @param value
         */
        public void add(IRI property, Value value) {
            List<Value> values = properties.get(property);
            if (values == null) {
                values = new LinkedList<>();
                properties.put(property, values);
            }
            values.add(value);
        }

        /**
         * Set value of given property to given value. Any previous
         * values are deleted.
         *
         * @param property
         * @param value
         */
        public void set(IRI property, Value value) {
            final List<Value> values = new LinkedList<>();
            values.add(value);
            properties.put(property, values);
        }

        /**
         * Set value of given property to given IRI.
         *
         * @param property
         * @param iriAsString IRI as a string.
         */
        public void setIri(IRI property, String iriAsString) {
            set(property,
                    SimpleValueFactory.getInstance().createIRI(iriAsString));
        }

        /**
         * @return Outer class (owner).
         */
        protected Model getModel() {
            return Model.this;
        }
    }

    public class EntityList {

        protected LinkedList<Entity> entities = new LinkedList<>();

        /**
         * @return First entity or null.
         */
        public Entity single() {
            if (entities.isEmpty()) {
                return null;
            }
            return entities.getFirst();
        }

    }

    /**
     * List of all entities.
     */
    protected Map<Resource, Entity> entities = new HashMap<>();

    protected Model() {

    }

    public Entity getOrCreate(Resource resource) {
        Entity entity = entities.get(resource);
        if (entity == null) {
            entity = new Entity(resource);
            entities.put(resource, entity);
        }
        return entity;
    }

    /**
     * Add copy of entity to this model.
     *
     * @param entity
     */
    public void add(Entity entity) {
        final Entity newEntity = new Entity(entity.resource);
        for (Map.Entry<IRI, List<Value>> entry
                : newEntity.properties.entrySet()) {
            final List<Value> data = new LinkedList<>();
            data.addAll(entry.getValue());
            newEntity.properties.put(entry.getKey(), data);
        }
    }

    /**
     * Create, add and return new entity. If entity of given resource
     * already exists then add the value to it and return it.
     *
     * @param resource
     * @param predicate
     * @param value
     * @return
     */
    public Entity add(Resource resource, IRI predicate, Value value) {
        Entity entity = entities.get(resource);
        if (entity == null) {
            entity = new Entity(resource);
            entities.put(resource, entity);
        }
        //
        entity.add(predicate, value);
        return entity;
    }

    /**
     * Select entities that match given requirements. Use null as a wildcard.
     *
     * @param resource
     * @param predicate
     * @param object
     * @return
     */
    public EntityList select(Resource resource, IRI predicate, Value object) {
        final EntityList list = new EntityList();
        entities.values().forEach((entity) -> {
            if (resource != null && !resource.equals(entity.resource)) {
                return;
            }
            if (predicate == null) {
                // Scan all.
                for (List<Value> values : entity.properties.values()) {
                    if (values.contains(object)) {
                        list.entities.add(entity);
                    }
                }
            } else {
                if (!entity.properties.containsKey(predicate)) {
                    return;
                }
                if (entity.properties.get(predicate).contains(object)) {
                    list.entities.add(entity);
                }
            }
            entity.resource.equals(resource);
        });
        return list;
    }

    /**
     * Update resources (and references to them) for all stored entities.
     *
     * @param baseIri
     */
    public void updateResources(String baseIri) {
        int counter = 0;
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final Map<Resource, Value> mapping = new HashMap<>();
        final Map<Resource, Entity> newEntitites = new HashMap<>();
        for (Entity entity : entities.values()) {
            final Resource oldResource = entity.resource;
            entity.resource = vf.createIRI(baseIri + (++counter));
            mapping.put(oldResource, entity.resource);
            newEntitites.put(entity.resource, entity);
        }
        entities.clear();
        entities.putAll(newEntitites);
        // Replace in references.
        for (Entity entity : entities.values()) {
            entity.properties.values().forEach((list) -> {
                for (int i = 0; i < list.size(); ++i) {
                    final Value mapped = mapping.get(list.get(i));
                    if (mapped != null) {
                        list.set(i, mapped);
                    }
                }
            });
        }
    }

    /**
     * Return sub-tree as defined by references in given entity.
     *
     * @param entity
     * @return
     */
    private void getSubTree(Entity entity, Set<Entity> result) {
        entity.properties.values().forEach((list) -> {
            list.forEach((value) -> {
                if (!entities.containsKey(value)) {
                    return;
                }
                final Entity item = entities.get(value);
                if (result.contains(item)) {
                    return;
                }
                result.add(item);
                result.addAll(getSubTree(item));
            });
        });
    }

    /**
     * Return sub-tree as defined by references in given entity.
     *
     * @param entity
     * @return
     */
    public Collection<Entity> getSubTree(Entity entity) {
        final Set<Entity> result = new HashSet<>();
        getSubTree(entity, result);
        return result;
    }

    /**
     * @return Representation of all entities.
     */
    public Collection<Statement> asStatements() {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final Collection<Statement> statements = new LinkedList<>();
        entities.values().forEach((entity) -> {
            entity.properties.entrySet().forEach((entry) -> {
                entry.getValue().forEach((value) -> {
                    statements.add(vf.createStatement(entity.resource,
                            entry.getKey(), value
                    ));
                });
            });
        });
        return statements;
    }

    /**
     * Representation of the entities as RDF. Only entities recursively
     * referenced from the given entity are used.
     *
     * @param root
     * @param graph Target graph.
     * @return
     */
    public Collection<Statement> asStatements(Entity root, Resource graph) {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final Collection<Entity> toOutput = getSubTree(root);
        toOutput.add(root);
        final Collection<Statement> statements = new LinkedList<>();
        toOutput.forEach((entity) -> {
            entity.properties.entrySet().forEach((entry) -> {
                entry.getValue().forEach((value) -> {
                    statements.add(vf.createStatement(entity.resource,
                            entry.getKey(), value, graph
                    ));
                });
            });
        });
        return statements;
    }

    /**
     * Create and return empty model.
     *
     * @return
     */
    public static Model create() {
        return new Model();
    }

    /**
     * Create and return model for given RDF.
     *
     * @param statements
     * @return
     */
    public static Model create(Collection<Statement> statements) {
        final Model model = new Model();
        for (Statement statement : statements) {
            model.add(statement.getSubject(),
                    statement.getPredicate(), statement.getObject());
        }
        return model;
    }

}
