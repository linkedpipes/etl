//
// Translate a view pipeline definition into executable one - should be standalone component.
//

'use strict';

var gHttp = require('http');
var gTemplates = require('./../modules/templates');
var gPipelines = require('./../modules/pipelines');
var gRequest = require('request'); // https://github.com/request/request
var gConfiguration = require('./../modules/configuration');

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

var prepareMetadata = function (data) {
    var metadata = {};
    var graphList;
    if (data['@graph']) {
        graphList = data['@graph'];
    } else {
        graphList = data;
    }
    //
    for (var graphIndex in graphList) {
        var graph = graphList[graphIndex];
        graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Pipeline') !== -1) {
                metadata.definition = {
                    'uri': graph['@id'],
                    'graph': graph
                };
                return;
            }
        });
    }
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
        }
    });
    // Store references.
    for (var key in references) {
        component[key] = references[key];
    }
};

var getReference = function (object, property) {
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

var getReferenceAll = function (object, property) {
    if (!object[property]) {
        return [];
    }
    var value = object[property];
    if (Array.isArray(value)) {
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
        return [getReference(object, property)];
    }
    return [];
};

var getIri = function (object) {
    if (!object['@id']) {
        return;
    }
    var value = object['@id'];
    if (value['@id']) {
        return value['@id'];
    } else if (value[0]['@id']) {
        return value[0]['@id'];
    } else {
        return value['@id'];
    }
};

var getString = function (object, property) {

    if (!object[property]) {
        return;
    }
    var value = object[property];

    /**
     * Return object with @lang and @value.
     */
    var getString = function (value, result) {
        if (typeof value['@value'] === 'undefined') {
            return value;
        } else if (typeof value['@lang'] === 'undefined') {
            return value['@value'];
        } else {
            // Return first value in given language.
            return value['@value'];
        }
    };

    if (Array.isArray(value)) {
        if (value.length === 0) {
            // Skip as empty array does not contains any data.
            return;
        } else if (value.length === 1) {
            return getString(value[0]);
        } else {
            for (var itemIndex in value) {
                return getString(value[itemIndex]);
            }
        }
    } else {
        return getString(value);
    }
};

var iterateGraphs = function (data, callback) {
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

var iterateObjects = function (data, callback) {
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
            for (var object_index in graph['@graph']) {
                callback(graph['@graph'][object_index]);
            }
        } else {
            console.log('Invalid graph detected: ', graph);
        }
    }
};

/**
 * Possible configurations:
 *
 * configuration
 *   .execute_to - Debug to IRI.
  *   .mapping
 *     .execution - URI of execution.
 *     .componets - Object with component to component mapping.
 *
 * @param String uri
 * @param Object configuration
 * @param Function callback Called(sucess, object) in case of sucess object is a TRIG in case of failure an error message.
 */
gModule.unpack = function (uri, configuration, callback) {

    var sequenceExecution = new SequenceExecution();

    sequenceExecution.data = {
        'pipeline': {},
        'metadata': {},
        'templates': {},
        'execution': {// Store execution realated data.
            'components': {} // A mimicked set of component to execute.
        },
        'executions': {} // List of mentioned executions.
    };

    sequenceExecution.add(function (data, next, executor) {
        // Get pipeline definition and all used definitions.
        // This require name resolution and fail without network connection!!
        gRequest(uri, function (error, response, body) {
            // TODO Expand prefixes here!
            data.pipeline = JSON.parse(body);
            // Search for the pipeline graph.
            data.metadata = prepareMetadata(data.pipeline);
            // Download templates and their configurations.
            data.metadata.definition.graph['@graph'].forEach(function (resource) {
                if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
                    downloadTemplate(executor, data,
                            getReference(resource, 'http://linkedpipes.com/ontology/template'));
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
                var template_iri = getReference(resource, 'http://linkedpipes.com/ontology/template');
                expandComponent(pipeline.graph, resource,
                        data.templates[template_iri]);
            }
        });
        next();
    }).add(function (data, next, executor) {
        // If we have some references to other executions in data.execution.mapping we download
        // their debug data.
        if (configuration.mapping) {
            var iri = configuration.mapping.execution;
            var id = iri.substring(iri.indexOf('executions/') + 11);
            getAndNext(executor, iri, function (error, response, body) {
                // TODO Add error handling here!
                data.executions[id] = JSON.parse(body);
            });
            configuration.mapping['id'] = id;
        }
        //
        next();
    }).add(function (data, next) {
        // Here we need to add sources to components based on the connections, for this we need
        // to build list of components's with their ports.

        // ports[PORT_IRI]
        var ports = {};
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Port') > -1) {
                ports[resource['@id']] = resource;
            }
        });

        // portsByOwnerAndBinding[COMPONENT_IRI][BINDING]
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

        // Add sources - ie. for each data unit determine it's sources. The source list
        // are build based on the connections in pipeline.
        try {
            data.metadata.definition.graph['@graph'].forEach(function (resource) {
                if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Connection') > -1) {
                    var source = getReference(resource, 'http://linkedpipes.com/ontology/sourceComponent');
                    var target = getReference(resource, 'http://linkedpipes.com/ontology/targetComponent');
                    var sourcePort = portsByOwnerAndBinding[source][
                            getString(resource, 'http://linkedpipes.com/ontology/sourceBinding')];
                    var targetPort = portsByOwnerAndBinding[target][
                            getString(resource, 'http://linkedpipes.com/ontology/targetBinding')];
                    if (typeof (targetPort) === 'undefined') {
                        throw {
                            'errorMessage': '',
                            'systemMessage': '',
                            'userMessage': "Invalid pipeline definition, cycle detected!",
                            'errorCode': 'ERROR'
                        };
                    }
                    if (!targetPort['http://linkedpipes.com/ontology/source']) {
                        targetPort['http://linkedpipes.com/ontology/source'] = [];
                    }
                    targetPort['http://linkedpipes.com/ontology/source'].push({'@id': sourcePort['@id']});
                }
            });
        } catch (error) {
            callback(false, error);
        }
        next();
    }).add(function (data, next) {
        // For some data units, we need to load resources from directory. This is used
        // when we use debug-from. So what we do here, is that we replace the source property
        // with the source object.

        if (!configuration.mapping) {
            next();
            return;
        }

        // Store for faster access.
        var mapping = configuration.mapping;

        var dataUnits = {};
        var componenetsDataUnit = {};
        // Load information from the execution.
        iterateObjects(data.executions[mapping['id']], function (resource) {
            if (resource['@type'].indexOf('http://etl.linkedpipes.com/ontology/DataUnit') > -1) {
                // The data unit we map could have been mapped from another execution.
                var execution = resource['http://etl.linkedpipes.com/ontology/execution'];
                if (typeof (execution) === 'undefined') {
                    execution = {'@id': mapping.execution};
                }
                //
                dataUnits[resource['@id']] = {
                    'execution': execution,
                    'debug': resource['http://etl.linkedpipes.com/ontology/debug'],
                    'loadPath': resource['http://etl.linkedpipes.com/ontology/dataPath'],
                    'debugPath': resource['http://etl.linkedpipes.com/ontology/debugPath']
                };
                return;
            }
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
                componenetsDataUnit[resource['@id']] = getReferenceAll(resource,
                        'http://etl.linkedpipes.com/ontology/dataUnit');
            }
        });

        var portSources = {};
        for (var sourceUri in mapping['components']) {
            var targetUri = mapping['components'][sourceUri];
            var dataUnitReference = componenetsDataUnit[sourceUri];
            if (typeof (dataUnitReference) === 'undefined') {
                console.log('Component without mapping detected!');
                console.log(sourceUri, '->', targetUri);
                continue;
            }
            for (var index in dataUnitReference) {
                var iri = dataUnitReference[index];
                portSources[iri] = dataUnits[iri];
            }
        }

        var ports = [];
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Port') > -1) {
                ports.push(resource);
            }
        });

        ports.forEach(function (resource) {
            if (portSources[resource['@id']]) {
                // Delete source as a depdency on other data units.
                delete resource['http://linkedpipes.com/ontology/source'];
                //
                var portSource = portSources[resource['@id']];
                // Create source object.
                var sourceObject = {
                    '@id': resource['@id'] + '/source',
                    '@type': ['http://linkedpipes.com/ontology/PortSource'],
                    'http://linkedpipes.com/ontology/execution': portSource['execution'],
                    'http://linkedpipes.com/ontology/debug': portSource['debug'],
                    'http://linkedpipes.com/ontology/loadPath': portSource['loadPath'],
                    'http://linkedpipes.com/ontology/debugPath': portSource['debugPath']
                };
                resource['http://linkedpipes.com/ontology/dataSource'] = {'@id': sourceObject['@id']};
                data.metadata.definition.graph['@graph'].push(sourceObject);
            }
        }
        );
        next();
    }).add(function (data, next) {
        // Here we need to construct the 'http://linkedpipes.com/ontology/executionOrder' for all instances.
        // neighboursList can contains some instances multiple times !
        //
        // As the execution order is not used if component is not connected with the pipeline resource,
        // we can assigne executionOrder to component that would notbe executed - in fact it's
        // desired to assign the value anyway so wa have full information in pipeline and
        // we got the same execution order no mather what (full run, debug from, debug to .. ).

        // neighboursList[COMPONENT] = [REQUIRED, REQUIRED, ... ]
        var neighboursList = {};
        var components = {};
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
                neighboursList[resource['@id']] = [];
                components[resource['@id']] = resource;
            }
        });
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Connection') > -1 ||
                    resource['@type'].indexOf('http://linkedpipes.com/ontology/RunAfter') > -1) {
                var source = getReference(resource, 'http://linkedpipes.com/ontology/sourceComponent');
                var target = getReference(resource, 'http://linkedpipes.com/ontology/targetComponent');
                // Check for duplicity.
                if (neighboursList[target].indexOf(source) === -1) {
                    neighboursList[target].push(source);
                }
            }
        });
        // Construct list of component to execute, in case of debug-to we need to restrict this list
        // to only some components.
        if (configuration && configuration.execute_to) {
            var toExecute = [configuration.execute_to]; // List of components to execute.
            // Add transitive dependencies.
            var toAdd = neighboursList[configuration.execute_to].slice(0);
            while (toAdd.length > 0) {
                var toAddNew = [];
                // Add all from toAdd to the toExecute.
                toExecute = toExecute.concat(toAdd);
                // Scan for dependencies of toAdd and add them to toAddNew.
                for (var index in toAdd) {
                    neighboursList[toAdd[index]].forEach(function (item) {
                        if (toExecute.indexOf(item) === -1) {
                            toAddNew.push(item);
                        }
                    });
                }
                //
                toAdd = toAddNew;
            }
            //
            data.execution.components = {};
            for (var index in toExecute) {
                data.execution.components[toExecute[index]] = true;
            }
        } else {
            // Set all components for execution.
            data.execution.components = {};
            for (var key in components) {
                data.execution.components[key] = true;
            }
        }
        // Construct executionOrder, this consumes and destroy neighboursList.
        // We look for component without a dependency, then we remove them and repeat the process.
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
    }).add(function (data, next) {
        // For debug we need a URI fragment for each data unit - this values is used
        // to determine the DataUnit ID.
        var portCounter = 1;
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Port') > -1) {
                var uriFragment = '00000' + portCounter;
                portCounter += 1;
                uriFragment = uriFragment.substr(uriFragment.length - 5);
                resource['http://linkedpipes.com/ontology/uriFragment'] = uriFragment;
            }
        });
        next();
    }).add(function (data, next) {
        // Add references from pipeline to all components and also set component execution type.
        var pipelineResource = {};
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Pipeline') > -1) {
                pipelineResource = resource;
            }
        });

        // If list of components to execute is given then use given list, else scan for all components.
        var components = {};
        var componentsReferences = [];
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') > -1) {
                // Store for later use.
                components[resource['@id']] = resource;
                // Add reference from pipeline.
                componentsReferences.push({'@id': resource['@id']});
            }
        });
        pipelineResource['http://linkedpipes.com/ontology/component'] = componentsReferences;

        // Create list of mapped components.
        var mappedComponents = {};
        if (configuration.mapping) {
            for (var key in configuration.mapping['components']) {
                mappedComponents[key] = true;
            }
        }

        // Set component execution types.
        //
        // data.execution.components
        for (var componentUri in components) {
            var component = components[componentUri];
            if (!data.execution.components[componentUri]) {
                // Do not execute.
                component['http://linkedpipes.com/ontology/executionType'] =
                        'http://linkedpipes.com/resources/execution/type/skip';
            } else {
                if (mappedComponents[componentUri]) {
                    // Mapped component.
                    component['http://linkedpipes.com/ontology/executionType'] =
                            'http://linkedpipes.com/resources/execution/type/mapped';
                } else {
                    // Execute.
                    component['http://linkedpipes.com/ontology/executionType'] =
                            'http://linkedpipes.com/resources/execution/type/execute';
                }
            }
        }
        next();
    }).add(function (data, next) {
        // Construct and add metadata about the execution.

        // Search for pipeline resource.
        var pipelineResource = {};
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Pipeline') > -1) {
                pipelineResource = resource;
            }
        });

        // Construct resource with information about the execution.
        var id = pipelineResource['@id'] + '/executionInfo';
        var executionInfo = {
            '@id' : id,
            '@type': [
                'http://linkedpipes.com/ontology/ExecutionMetadata'
            ]
        };
        var executionType = void 0;
        // Execution type.
        if (configuration.execute_to === undefined) {
            if (configuration.mapping === undefined) {
                executionType = 'http://linkedpipes.com/resources/executionType/Full';
            } else {
                executionType = 'http://linkedpipes.com/resources/executionType/DebugFrom';
            }
        } else {
            executionInfo['http://linkedpipes.com/ontology/execution/targetComponent'] = {
                '@id': configuration.execute_to
            };
            if (configuration.mapping === undefined) {
                executionType = 'http://linkedpipes.com/resources/executionType/DebugTo';
            } else {
                executionType = 'http://linkedpipes.com/resources/executionType/DebugFromTo';
            }
        }
        executionInfo['http://linkedpipes.com/ontology/execution/type'] = executionType;

        // Add to the graph.
        pipelineResource['http://linkedpipes.com/ontology/executionMetadata'] = {
            '@id' : id
        };
        data.metadata.definition.graph['@graph'].push(executionInfo);

        next();
    }).add(function (data) {
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

        var pipelineResource = {};
        data.metadata.definition.graph['@graph'].forEach(function (resource) {
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Pipeline') > -1) {
                pipelineResource = resource;
            }
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