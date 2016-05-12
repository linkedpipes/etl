//
// Contains definition of an execution model. This model can be used
// to read informations from an execution.
//
// TODO jsonld should be requireJs dependecy, factory funciton can return
//  instance of the service class only
//

define([
    'jquery'
], function (jQuery) {

    /**
     * Return object for component of given IRI, if object does not exists
     * in the model it's created.
     */
    function getComponent(model, iri) {
        if (model.components[iri] === undefined) {
            model.components[iri] = {
                'iri': iri,
                'messages': [],
                'mapping' : {
                    /**
                     * True if mapping is on for this component.
                     */
                    'enabled' : true,
                    /**
                     * True if there is possibility to map this component.
                     */
                    'available': false
                }
            };
        }
        return model.components[iri];
    }

    /**
     * Load Execution type resource into model.
     */
    function loadExecution(model, resource, jsonld) {
        model.execution.iri = resource['@id'];
        model.pipeline.iri = jsonld.getReference(resource,
                'http://etl.linkedpipes.com/ontology/pipeline');
        var status = jsonld.getReference(resource,
                'http://etl.linkedpipes.com/ontology/status');
        model.execution.status.iri = status;
        switch (status) {
            case 'http://etl.linkedpipes.com/resources/status/finished':
            case 'http://etl.linkedpipes.com/resources/status/failed':
                model.execution.status.running = false;
                break;
            default:
                model.execution.status.running = true;
                break;
        }
    }

    /**
     * Load ExecutionBegin type resource into model.
     */
    function loadExecutionBegin(model, resource, jsonld) {
        model.execution.start = jsonld.getString(resource,
                'http://linkedpipes.com/ontology/events/created');
    }

    /**
     * Load ExecutionBegin type resource into model.
     */
    function loadExecutionEnd(model, resource, jsonld) {
        model.execution.end = jsonld.getString(resource,
                'http://linkedpipes.com/ontology/events/created');
    }

    /**
     * Load ComponentBegin type resource into model.
     */
    function loadComponentBegin(model, resource, jsonld) {
        var component = getComponent(model, jsonld.getReference(resource,
                'http://linkedpipes.com/ontology/component'));
        component.start = jsonld.getString(resource,
                'http://linkedpipes.com/ontology/events/created');
    }

    /**
     * Load ComponentEnd type resource into model.
     */
    function loadComponentEnd(model, resource, jsonld) {
        var component = getComponent(model, jsonld.getReference(resource,
                'http://linkedpipes.com/ontology/component'));
        component.end = jsonld.getString(resource,
                'http://linkedpipes.com/ontology/events/created');
    }

    /**
     * Load ComponentFailed type resource into model.
     */
    function loadComponentFailed(model, resource, jsonld) {
        var component = getComponent(model, jsonld.getReference(resource,
                'http://linkedpipes.com/ontology/component'));
        component.end = jsonld.getString(resource,
                'http://linkedpipes.com/ontology/events/created');
        // Object with description of failed cause.
        component.failed = {
            'rootCause': jsonld.getString(resource,
                    'http://linkedpipes.com/ontology/events/rootException')
        };
    }

    /**
     * Load Component type resource into model.
     */
    function loadComponent(model, resource, jsonld) {
        var component = getComponent(model, resource['@id']);
        component.status = jsonld.getReference(resource,
                'http://etl.linkedpipes.com/ontology/status');
        component.order = jsonld.getInteger(resource,
                'http://linkedpipes.com/ontology/executionOrder');
        component.dataUnits = jsonld.getReferenceAll(resource,
                'http://etl.linkedpipes.com/ontology/dataUnit');
        // Check status for mapping.
        switch (component.status) {
            case 'http://etl.linkedpipes.com/resources/status/finished':
            case 'http://etl.linkedpipes.com/resources/status/mapped':
            case 'http://etl.linkedpipes.com/resources/status/failed':
                component.mapping.available = true;
                break;
            default:
                component.mapping.available = false;
                break;
        }
    }

    /**
     * Load DataUnit type resource into model.
     */
    function loadDataUnit(model, resource, jsonld) {
        var iri = resource['@id'];
        model.dataUnits[iri] = {
            'iri': iri,
            'binding': jsonld.getString(resource,
                    'http://etl.linkedpipes.com/ontology/binding'),
            'debug': jsonld.getString(resource,
                    'http://etl.linkedpipes.com/ontology/debug')
        };
    }

    /**
     * Load readProgressReport type resource into model.
     */
    function loadProgressReport(model, resource, jsonld) {
        var component = getComponent(model, jsonld.getReference(resource,
                'http://linkedpipes.com/ontology/component'));
        if (component.progress === undefined) {
            component.progress = {
                'total': jsonld.getInteger(resource,
                        'http://linkedpipes.com/ontology/progress/total'),
                'current': 0,
                'value': 0
            };
        }
        // Save progress.
        var progress = component.progress;
        var current = jsonld.getInteger(resource,
                'http://linkedpipes.com/ontology/progress/current');
        if (current <= progress.current) {
            return;
        }
        progress.current = current;
        progress.value = 100 * (progress.current / progress.total);
        // Save message.
        component.messages.push({
            'label': jsonld.getString(resource,
                    'http://www.w3.org/2004/02/skos/core#prefLabel'),
            // TODO Use getUpdate function.
            'created': jsonld.getString(resource,
                    'http://linkedpipes.com/ontology/events/created'),
            'order': jsonld.getInteger(resource,
                    'http://linkedpipes.com/ontology/order')
        });
    }

    // Store actions for loading RDF resources based on type.
    var loadActions = {
        'http://etl.linkedpipes.com/ontology/Execution':
                loadExecution,
        'http://linkedpipes.com/ontology/events/ExecutionBegin':
                loadExecutionBegin,
        'http://linkedpipes.com/ontology/events/ExecutionEnd':
                loadExecutionEnd,
        'http://linkedpipes.com/ontology/events/ComponentBegin':
                loadComponentBegin,
        'http://linkedpipes.com/ontology/events/ComponentEnd':
                loadComponentEnd,
        'http://linkedpipes.com/ontology/events/ComponentFailed':
                loadComponentFailed,
        'http://linkedpipes.com/ontology/Component':
                loadComponent,
        'http://etl.linkedpipes.com/ontology/DataUnit':
                loadDataUnit,
        'http://linkedpipes.com/ontology/progress/ProgressReport':
                loadProgressReport
    };

    // TODO Move to JsonLD service
    function getTypes(resource) {
        if (jQuery.isArray(resource['@type'])) {
            return resource['@type'];
        } else {
            return [resource['@type']];
        }
    }

    function loadModel(model, data, jsonld) {
        console.time('executionModel.loadModel');
        jsonld.iterateObjects(data, function (resource, graph) {
            var types = getTypes(resource);
            for (var index in types) {
                var action = loadActions[types[index]];
                if (action !== undefined) {
                    action(model, resource, jsonld);
                }
            }
        });
        console.timeEnd('executionModel.loadModel');
    }

    var service = {};

    service.loadJsonLd = function (data) {
        loadModel(this.data, data, this.jsonldService.jsonld());
    };

    service.getComponents = function () {
        return this.data.components;
    };

    service.getComponentStatus = function (resource) {
        return resource.status;
    };

    service.mapping = {};

    service.mapping.enable = function(resource) {
        resource.mapping.enabled = resource.mapping.available;
    };

    service.mapping.disable = function(resource) {
        resource.mapping.enabled = false;
    };

    service.mapping.isEnabled = function(resource) {
        // TODO Raise exception if this is not true!
        return resource.mapping.enabled;
    };

    /**
     * Report change on the component and thus disable mapping.
     */
    service.mapping.changed = function(resource) {
        resource.mapping.enabled = false;
        resource.mapping.available = false;
    };

    service.mapping.isAvailable = function(resource) {
        return resource.mapping.available;
    };

    service.isFinished = function () {
        if (this.data.execution.status.running === undefined) {
            return false;
        }
        return !this.data.execution.status.running;
    };

    service.getDataUnit = function(component, bindingName) {
        for (var index in component.dataUnits) {
            var dataUnit = this.data.dataUnits[component.dataUnits[index]];
            if (dataUnit !== undefined && dataUnit.binding === bindingName) {
                return dataUnit;
            }
        }
    };

    /**
     * Return execution IRI.
     */
    service.getIri = function() {
        return this.data.execution.iri;
    };

    function factoryFunction(jsonldService) {
        return {
            'create': function (jsonldService) {
                return jQuery.extend({
                    'data': {
                        'pipeline': {},
                        'components': {},
                        'dataUnits': {},
                        'metadata': {
                        },
                        'execution': {
                            'status': {
                            },
                            'iri': void 0
                        }
                    },
                    'jsonldService': jsonldService
                }, service);
            }
        };
    }

    return function init(app) {
        app.factory('models.execution', ['services.jsonld', factoryFunction]);
    };

});
