/**
 *
 */
define([], function () {

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

    /**
     * Load and return metadata object.
     */
    var parseMetadata = function (jsonld) {
        var json = jsonld.toJson({
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

    /**
     * Data must be array of graphs.
     */
    function JsonldObject(data) {
        this.data = data;
        // Store current data, use for chaining.
        this.current = {};

        /**
         * Evaluate query and use result objects to construct JSON
         * based on given tempalte. For each query result object
         * create a separated JSON, ie. return array of JSON objects.
         */
        this.toJson = function (query, template) {
            var objects = [];
            this.iterateObjects(function (object, graph) {
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
                json.push(evaluateTemplate(this.data,
                        object['graph'], object['value'], template));
            }
            return json;
        };

        /**
         * Itarate over all nobjects, if callback return false
         * then stop the iteration.
         */
        this.iterateObjects = function (callback) {
            for (var graphIndex in this.data) {
                var graph = this.data[graphIndex];
                for (var object_index in graph['@graph']) {
                    callback(graph['@graph'][object_index], graph['@id']);
                }
            }
        };
    }

    function JsonldRepository(settings) {
        this.data = [];
        this.template = settings['template'];
        this.query = settings['query'];
        this.loaded = false;
        this.loading = false;
        this.url = settings['url'];
        this.decorator = settings['decorator'];
        this.lastCheck = '';
    }

    //
    function factoryFunction($http) {
        return {
            'createJsonld' : function(data) {
                return new JsonldObject(data);
            },
            'createRepository': function (settings) {
                return new JsonldRepository(settings);
            },
            'load': function (repository, onSuccess, onError) {
                if (repository.loading) {
                    return;
                }
                //
                repository.loading = true;
                //
                $http.get('/resources/executions').then(function (response) {
                    console.time('Loading data');
                    var jsonld = new JsonldObject(response.data);
                    var json = jsonld.toJson(repository.query.data,
                            repository.template);
                    // Decorate items.
                    json.forEach(repository.decorator);
                    // Store all data.
                    repository.data = json;
                    //
                    var metadata = parseMetadata(jsonld);
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
            },
            'update': function (repository, onSuccess, onError) {
                /**
                 * Update content of the repository.
                 */
                if (!repository.loaded) {
                    // We need to load before updating.
                    this.load(repository, onSuccess, onError);
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
                    console.time('Updating data');
                    var jsonld = new JsonldObject(response.data);
                    // Delete executions, based on tombstones.
                    var deleted = jsonld.toJson(repository.query.deleted, {
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
                    var json = jsonld.toJson(repository.query.data,
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
                    var metadata = parseMetadata(jsonld);
                    repository.lastCheck = metadata.serverTime;
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
                    repository.loading = false;
                });
            },
            'delete': function (repository, object, onSuccess, onError) {
                $http({method: 'DELETE', url: object.iri})
                        .then(function () {
                            if (onSuccess) {
                                onSuccess();
                            }
                        }, function (response) {
                            if (onError) {
                                onError(response);
                            }
                        });
            }

        };
    }
    //
    factoryFunction.$inject = [];
    //
    function init(app) {
        app.factory('services.jsonld', ['$http', factoryFunction]);
    }
    return init;
});
