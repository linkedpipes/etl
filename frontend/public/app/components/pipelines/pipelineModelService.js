define([], function () {
    function factoryFunction() {

        var service = {};

        /**
         *
         * @param json JSON-LD pipeline object.
         * @returns Pipeline model.
         */
        service.modelFromJsonLd = function (json) {
            var model = {
                'data': json,
                'graphs': {}
            };
            // Search graphs.
            json['@graph'].forEach(function (graph) {
                model.graphs[graph['@id']] = graph;
                // Search for pipeline definition.
                if (!model.definition) {
                    graph['@graph'].forEach(function (resource) {
                        if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Pipeline') !== -1) {
                            model.definition = {
                                'uri': graph['@id'],
                                'graph': graph
                            };
                        }
                    });
                }
            });
            return model;
        };

        /**
         *
         * @param model Pipeline model.
         * @returns JSON-LD pipeline object.
         */
        service.modelToJsonLd = function (model) {
            var output = {
                '@graph': []
            };
            for (var uri in model.graphs) {
                var graph = model.graphs[uri];
                // Some graphs may not have a context specified.
                var newGraph = {
                    '@graph': graph['@graph'],
                    '@id': uri
                };
                if (graph['@context']) {
                    newGraph['@context'] = graph['@context'];
                }
                output['@graph'].push(newGraph);
            }
            return output;
        };

        /**
         *
         * @param model Pipeline model.
         * @returns Definition graph.
         */
        service.getDefinitionGraph = function (model) {
            return model.definition.graph;
        };

        /**
         *
         * @param model
         * @returns Object with pipeline definition.
         */
        service.getPipelineDefinition = function (model) {
            return service.findByType(service.getDefinitionGraph(model), 'http://linkedpipes.com/ontology/Pipeline')[0];
        };

        service.getComponents = function (model) {
            return service.findByType(service.getDefinitionGraph(model), 'http://linkedpipes.com/ontology/Component');
        };

        service.getConnections = function (model) {
            return service.findByType(service.getDefinitionGraph(model), 'http://linkedpipes.com/ontology/Connection');
        };

        service.getRunAfter = function (model) {
            return service.findByType(service.getDefinitionGraph(model), 'http://linkedpipes.com/ontology/RunAfter');
        };
        /**
         * This function does not return the vertices objects but their representation that can be used by view.
         *
         * @param model
         * @param connection Connection object.
         * @returns Ordered list of objects {x, y, order} that represent vertices.
         */
        service.getVertices = function (model, connection) {
            var verticiesUri = connection['http://linkedpipes.com/ontology/vertex'];
            if (!verticiesUri || verticiesUri.length === 0) {
                return [];
            }
            var result = [];

            // TODO Use caching and map here!
            var allVerticies = service.findByType(service.getDefinitionGraph(model), 'http://linkedpipes.com/ontology/Vertex');
            verticiesUri.forEach(function (vertexReference) {
                // Search for vertext object.
                for (var index in allVerticies) {
                    var item = allVerticies[index];
                    if (item['@id'] === vertexReference['@id']) {
                        result.push({
                            'x': item['http://linkedpipes.com/ontology/x'],
                            'y': item['http://linkedpipes.com/ontology/y'],
                            'order': item['http://linkedpipes.com/ontology/order']
                        });
                    }
                }
            });
            result.sort(function (left, right) {
                return left['order'] - right['order'];
            });
            return result;
        };

        /**
         *
         * @param model Pipeline model object.
         * @param uri
         * @returns Component with URI, or nothing if no such component exists.
         */
        service.getResource = function (model, uri) {
            var graph = service.getDefinitionGraph(model)['@graph'];
            for (var index in graph) {
                var item = graph[index];
                if (item['@id'] === uri) {
                    return item;
                }
            }
        };

        /**
         *
         * @param graph The JSON-LD graph object.
         * @param type
         * @returns List of objects with given type.
         */
        service.findByType = function (graph, type) {
            var resources = [];
            graph['@graph'].forEach(function (resource) {
                if (resource['@type'].indexOf(type) !== -1) {
                    resources.push(resource);
                }
            });
            return resources;
        };

        /**
         *
         * @param model Pipeline model.
         * @param template Template object.
         * @returns New component model, without URI and position.
         */
        service.createComponent = function (model, template) {
            var component = {
                '@id': '',
                '@type': ['http://linkedpipes.com/ontology/Component'],
                'http://www.w3.org/2004/02/skos/core#prefLabel': template.label,
                'http://linkedpipes.com/ontology/template': {'@id': template.id}
            };
            service.getDefinitionGraph(model)['@graph'].push(component);
            return component;
        };

        /**
         * Clone component with configuration.
         *
         * @param model
         * @param component Component to clone.
         * @param id New component ID.
         * @returns Clone of given component, without the URI set.
         */
        service.cloneComponent = function (model, component, id) {
            var newComponent = jQuery.extend(true, {}, component);
            service.getDefinitionGraph(model)['@graph'].push(newComponent);
            // Move component.
            newComponent["http://linkedpipes.com/ontology/y"] += 100;
            // Set new URI.
            service.setComponentUriFromId(model, newComponent, id);
            // Copy configuration if it exists.
            if (component['http://linkedpipes.com/ontology/configurationGraph']) {
                var configUri = component['http://linkedpipes.com/ontology/configurationGraph']['@id'];
                var newConfigUri = newComponent['@id'] + '/configuration';
                newComponent['http://linkedpipes.com/ontology/configurationGraph'] = {
                    '@id': newConfigUri
                };
                model['graphs'][newConfigUri] = jQuery.extend(true, {}, model['graphs'][configUri]);
            }
            return newComponent;
        };

        /**
         *
         * @param model
         * @returns New connectino model.
         */
        service.createConnection = function (model) {
            var connection = {
                '@id': '',
                '@type': ['http://linkedpipes.com/ontology/Connection']
            };
            service.getDefinitionGraph(model)['@graph'].push(connection);
            return connection;
        };

        /**
         *
         * @param model
         * @returns New connectino model.
         */
        service.createRunAfter = function (model) {
            var connection = {
                '@id': '',
                '@type': ['http://linkedpipes.com/ontology/RunAfter']
            };
            service.getDefinitionGraph(model)['@graph'].push(connection);
            return connection;
        };

        /**
         * Place null on the place of resource with given URI.
         *
         * If given resource is a connection then also remove all its verticies.
         *
         * @param model
         * @param uri Resource URI.
         * @param reorganize If true then reorganization is done after removal.
         */
        service.removeResource = function (model, uri, reorganize) {
            var collection = service.getDefinitionGraph(model)['@graph'];
            for (var index in collection) {
                if (collection[index] !== null && collection[index]['@id'] === uri) {
                    // Check if given object is a connection and if so, remove all it's verticies.
                    if (collection[index]['@type'].indexOf('http://linkedpipes.com/ontology/Connection') !== -1) {
                        service.removeVertices(model, collection[index]);
                    }
                    collection[index] = null;
                    break;
                }
            }
            if (reorganize) {
                service.reorganize(model);
            }
        };

        /**
         * Perform reorganizatoin, ie. remove 'null' elements from definition graph.
         *
         * @param model Pipeline model.
         */
        service.reorganize = function (model) {
            // TODO Be more effective here - shift all nulls to end and then update at once.
            var collection = service.getDefinitionGraph(model)['@graph'];
            for (var index = collection.length; index > 0; index--) {
                if (collection[index] === null) {
                    collection.splice(index, 1);
                }
            }
        };

        /**
         *
         * @param model Pipeline model.
         * @param component
         * @param id ID used to create an URI.
         */
        service.setComponentUriFromId = function (model, component, id) {
            service.setComponentUri(model, component, model.definition.uri + '/components/' + id);
        };

        /**
         *
         * @param model Pipeline model.
         * @param connection
         * @param id ID used to create an URI.
         */
        service.setConnectionUriFromId = function (model, component, id) {
            service.setComponentUri(model, component, model.definition.uri + '/connection/' + id);
        };

        /**
         *
         * @param model Pipeline model.
         * @param component
         * @param uri New URI.
         */
        service.setComponentUri = function (model, component, uri) {
            component['@id'] = uri;
        };

        /**
         *
         * @param component Component.
         * @param x Position x.
         * @param y Position y.
         */
        service.setComponentPosition = function (component, x, y) {
            component['http://linkedpipes.com/ontology/x'] = x;
            component['http://linkedpipes.com/ontology/y'] = y;
        };

        service.setConnectionSource = function (connection, component, binding) {
            connection['http://linkedpipes.com/ontology/sourceComponent'] = {'@id': component['@id']};
            if (binding) {
                connection['http://linkedpipes.com/ontology/sourceBinding'] = binding;
            }
        };

        service.setConnectionTarget = function (connection, component, binding) {
            connection['http://linkedpipes.com/ontology/targetComponent'] = {'@id': component['@id']};
            if (binding) {
                connection['http://linkedpipes.com/ontology/targetBinding'] = binding;
            }
        };

        service.getComponentLabel = function (component) {
            return component['http://www.w3.org/2004/02/skos/core#prefLabel'];
        };

        service.setComponentLabel = function (component, label) {
            component['http://www.w3.org/2004/02/skos/core#prefLabel'] = label;
        };

        service.getComponentColor = function (component) {
            return component['http://linkedpipes.com/ontology/color'];
        };

        service.setComponentColor = function (component, color) {
            if (color) {
                component['http://linkedpipes.com/ontology/color'] = color;
            } else if (component['http://linkedpipes.com/ontology/color']) {
                delete component['http://linkedpipes.com/ontology/color'];
            }
        };

        service.getComponentConfigurationUri = function (component) {
            if (component['http://linkedpipes.com/ontology/configurationGraph']) {
                return component['http://linkedpipes.com/ontology/configurationGraph']['@id'];
            }
        };

        service.getComponentConfigurationGraph = function (model, component) {
            var uri = service.getComponentConfigurationUri(component);
            if (uri) {
                return model['graphs'][uri];
            }
        };

        service.setComponentConfiguration = function (model, component, uri, graph) {
            var oldUri = service.getComponentConfigurationUri(component);
            if (oldUri !== uri) {
                delete model['graphs'][oldUri];
            }
            component['http://linkedpipes.com/ontology/configurationGraph'] = {'@id': uri};
            model['graphs'][uri] = graph;
        };

        service.getComponentTemplateUri = function (component) {
            return component['http://linkedpipes.com/ontology/template']['@id'];
        };

        service.removeVertices = function (model, connection) {
            var verticiesUri = connection['http://linkedpipes.com/ontology/vertex'];
            if (!verticiesUri || verticiesUri.length === 0) {
                return [];
            }
            verticiesUri.forEach(function (item) {
                service.removeResource(model, item['@id']);
            });
        };

        /**
         *
         * @param model
         * @param connection Connection object.
         * @param vertices Ordered list of vertices.
         */
        service.setVertices = function (model, connection, vertices) {
            service.removeVertices(model, connection);
            if (!vertices || vertices.length === 0) {
                return;
            }
            connection['http://linkedpipes.com/ontology/vertex'] = [];
            var order = 1;
            var definition = service.getDefinitionGraph(model)['@graph'];
            vertices.forEach(function (vertex) {
                vertex['@id'] = connection['@id'] + '/vertex/' + order;
                vertex['http://linkedpipes.com/ontology/order'] = order;
                connection['http://linkedpipes.com/ontology/vertex'].push({'@id': vertex['@id']});
                definition.push(vertex);
                order += 1;
            });
        };

        /**
         *
         * @param x
         * @param y
         * @returns New vertex object.
         */
        service.createVertex = function (x, y) {
            return {
                '@id': '',
                '@type': 'http://linkedpipes.com/ontology/Vertex',
                'http://linkedpipes.com/ontology/x': x,
                'http://linkedpipes.com/ontology/y': y,
                'http://linkedpipes.com/ontology/order': ''
            };
        };


        return service;
    }
    /**
     *
     * @param app Angular modeule.
     */
    function register(app) {
        app.factory('components.pipelines.services.model', factoryFunction);
    }
    return register;
});