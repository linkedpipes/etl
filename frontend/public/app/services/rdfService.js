/**
 * A simple service that can be used to manipulate RDF data.
 *
 * This service can be used by the configuration dialogs.
 *
 */
define([], function () {
    function factoryFunction() {
        return {
            /**
             *
             * @param prefix Prefix used for all predicates.
             * @return RDF service on given data.
             */
            'create': function (prefix) {
                var service = {
                    'prefix': prefix,
                    'graph': {}
                };

                var generateRandomString = function (size) {
                    // TODO Check if not already presented.
                    var text = "";
                    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                    for (var i = 0; i < size; i++) {
                        text += possible.charAt(Math.floor(Math.random() * possible.length));
                    }
                    return text;
                };

                /**
                 *
                 * @param value Object with '@id' or array of such objects.
                 * @return URIs of referenced objects.
                 */
                var readReferences = function (value) {
                    if (typeof value === 'undefined') {
                        return [];
                    }
                    if ($.isArray(value)) {
                        var result = [];
                        for (var index in value) {
                            result = result.concat(readReferences(value[index]));
                        }
                        return result;
                    } else if (value instanceof Object) {
                        if (typeof value['@id'] === 'undefined') {
                            console.log('@id property is missing for: ', value);
                            return [];
                        } else {
                            return [value['@id']];
                        }
                    } else {
                        return [];
                    }
                };

                /**
                 * Set data object.
                 *
                 * @param graph
                 */
                service.setData = function (graph) {
                    service.graph = graph;
                };

                /**
                 *
                 * @return Current data object.
                 */
                service.getData = function () {
                    return service.graph;
                };

                /**
                 *
                 * @param array Array of object.
                 * @return A single object.
                 */
                service.filterSingle = function (array) {
                    if (array.length === 0) {
                        return;
                    } else if (array.length === 1) {
                        return array[0];
                    } else {
                        console.log('Multiple instances detected.', service.prefix + service.type, 'in graph', service.graph);
                        return array[0];
                    }
                };

                /**
                 *
                 * @param type
                 * @return All objects of given type.
                 */
                service.findByType = function (type) {
                    type = prefix + type;
                    var resources = [];
                    service.graph['@graph'].forEach(function (resource) {
                        if (resource['@type'].indexOf(type) !== -1) {
                            resources.push(resource);
                        }
                    });
                    return resources;
                };

                /**
                 *
                 * @param uri
                 * @return All objects with given URI.
                 */
                service.findByUri = function (uri) {
                    var resources = [];
                    service.graph['@graph'].forEach(function (resource) {
                        if (resource['@id'] && resource['@id'].indexOf(uri) !== -1) {
                            resources.push(resource);
                        }
                    });
                    return resources;
                };

                /**
                 * Delete object with given URI, does not check for references!
                 *
                 * @param uri
                 */
                service.deleteByUri = function (uri) {
                    var graph = service.graph['@graph'];
                    for (var index in graph) {
                        if (graph[index]['@id'] && graph[index]['@id'] === uri) {
                            graph.splice(index, 1);
                            return;
                        }
                    }
                };

                /**
                 * Create a new object and add it to current graph.
                 *
                 * @param type Given prefix is appllied.
                 * @return Newly created object.
                 */
                service.createObject = function (type) {
                    var newResource = {
                        '@id': 'http://localhost/resources/temp/' + generateRandomString(7),
                        '@type': [
                            service.prefix + type
                        ]
                    };
                    service.graph['@graph'].push(newResource);
                    return newResource;
                };

                /**
                 * Get object of given type, if object doeas not exist it's created.
                 *
                 * @param type
                 * @return
                 */
                service.secureByType = function (type) {
                    var resources = service.filterSingle(service.findByType(type));
                    if (typeof resources !== 'undefined') {
                        return resources;
                    } else {
                        return service.createObject(type);
                    }
                };

                /**
                 * Search for object and return it, if it does not exists than it's created.
                 *
                 * @param resource
                 * @param property
                 * @param type
                 * @return
                 */
                service.secureObject = function (resource, property, type) {
                    var object = service.getObject(resource, property);
                    if (object) {
                        return object;
                    } else {
                        object = service.createObject(type);
                        service.setObjects(resource, property, [object['@id']]);
                        return object;
                    }
                };

                /**
                 * If more then one object is referenced, one object is picked at random.
                 *
                 * @param resource
                 * @param property
                 * @return An instance of object referenced from the given property.
                 */
                service.getObject = function (resource, property) {
                    return service.filterSingle(service.getObjects(resource, property));
                };

                /**
                 *
                 * @param resource
                 * @param property
                 * @return Instances of object referenced from the given property.
                 */
                service.getObjects = function (resource, property) {
                    property = service.prefix + property;
                    var ids = [];
                    if (typeof resource[property] === 'undefined') {
                        return [];
                    }
                    var ids = readReferences(resource[property]);
                    var result = [];
                    for (var index in ids) {
                        var object = service.findByUri(ids[index]);
                        result = result.concat(service.findByUri(ids[index]));
                    }
                    return result;
                };

                /**
                 * Set objects to given property, original references are lost, referenced objects are
                 * not deleted.
                 *
                 * In general it's bettern to use updateObjects method.
                 *
                 * @param resource
                 * @param property
                 * @param uris URIs of new referenced objects.
                 */
                service.setObjects = function (resource, property, uris) {
                    property = service.prefix + property;
                    var references = [];
                    for (var index in uris) {
                        references.push({
                            '@id': uris[index]
                        });
                    }
                    resource[property] = references;
                };

                /**
                 * Given list of object, update references for objects. Any removed object is deleted.
                 *
                 * The instances of objects that does not change (based on '@id') are not changed.
                 *
                 * @param resource
                 * @param property
                 * @param objects
                 * @param addNew True if new objects should not be added.
                 */
                service.updateObjects = function (resource, property, objects, addNew) {
                    // Get IDs of existing objects.
                    var oldList = readReferences(resource[service.prefix + property]);
                    // Prepare ids of inserted objects.
                    var newList = [];
                    var newMap = {}; // Used for inset of new value.
                    objects.forEach(function (item) {
                        if (!item['@id']) {
                            console.log('Blank nodes are not suported!');
                            item['@id'] = 'http://localhost/resources/temp/blank/' + generateRandomString(7);
                        }
                        newList.push(item['@id']);
                        newMap[item['@id']] = item;
                    });
                    // Remove.
                    var toRemove = $(oldList).not(newList).get();
                    for (var index in toRemove) {
                        service.deleteByUri(toRemove[index]);
                    }
                    // Add.
                    if (addNew) {
                        var toAdd = $(oldList).not(newList).get();
                        for (var index in toAdd) {
                            service.graph['@graph'].push(newMap[toAdd[index]]);
                        }
                    }
                    // Update references.
                    service.setObjects(resource, property, newList);
                };

                /**
                 *
                 * @param resource
                 * @param property
                 * @return Value under given property, or empty string if value is missing.
                 */
                service.getString = function (resource, property) {
                    property = service.prefix + property;
                    if (typeof resource[property] === 'undefined') {
                        return '';
                    } else {
                        return resource[property];
                    }
                };

                /**
                 * If value is not set or is empty ('') then the given predicate is removed.
                 *
                 * @param resource
                 * @param property
                 * @param value Value to set.
                 */
                service.setString = function (resource, property, value) {
                    property = service.prefix + property;
                    if (typeof value === 'undefined' || value === '') {
                        resource[property] = '';
                    } else {
                        resource[property] = value;
                    }
                };

                service.getInteger = function (resource, property) {
                    // Information about type is stored in context.
                    return service.getString(resource, property);
                };

                service.setInteger = function (resource, property, value) {
                    // Information about type is stored in context.
                    property = service.prefix + property;
                    // For defensive approach we assume we can get empty string as an null "integer".
                    if (typeof value === 'undefined' || value === null || value === '') {
                        delete resource[property];
                    } else {
                        resource[property] = value;
                    }
                };

                service.getDate = function (resource, property) {
                    // Information about type is stored in context.
                    var value = service.getString(resource, property);
                    if (typeof value === 'undefined' || value === null || value === '') {
                        return '';
                    } else {
                        return new Date(value);
                    }
                };

                service.setDate = function (resource, property, value) {
                    // Information about type is stored in context.
                    property = service.prefix + property;
                    // For defensive approach we assume we can get empty date as unset value.
                    if (typeof value === 'undefined' || value === null || value === '') {
                        delete resource[property];
                    } else {
                        resource[property] = value;
                    }
                };

                service.getBoolean = function (resource, property) {
                    property = service.prefix + property;
                    if (typeof resource[property] === 'undefined') {
                        // We can not return reasonable default value here, so we return nothing.
                        return;
                    } else {
                        return resource[property];
                    }
                };

                service.setBoolean = function (resource, property, value) {
                    // Information about type is stored in context.
                    property = service.prefix + property;
                    if (typeof value === 'undefined') {
                        delete resource[property];
                    } else {
                        if (value) {
                            resource[property] = true;
                        } else {
                            resource[property] = false;
                        }
                    }
                };

                service.getList = function (resource, property) {
                    property = service.prefix + property;
                    if (typeof resource[property] === 'undefined') {
                        return [];
                    } else {
                        return resource[property];
                    }
                };

                service.setList = function (resource, property, value) {
                    property = service.prefix + property;
                    if (typeof value === 'undefined' || !$.isArray(value) || value.length === 0) {
                        delete resource[property];
                    } else {
                        resource[property] = value;
                    }
                };

                return service;
            }
        };
    }
    //
    function init(app) {
        app.factory('services.rdf.0.0.0', factoryFunction);
    }
    return init;
});