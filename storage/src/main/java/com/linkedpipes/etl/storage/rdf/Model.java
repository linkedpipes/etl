package com.linkedpipes.etl.storage.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
         * Can return Null if the value is missing.
         */
        public Value getProperty(IRI property) {
            List<Value> values = properties.get(property);
            if (values == null) {
                return null;
            }
            if (values.isEmpty()) {
                return null;
            }
            return values.get(0);
        }

        /**
         * Set value of given property to point to given entity. In
         * recursive mode also importJarComponent the entity.
         * Any previous values are deleted.
         *
         * <p>This does not delete any referenced entities.
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
         * Set recursive to true only if the given value is reference to
         * a object.
         */
        public void replace(
                IRI property, Entity entity, Value value, boolean recursive) {
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
         */
        public void set(IRI property, Value value) {
            if (value == null) {
                properties.remove(property);
                return;
            }
            List<Value> values = new LinkedList<>();
            values.add(value);
            properties.put(property, values);
        }

        protected Model getModel() {
            return Model.this;
        }
    }

    /**
     * List of all entities.
     */
    protected Map<Resource, Entity> entities = new HashMap<>();

    protected Model() {

    }

    /**
     * Add importJarComponent of entity to this model.
     */
    public void add(Entity entity) {
        Entity newEntity = new Entity(entity.resource);
        for (Map.Entry<IRI, List<Value>> entry
                : newEntity.properties.entrySet()) {
            List<Value> data = new LinkedList<>();
            data.addAll(entry.getValue());
            newEntity.properties.put(entry.getKey(), data);
        }
    }

    /**
     * Create, add and return new entity. If entity of given resource
     * already exists then add the value to it and return it.
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
     * Return sub-tree as defined by references in given entity.
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
     */
    public Collection<Entity> getSubTree(Entity entity) {
        Set<Entity> result = new HashSet<>();
        getSubTree(entity, result);
        return result;
    }

    /**
     * Create and return empty model.
     */
    public static Model create() {
        return new Model();
    }

    /**
     * Create and return model for given RDF.
     */
    public static Model create(Collection<Statement> statements) {
        Model model = new Model();
        for (Statement statement : statements) {
            model.add(statement.getSubject(),
                    statement.getPredicate(), statement.getObject());
        }
        return model;
    }

}
