define([], function () {

    function factoryFunction(jsonldService) {

        var service = {
            'component': {},
            'connection': {}
        };

        var jsonld = jsonldService.jsonld();

        service.component.getIri = function (component) {
            return component['@id'];
        };

        service.component.getLabel = function (component) {
            return jsonld.getString(component,
                    'http://www.w3.org/2004/02/skos/core#prefLabel');
        };

        service.component.getDescription = function (component) {
            return jsonld.getString(component,
                    'http://purl.org/dc/terms/description');
        };

        service.component.getX = function (component) {
            return jsonld.getInteger(component,
                    'http://linkedpipes.com/ontology/x');
        };

        service.component.getY = function (component) {
            return jsonld.getInteger(component,
                    'http://linkedpipes.com/ontology/y');
        };

        service.component.getColor = function (component) {
            return jsonld.getString(component,
                    'http://linkedpipes.com/ontology/color');
        };

        service.component.getTemplateIri = function (component) {
            return jsonld.getReference(component,
                    'http://linkedpipes.com/ontology/template');
        };

        service.component.setPosition = function (component, x, y) {
            component['http://linkedpipes.com/ontology/x'] = x;
            component['http://linkedpipes.com/ontology/y'] = y;
        };

        /**
         *
         * @param model Pipeline model.
         * @param component
         * @param id ID used to create an URI.
         */
        service.component.setIriFromId = function (model, component, id) {
            component['@id'] = model.definition.iri + '/components/' + id;
        };

        service.connection.getSource = function (connection) {
            return jsonld.getReference(connection,
                    'http://linkedpipes.com/ontology/sourceComponent');
        };

        service.connection.getSourceBinding = function (connection) {
            return jsonld.getString(connection,
                    'http://linkedpipes.com/ontology/sourceBinding');
        };

        service.connection.getTarget = function (connection) {
            return jsonld.getReference(connection,
                    'http://linkedpipes.com/ontology/targetComponent');
        };

        service.connection.getTargetBinding = function (connection) {
            return jsonld.getString(connection,
                    'http://linkedpipes.com/ontology/targetBinding');
        };

        /**
         *
         * @param model
         * @returns New connectino model.
         */
        service.connection.createConnection = function (model) {
            var connection = {
                '@id': '',
                '@type': ['http://linkedpipes.com/ontology/Connection']
            };
            service.getDefinitionGraph(model).push(connection);
            return connection;
        };

        /**
         *
         * @param model
         * @returns New connectino model.
         */
        service.connection.createRunAfter = function (model) {
            var connection = {
                '@id': '',
                '@type': ['http://linkedpipes.com/ontology/RunAfter']
            };
            service.getDefinitionGraph(model).push(connection);
            return connection;
        };

        service.connection.createVertex = function (x, y) {
            return {
                '@id': '',
                '@type': 'http://linkedpipes.com/ontology/Vertex',
                'http://linkedpipes.com/ontology/x': x,
                'http://linkedpipes.com/ontology/y': y,
                'http://linkedpipes.com/ontology/order': ''
            };
        };

        /**
         *
         * @param model Pipeline model.
         * @param connection
         * @param id ID used to create an URI.
         */
        service.connection.setIriFromId = function (model, component, id) {
            component['@id'] = model.definition.iri + '/connection/' + id;
        };

        service.connection.setSource = function (connection, component, binding) {
            connection['http://linkedpipes.com/ontology/sourceComponent'] = {
                '@id': component['@id']
            };
            if (binding) {
                connection['http://linkedpipes.com/ontology/sourceBinding']
                        = binding;
            }
        };

        service.connection.setTarget = function (connection, component, binding) {
            connection['http://linkedpipes.com/ontology/targetComponent'] =
                    {'@id': component['@id']};
            if (binding) {
                connection['http://linkedpipes.com/ontology/targetBinding']
                        = binding;
            }
        };

        /**
         * This function does not return the vertices objects but their
         * representation that can be used by view.
         *
         * @param model
         * @param connection Connection object.
         * @returns Ordered list of verticies {x, y, order}.
         */
        service.connection.getVerticesView = function (model, connection) {
            var verticiesIri = jsonld.getReferenceAll(connection,
                    'http://linkedpipes.com/ontology/vertex');
            if (!verticiesIri || verticiesIri.length === 0) {
                return [];
            }
            var result = [];
            // TODO Use caching and map here!
            var allVerticies = service.findByType(
                    service.getDefinitionGraph(model),
                    'http://linkedpipes.com/ontology/Vertex');
            verticiesIri.forEach(function (iri) {
                // Search for vertext object.
                for (var index in allVerticies) {
                    var vertex = allVerticies[index];
                    if (vertex['@id'] === iri) {
                        result.push({
                            'x': jsonld.getInteger(vertex,
                                    'http://linkedpipes.com/ontology/x'),
                            'y': jsonld.getInteger(vertex,
                                    'http://linkedpipes.com/ontology/y'),
                            'order': jsonld.getInteger(vertex,
                                    'http://linkedpipes.com/ontology/order')
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
         * @param model
         * @param connection Connection object.
         * @param vertices Ordered list of vertices.
         */
        service.connection.setVertices = function (model, connection, vertices) {
            // Remove old.
            var verticiesIri = jsonld.getReferenceAll(connection,
                    'http://linkedpipes.com/ontology/vertex');
            verticiesIri.forEach(function (iri) {
                service.delete(model, iri);
            });
            // Add new.
            if (!vertices || vertices.length === 0) {
                return;
            }
            connection['http://linkedpipes.com/ontology/vertex'] = [];
            var order = 1;
            var definition = service.getDefinitionGraph(model);
            vertices.forEach(function (vertex) {
                vertex['@id'] = connection['@id'] + '/vertex/' + order;
                vertex['http://linkedpipes.com/ontology/order'] = order;
                connection['http://linkedpipes.com/ontology/vertex'].push({
                    '@id': vertex['@id']
                });
                definition.push(vertex);
                order += 1;
            });
        };

        service.getLabel = function (model) {
            var pipeline = service.getPipeline(model);
            return jsonld.getString(pipeline,
                    'http://www.w3.org/2004/02/skos/core#prefLabel');
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
         * @param graph The content of JSON-LD graph object.
         * @param type Required resource's graph-type.
         * @returns List of objects with given type.
         */
        service.findByType = function (graph, type) {
            var resources = [];
            graph.forEach(function (resource) {
                if (resource['@type'].indexOf(type) !== -1) {
                    resources.push(resource);
                }
            });
            return resources;
        };

        /**
         *
         * @param model Pipeline model object.
         * @param iri
         * @returns Component with URI, or nothing if no such component exists.
         */
        service.getResource = function (model, iri) {
            var graph = service.getDefinitionGraph(model);
            for (var index in graph) {
                var item = graph[index];
                if (item && item['@id'] === iri) {
                    return item;
                }
            }
        };

        /**
         * Place null on the place of resource with given URI. Call
         * reorganize function to remove the null values.
         * If given resource is a connection then also remove all its verticies.
         *
         * @param model
         * @param iri Resource URI.
         */
        service.delete = function (model, iri) {
            var graph = service.getDefinitionGraph(model);
            for (var index in graph) {
                var resource = graph[index];
                if (resource === null) {
                    // Empty record.
                    continue;
                }
                if (resource['@id'] !== iri) {
                    continue;
                }
                // Check if given object is a connection and if so,
                // remove all it's verticies.
                if (resource['@type'].indexOf(
                        'http://linkedpipes.com/ontology/Connection') !== -1) {
                    service.connection.setVertices(model, graph[index], []);
                }
                graph[index] = null;
                break;
            }
        };

        /**
         * Perform reorganizatoin, ie. remove 'null' elements from definition
         * graph.
         *
         * @param model Pipeline model.
         */
        service.reorganize = function (model) {
            // TODO Be more effective here - shift all nulls to end and
            // then update at once.
            var collection = service.getDefinitionGraph(model);
            for (var index = collection.length; index > 0; index--) {
                if (collection[index] === null) {
                    collection.splice(index, 1);
                }
            }
        };

        /**
         *
         * @param model
         * @returns Object with pipeline definition.
         */
        service.getPipeline = function (model) {
            return service.findByType(service.getDefinitionGraph(model),
                    'http://linkedpipes.com/ontology/Pipeline')[0];
        };

        service.getComponents = function (model) {
            return service.findByType(service.getDefinitionGraph(model),
                    'http://linkedpipes.com/ontology/Component');
        };

        service.getConnections = function (model) {
            return service.findByType(service.getDefinitionGraph(model),
                    'http://linkedpipes.com/ontology/Connection');
        };

        service.getRunAfter = function (model) {
            return service.findByType(service.getDefinitionGraph(model),
                    'http://linkedpipes.com/ontology/RunAfter');
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
                'http://linkedpipes.com/ontology/template': {
                    '@id': template.id
                }
            };
            service.getDefinitionGraph(model).push(component);
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
            service.getDefinitionGraph(model).push(newComponent);
            // Move component.
            service.component.setPosition(newComponent,
                    service.component.getX(component),
                    service.component.getY(component) + 100);
            service.component.setIriFromId(model, newComponent, id);
            // Copy configuration if it exists.
            var configIri = jsonld.getReference(component,
                    'http://linkedpipes.com/ontology/configurationGraph');
            if (configIri) {
                var newConfigUri = newComponent['@id'] + '/configuration';
                newComponent['http://linkedpipes.com/ontology/configurationGraph'] = {
                    '@id': newConfigUri
                };
                var configGraph = [];
                model['graphs'][configIri].forEach(function (item) {
                    configGraph.push(jQuery.extend(true, {}, item));
                });
                model['graphs'][newConfigUri] = configGraph;
                console.log('graph', model['graphs']);
            }
            return newComponent;
        };

        /**
         *
         * @param data JSON-LD pipeline object.
         * @returns Pipeline model.
         */
        service.fromJsonLd = function (data) {

            var model = {
                /**
                 * Conteins the whole pipeline graph.
                 */
                'data': data,
                /**
                 * Store references of all graphs.
                 */
                'graphs': {}
            };

            jsonld.iterateGraphs(data, function (graph, graph_iri) {
                model.graphs[graph_iri] = graph;
            });

            // Search for pipeline definition.
            var pipeline = jsonld.query(data, {
                'property': '@type',
                'operation': 'in',
                'value': 'http://linkedpipes.com/ontology/Pipeline'
            }, false);

            // Store the definition graph.
            model.definition = {
                'graph': model.graphs[pipeline.graphIri],
                'iri': pipeline.graphIri
            };
            return model;
        };

        /**
         *
         * @param model Pipeline model.
         * @returns JSON-LD pipeline object.
         */
        service.toJsonLd = function (model) {
            //
            service.reorganize(model);
            //
            var output = {
                '@graph': []
            };
            for (var iri in model.graphs) {
                var graph = model.graphs[iri];
                // Some graphs may not have a context specified.
                var newGraph = {
                    '@graph': graph,
                    '@id': iri
                };
                if (graph['@context']) {
                    newGraph['@context'] = graph['@context'];
                }
                output['@graph'].push(newGraph);
            }
            return output;
        };

        // ------------------------------------------------------------------ //

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

        return service;
    }

    return function register(app) {
        app.factory('components.pipelines.services.model',
                ['services.jsonld', factoryFunction]);
    };

});