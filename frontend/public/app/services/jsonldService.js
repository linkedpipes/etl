define(['jquery'], function (jQuery) {

    /**
     * Evaluate given filter on given resource.
     */
    var evaluteFilter = function (resource, filter) {
        var value = resource[filter['property']];
        if (typeof value === "undefined") {
            return false;
        }
        //
        if (filter['operation'] === 'in') {
            if (jQuery.isArray(value)) {
                return value.indexOf(filter['value']) !== -1;
            } else {
                return value === filter['value'];
            }
        }
        // Unknown filter.
        return false;
    };

    /**
     * Read and store value based on the reference template.
     *
     * @param result The output object.
     */
    var convertOneToOne = function (value, result, data, graph, templateItem) {
        if (value[0] && value[0]['@id']) {
            var resource = getResourceByIri(data, graph,
                    value[0]['@id']);
            // Check if we have the reference to the object.
            if (typeof resource === 'undefined') {
                return;
            }
            var toAdd = evaluateTemplate(data, graph, resource,
                    templateItem['$oneToOne']);
            // Merge results.
            for (var key in toAdd) {
                result[key] = toAdd[key];
            }
        }
    };

    /**
     * Convert value with string to the string langugae map.
     */
    var convertString = function (value) {

        /**
         * Return object with @lang and @value.
         */
        var getString = function (value, result) {
            if (typeof value['@value'] === 'undefined') {
                result[''] = value;
            } else if (typeof value['@lang'] === 'undefined') {
                result[''] = value['@value'];
            } else {
                result[value['@lang']] = value['@value'];
            }
        };

        var result = {};
        if (jQuery.isArray(value)) {
            if (value.length === 0) {
                // Skip as empty array does not contains any data.
                return;
            } else if (value.length === 1) {
                getString(value[0], result);
            } else {
                for (var itemIndex in value) {
                    getString(value[itemIndex], result);
                }
            }
        } else {
            getString(value, result);
        }
        return result;
    };

    /**
     * Convert given object (value of a property) into a value
     * usable in JSON.
     */
    var convertValue = function (value) {

        /**
         * Can be used to get no n referenced value.
         */
        var getValue = function (value) {
            if (typeof value['@id'] !== 'undefined') {
                // Represent an IRI.
                return value['@id'];
            } else if (typeof value['@value'] !== 'undefined') {
                // TODO: We can try to use @type here to parse the value.
                return value['@value'];
            } else {
                // Simple value, stored directly as value.
                return value;
            }
        };

        var result;
        if (jQuery.isArray(value)) {
            if (value.length === 0) {
                // Skip as empty array does not contains any data.
                return;
            } else if (value.length === 1) {
                result = getValue(value[0]);
            } else {
                result = [];
                for (var itemIndex in value) {
                    result.push(getValue(value[itemIndex]));
                }
            }
        } else {
            result = getValue(value);
        }
        return result;
    };

    /**
     * Find and return resource in given graph with given IRI.
     */
    var getResourceByIri = function (data, graphIri, iri) {
        if (typeof graphIri === 'undefined') {
            // Search in every graph.
            for (var graphIndex in data) {
                var graph = data[graphIndex];
                for (var object_index in graph['@graph']) {
                    if (graph['@graph'][object_index]['@id'] &&
                            graph['@graph'][object_index] === iri) {
                        return graph['@graph'][object_index];
                    }
                }
            }
        } else {
            // Search only in given graph.
            for (var graphIndex in data) {
                if (data[graphIndex]['@id'] === graphIri) {
                    var graph = data[graphIndex];
                    for (var object_index in graph['@graph']) {
                        if (graph['@graph'][object_index]['@id'] === iri) {
                            return graph['@graph'][object_index];
                        }
                    }
                    break;
                }
            }
        }
    };

    var evaluateTemplate = function (data, graph, object, template) {
        var result = {};
        for (var key in template) {
            var templateItem = template[key];
            // Check for '$resource' template.
            if (typeof templateItem['$resource'] !== 'undefined') {
                result[key] = object['@id'];
                continue
            }
            // Check for object without $property - sub-objects.
            if (typeof templateItem['$property'] === 'undefined') {
                result[key] = evaluateTemplate(data, graph, object,
                        templateItem);
                continue;
            }
            // Check if the value is property from a template is presented
            // ub the object.
            if (typeof object[templateItem['$property']] === 'undefined') {
                continue;
            }
            var propertyValue = object[templateItem['$property']];
            // Select a convertion function based on the template.
            if (typeof templateItem['$oneToOne'] !== 'undefined') {
                // Reference we should continue with another object.
                convertOneToOne(propertyValue, result, data, graph, templateItem);
            } else if (templateItem['$type'] === 'string') {
                var value = convertString(propertyValue);
                if (typeof value !== 'undefined') {
                    result[key] = value;
                }
            } else {
                var value = convertValue(propertyValue);
                if (typeof value !== 'undefined') {
                    result[key] = value;
                }
            }
        }
        return result;
    };

    // ---------------------------------------------------------------------- //

    var jsonldService = {};

    /**
     * Iterate graphs. Call callback as callback(graph, graph_iri). If callback
     * return true then stop the iteration and return graph's id.
     * In other case return nothing.
     */
    jsonldService.iterateGraphs = function (data, callback) {
        var graphList;
        if (data['@graph']) {
            if (data['@id']) {
                // There is a graph directly in the data root.
                graphList = [data];
            } else {
                graphList = data['@graph'];
            }
        } else {
            graphList = data;
        }
        //
        for (var graphIndex in graphList) {
            var graph = graphList[graphIndex];
            if (graph['@graph'] && graph['@id']) {
                if (callback(graph['@graph'], graph['@id'])) {
                    return graph['@id'];
                }
            } else {
                console.log('Invalid graph detected: ', graph);
            }
        }
    };

    /**
     * Itarate over all objects, if callback(resource, graph_iri) return true
     * then stop the iteration and return object with graph name
     * and resource IRI.
     */
    jsonldService.iterateObjects = function (data, callback) {
        var result;
        this.iterateGraphs(data, function (graph, iri) {
            for (var object_index in graph) {
                if (callback(graph[object_index], iri)) {
                    result = {
                        'resource': graph[object_index],
                        'graph': iri
                    };
                    return true;
                }
            }
        });
        return result;
    };

    /**
     * Evaluate given query and return the result {resource_object, graph_iri}.
     */
    jsonldService.query = function (data, queryObject) {
        var result;
        this.iterateObjects(data, function (object, graphIri) {
            if (evaluteFilter(object, queryObject)) {
                result = {
                    'resource': object,
                    'graphIri': graphIri
                };
                return true;
            }
        });
        return result;
    };

    jsonldService.queryAll = function (data, queryObject) {
        var result = [];
        this.iterateObjects(data, function (object, graphIri) {
            if (evaluteFilter(object, queryObject)) {
                result.push({
                    'resource': object,
                    'graphIri': graphIri
                });
            }
        });
        return result;
    };

    /**
     * Return IRI of the reference. Ie. IRI of object stored under given
     * predicate. Return nothing if there are no data.
     */
    jsonldService.getReference = function (object, property) {
        if (!object[property]) {
            return;
        }
        var value = object[property];
        if (value['@id']) {
            return value['@id'];
        } else if (value[0]['@id']) {
            return value[0]['@id'];
        } else {
            return value['@id'];
        }
    };

    /**
     * Return array of references IRI. Ie. IRI of object stored under given
     * predicate. Return empty array if there are no references or
     * the property is missing.
     */
    jsonldService.getReferenceAll = function (object, property) {
        if (!object[property]) {
            return [];
        }
        var value = object[property];
        if (jQuery.isArray(value)) {
            var result = [];
            value.forEach(function (item) {
                if (item['@id']) {
                    result.push(item['@id']);
                } else {
                    result.push(item);
                }
            });
            return result;
        } else {
            return [this.getReference(object, property)];
        }
        return [];
    };

    jsonldService.getString = function (object, property) {
        var value = object[property];
        if (!value) {
            return;
        }
        if (jQuery.isArray(value)) {
            value = value[0];
            if (!value) {
                return;
            }
            //
            if (value['@value']) {
                return value['@value'];
            } else {
                return value;
            }
        } else {
            if (value['@value']) {
                return value['@value'];
            } else {
                return value;
            }
        }
    };

    jsonldService.getInteger = function (object, property) {
        if (!object[property]) {
            return;
        }
        var value = object[property];
        if (jQuery.isArray(value)) {
            value = value[0];
        }
        if (value['@value']) {
            return parseInt(value['@value']);
        } else {
            return parseInt(value);
        }
    };

    // ---------------------------------------------------------------------- //

    var toJson = function (data, query, template) {
        var objects = [];
        jsonldService.iterateObjects(data, function (object, graph) {
            if (evaluteFilter(object, query)) {
                objects.push({
                    'value': object,
                    'graph': graph
                });
            }
        });
        var json = [];
        for (var object_index in objects) {
            var object = objects[object_index];
            json.push(evaluateTemplate(data,
                    object['graph'], object['value'], template));
        }
        return json;
    };

    /**
     * Class that cen be used to load, update and delete jsonld object.
     */
    function JsonldRepository(settings, $http) {
        this.data = [];
        this.template = settings['template'];
        this.query = settings['query'];
        this.loaded = false;
        this.loading = false;
        this.url = settings['url'];
        this.decorator = settings['decorator'];
        this.lastCheck = '';
        var repository = this;

        /**
         * Load and return metadata object.
         */
        var parseMetadata = function (data) {
            var json = toJson(data, {
                'property': '@type',
                'operation': 'in',
                'value': 'http://etl.linkedpipes.com/ontology/Metadata'
            }, {
                'serverTime': {
                    '$property': 'http://etl.linkedpipes.com/ontology/serverTime'
                }
            });
            return json[0];
        };

        this.load = function (onSuccess, onError) {
            if (this.loading) {
                return;
            }
            //
            this.loading = true;

            //
            $http.get('/resources/executions').then(function (response) {
                console.time('Loading data');
                var json = toJson(response.data, repository.query.data,
                        repository.template);
                // Decorate items.
                json.forEach(repository.decorator);
                // Store all data.
                repository.data = json;
                //
                var metadata = parseMetadata(response.data);
                repository.lastCheck = metadata.serverTime;
                //
                console.timeEnd('Loading data');
                if (onSuccess) {
                    onSuccess();
                }
                //
                repository.loaded = true;
                repository.loading = false;
            }, function (response) {
                if (onError) {
                    onError(response);
                }
                //
                repository.loading = false;
            });
        };

        this.update = function (onSuccess, onError) {
            /**
             * Update content of the this.
             */
            if (!repository.loaded) {
                // We need to load before updating.
                repository.load(onSuccess, onError);
                return;
            }
            //
            if (repository.loading) {
                return;
            }
            //
            repository.loading = true;
            //
            $http.get('/resources/executions', {
                params: {'changedSince': repository.lastCheck}}
            ).then(function (response) {
                // Delete executions, based on tombstones.
                var deleted = toJson(response.data, repository.query.deleted, {
                    'iri': {'$resource': ''}
                });
                deleted.forEach(function (execution) {
                    for (var i = repository.data.length - 1; i >= 0; --i) {
                        if (repository.data[i].iri === execution.iri) {
                            repository.data.splice(i, 1);
                        }
                    }
                });
                // Update and add new executions.
                var json = toJson(response.data, repository.query.data,
                        repository.template);
                // Decorate items.
                json.forEach(function (item) {
                    repository.decorator(item);
                    //
                    var isNew = true;
                    for (var i = 0; i < repository.data.length; i++) {
                        if (repository.data[i].iri === item.iri) {
                            // Replace original value.
                            repository.data[i] = item;
                            isNew = false;
                            break;
                        }
                    }
                    if (isNew) {
                        repository.data.push(item);
                    }
                });
                //
                var metadata = parseMetadata(response.data);
                this.lastCheck = metadata.serverTime;
                //
                console.timeEnd('Updating data');
                if (onSuccess) {
                    onSuccess();
                }
                //
                repository.loaded = true;
                repository.loading = false;
            }, function (response) {
                if (onError) {
                    onError(response);
                }
                //
                this.loading = false;
            });
        };

        this.delete = function (object, onSuccess, onError) {
            $http({method: 'DELETE', url: object.iri}).then(function () {
                if (onSuccess) {
                    onSuccess();
                }
            }, function (response) {
                if (onError) {
                    onError(response);
                }
            });
        };

    }

    function factoryFunction($http) {
        return {
            'toJson': function (data, query, template) {
                return toJson(data, query, template);
            },
            'createRepository': function (settings) {
                return new JsonldRepository(settings, $http);
            },
            'jsonld': function () {
                return jsonldService;
            }
        };
    }

    factoryFunction.$inject = [];

    return function init(app) {
        app.factory('services.jsonld', ['$http', factoryFunction]);
    };

});
