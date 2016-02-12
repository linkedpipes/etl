//
// Translate a view pipeline definition into executable one - should be standalone component.
//

'use strict';

var gHttp = require('http');
var gTemplates = require('./../modules/templates');
var gPipelines = require('./../modules/pipelines');
var gRequest = require('request'); // https://github.com/request/request

var gModule = {};

module.exports = gModule;

// Enable sequence call of given functions.
var SequenceExecution = function () {
    return {
        'list': [],
        'index': 0,
        'data': {},
        'add': function (action) {
            this.list.push(action);
            return this;
        },
        'insert': function (action) {
            // Add action after current action.
            this.list.splice(this.index, 0, action);
        },
        'next': function () {
            this.list[this.index++](this.data, this.next.bind(this), this);
        },
        'execute': function () {
            this.next();
        }
    };
};

//
//
//

/**
 * Queue get ot given URI and its processing after current method in the executor list.
 *
 * @param executor
 * @param uri URI to get.
 * @param callback Called after GET on given data.
 * @param args Oprional parameter that can be used to pass aditional arguments.
 */
var getAndNext = function (executor, uri, callback, args) {
    executor.insert(function (data, next, executor) {
        // Download, call callback and process.
        gRequest(uri, function (error, response, body) {
            callback(error, response, body, args);
            next();
        });
    });
};

var prepareMetadata = function (pipeline) {
    var metadata = {};

    pipeline['@graph'].forEach(function (graph) {
        // Search for pipeline definition.
        if (!metadata.definition) {
            graph['@graph'].forEach(function (resource) {
                if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Pipeline') !== -1) {
                    metadata.definition = {
                        'uri': graph['@id'],
                        'graph': graph
                    };
                }
            });
        }
    });

    return metadata;
};

/**
 *
 *
 * @param executor Executor instances used to queue the task.
 * @param data Data context.
 * @param uri Template URIs.
 */
var downloadTemplate = function (executor, data, uri) {
    if (data.templates[uri]) {
        return;
    }
    getAndNext(executor, uri, function (error, response, body) {
        var definition = JSON.parse(body);
        // TODO Expand prefixes here!
        data.templates[uri] = definition;
        // Search for configuration.
        for (var index in definition['@graph']) {
            var resource = definition['@graph'][index];
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
                var configurationUri = resource['http://linkedpipes.com/ontology/configurationGraph'];
                if (configurationUri) {
                    // Download configuration - we pass the template property as argument.
                    getAndNext(executor, configurationUri, function (error, response, body) {
                        var configuration = JSON.parse(body);
                        // Add ID to the graph.
                        configuration['@id'] = configurationUri;
                        data.pipeline['@graph'].push(configuration);
                    });
                }
            }
        }
    });
};

/**
 * Merge given component with informations from template. Also add instances for related resources (ports).
 * Also takes care about configuration expansion.
 *
 *
 * @param pipeline Pipeline graph.
 * @param component Component to expand.
 * @param template Component's tamplate graph.
 */
var expandComponent = function (pipeline, component, template) {
    var uri = component['@id'];
    var portCounter = 1;
    var configurationCounter = 1;
    var references = {
        'http://linkedpipes.com/ontology/port': [],
        'http://linkedpipes.com/ontology/configuration': []
    };
    // Add first configuration reference.
    if (component['http://linkedpipes.com/ontology/configurationGraph']) {
        var configuration = {
            '@id': uri + '/configuration/' + configurationCounter,
            '@type': ['http://linkedpipes.com/ontology/Configuration'],
            'http://linkedpipes.com/ontology/configuration/order': configurationCounter,
            'http://linkedpipes.com/ontology/configuration/graph':
                    component['http://linkedpipes.com/ontology/configurationGraph']
        };
        configurationCounter += 1;
        references['http://linkedpipes.com/ontology/configuration'].push({'@id': configuration['@id']});
        pipeline['@graph'].push(configuration);
    }
    template['@graph'].forEach(function (resource) {
        if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
            for (var predicate in resource) {
                if (predicate === 'http://linkedpipes.com/ontology/port') {
                    // We add port via their classes.
                    continue;
                }
                if (predicate === 'http://linkedpipes.com/ontology/configurationGraph') {
                    var configuration = {
                        '@id': uri + '/configuration/' + configurationCounter,
                        '@type': ['http://linkedpipes.com/ontology/Configuration'],
                        'http://linkedpipes.com/ontology/configuration/order': configurationCounter,
                        'http://linkedpipes.com/ontology/configuration/graph':
                                resource['http://linkedpipes.com/ontology/configurationGraph']
                    };
                    configurationCounter += 1;
                    references['http://linkedpipes.com/ontology/configuration'].push({'@id': configuration['@id']});
                    pipeline['@graph'].push(configuration);
                    continue;
                }
                // Only add values from teplate to a component.
                if (!component[predicate]) {
                    component[predicate] = resource[predicate];
                }
            }
        } else if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Port') > -1) {
            var portObject = {};
            // Create shallow copy as we need to modify only firtst level content.
            for (var key in resource) {
                portObject[key] = resource[key];
            }
            portObject['@id'] = uri + '/port/' + portCounter;
            references['http://linkedpipes.com/ontology/port'].push({'@id': portObject['@id']});
            portCounter += 1;
            // Add port to the pipeline definition.
            pipeline['@graph'].push(portObject);
        } else {
            // Unknown object.
            console.log('Unknown object type record ignored:', resource);
        }
    });
    // Store references.
    for (var key in references) {
        component[key] = references[key];
    }
};

/**
 *
 * @param String uri
 * @param Function callback Called(sucess, object) in case of sucess object is a TRIG in case of failure an error message.
 */
gModule.unpack = function (uri, callback) {

    var sequenceExecution = new SequenceExecution();

    sequenceExecution.data = {
        'pipeline': {},
        'metadata': {},
        'templates': {}
    };

    sequenceExecution.add(function (data, next, executor) {
        // Get definition and parse it.
        // This in fact require name resolution and fail without network connection!!
        gRequest(uri, function (error, response, body) {
            // TODO Expand prefixes here!
            data.pipeline = JSON.parse(body);
            // Search for the pipeline graph.
            data.metadata = prepareMetadata(data.pipeline);
            // Download templates and their configurations.
            data.metadata.definition.graph['@graph'].forEach(function (resource) {
                if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
                    downloadTemplate(executor, data, resource['http://linkedpipes.com/ontology/template']['@id']);
                }
            });
            next();
        });
    }).add(function (data, next) {
        // Now we have downloaded all components mentioned on pipeline so we can begin transformation - in
        // this step we expand components to templates.
        var pipeline = data.metadata.definition;
        pipeline.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
                expandComponent(pipeline.graph, resource,
                        data.templates[resource['http://linkedpipes.com/ontology/template']['@id']]);
            }
        });
        next();
    }).add(function (data, next) {
        // Here we need to add sources to components based on the connections, for this we need
        // to build list of components's with their ports.
        var ports = {};
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Port') > -1) {
                ports[resource['@id']] = resource;
            }
        });
        var portsByOwnerAndBinding = {};
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
                if (!resource['http://linkedpipes.com/ontology/port']) {
                    return;
                }
                var resourcePorts = {};
                resource['http://linkedpipes.com/ontology/port'].forEach(function (portReference) {
                    var port = ports[portReference['@id']];
                    resourcePorts[port['http://linkedpipes.com/ontology/binding']] = port;
                });
                portsByOwnerAndBinding[resource['@id']] = resourcePorts;
            }
        });
        // Add sources.
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Connection') > -1) {
                var source = resource['http://linkedpipes.com/ontology/sourceComponent']['@id'];
                var target = resource['http://linkedpipes.com/ontology/targetComponent']['@id'];
                var sourcePort = portsByOwnerAndBinding[source][resource['http://linkedpipes.com/ontology/sourceBinding']];
                var targetPort = portsByOwnerAndBinding[target][resource['http://linkedpipes.com/ontology/targetBinding']];
                if (!targetPort['http://linkedpipes.com/ontology/source']) {
                    targetPort['http://linkedpipes.com/ontology/source'] = [];
                }
                targetPort['http://linkedpipes.com/ontology/source'].push({'@id': sourcePort['@id']});
            }
        });
        next();
    }).add(function (data, next) {
        // Here we need to construct the 'http://linkedpipes.com/ontology/executionOrder' for all instances.
        // neighboursList can contains some instances multiple times !
        var neighboursList = {};
        var components = {};
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
                neighboursList[resource['@id']] = [];
                components[resource['@id']] = resource;
            }
            ;
        });

        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Connection') > -1 ||
                    resource['@type'].indexOf('http://linkedpipes.com/ontology/RunAfter') > -1) {
                var source = resource['http://linkedpipes.com/ontology/sourceComponent']['@id'];
                var target = resource['http://linkedpipes.com/ontology/targetComponent']['@id'];
                // Check for duplicity.
                if (neighboursList[target].indexOf(source) === -1) {
                    neighboursList[target].push(source);
                }
            }
        });
        //
        var executionOrder = 0;
        while (true) {
            var removed = [];
            // Search for nodes with no dependency.
            for (var key in neighboursList) {
                if (neighboursList[key].length === 0) {
                    components[key]['http://linkedpipes.com/ontology/executionOrder'] = executionOrder;
                    executionOrder += 1;
                    removed.push(key);
                }
            }
            // Remove.
            removed.forEach(function (item) {
                delete neighboursList[item];
            });
            for (var key in neighboursList) {
                removed.forEach(function (item) {
                    var index = neighboursList[key].indexOf(item);
                    if (index !== -1) {
                        neighboursList[key].splice(index, 1);
                    }
                });
            }
            ;
            //
            if (Object.keys(neighboursList).length === 0) {
                break;
            } else {
                if (removed.length === 0) {
                    callback(false, {
                        'exception': {
                            'errorMessage': '',
                            'systemMessage': '',
                            'userMessage': "Invalid pipeline definition, cycle detected!",
                            'errorCode': 'ERROR'
                        }
                    });
                    return;
                }
            }
        }
        next();
    }).add(function (data) {
        // Add other required classes and properties.

        // URI fragments for data units - this value is appended to the execution URI
        // and used to access debug data.
        var portCounter = 1;
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Port') > -1) {
                var uriFragment = '00000' + portCounter;
                portCounter += 1;
                uriFragment = uriFragment.substr(uriFragment.length - 5);
                resource['http://linkedpipes.com/ontology/uriFragment'] = uriFragment;
            }
        });

        // References from pipeline to tempaltes.
        var pipelineResource = {};
        var componentsReferences = [];
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
                componentsReferences.push({'@id': resource['@id']});
            } else if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Pipeline') > -1) {
                pipelineResource = resource;
            }
        });
        pipelineResource['http://linkedpipes.com/ontology/component'] = componentsReferences;

        // Add some hard coded objects (hopefully just for now).

        data.metadata.definition.graph['@graph'].push({
            '@id': 'http://linkedpipes.com/resources/requirement/workingDirectory',
            '@type': [
                'http://linkedpipes.com/ontology/requirements/Requirement',
                'http://linkedpipes.com/ontology/requirements/TempDirectory'
            ],
            'http://linkedpipes.com/ontology/requirements/target': 'http://linkedpipes.com/ontology/workingDirectory'
        });

        data.metadata.definition.graph['@graph'].push({
            '@id': 'http://linkedpipes.com/resources/requirement/debug',
            '@type': [
                'http://linkedpipes.com/ontology/requirements/Requirement',
                'http://linkedpipes.com/ontology/requirements/TempDirectory'
            ],
            'http://linkedpipes.com/ontology/requirements/target': 'http://linkedpipes.com/ontology/debugDirectory'
        });

        data.metadata.definition.graph['@graph'].push({
            '@id': 'http://localhost/repository/sesame',
            '@type': [
                'http://linkedpipes.com/ontology/Repository',
                'http://linkedpipes.com/ontology/dataUnit/sesame/1.0/Repository'
            ],
            'http://linkedpipes.com/ontology/requirement': {'@id': 'http://linkedpipes.com/resources/requirement/workingDirectory'}
        });

        pipelineResource['http://linkedpipes.com/ontology/repository'] = {'@id': 'http://localhost/repository/sesame'};

        // Add debug directory to all Ports.
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Port') > -1) {
                if (!resource['http://linkedpipes.com/ontology/requirement']) {
                    resource['http://linkedpipes.com/ontology/requirement'] = [];
                }
                resource['http://linkedpipes.com/ontology/requirement'].push({'@id': 'http://linkedpipes.com/resources/requirement/debug'});
            }
        });


        // Return JSON representaio.
        callback(true, data.pipeline);
    }).execute();
};