//
// Manage pipeline storage - should be standalone component.
//

'use strict';

var gFs = require('fs');
var gUuid = require('node-uuid');
var gConfiguration = require('./configuration');
var gTemplates = require('./../modules/templates');
var gRequest = require('request'); // https://github.com/request/request

var gModule = {
    'list': [],
    'map': {}
};
module.exports = gModule;

/**
 *
 * @param definition
 * @return String as a pipeline label.
 */
var getPipelineLabel = function (id, definition) {

    var findLabels = function (resource) {
        if (!resource['@type']) {
            return;
        }
        var type = resource['@type'];
        if (type.indexOf('http://linkedpipes.com/ontology/Pipeline') !== -1) {
            var value = resource['http://www.w3.org/2004/02/skos/core#prefLabel'];
            if (!value) {
                // Pipeline does not have a name.
                return resource['@id'];
            }
            // Label can be save as a direct value or in a array
            // under the "@value" element.
            if (value[0]['@value']) {
                return value[0]['@value'];
            } else {
                return value;
            }
        }
    };

    var graphList;
    if (definition['@graph']) {
        graphList = definition['@graph'];
    } else {
        graphList = definition;
    }

    var label;
    for (var graphIndex in graphList) {
        var graph = graphList[graphIndex];
        if (graph['@graph']) {
            // Search for pipeline object and label.
            graph['@graph'].forEach(function (resource) {
                if (!label) {
                    label = findLabels(resource);
                }
            });
        }
        // Break if we found the label.
        if (label) {
            break;
        }
    }

    if (label) {
        return label;
    } else {
        return id;
    }
};

/**
 * Insert record about pipeline with given id.
 *
 * @param String id
 * @param Object definition Optional pipeline definition object.
 */
var insertPipeline = function (id, definition) {
    var definitionFile = gConfiguration.storage.pipelines + '/' + id + '.json';
    if (!definition) {
        definition = JSON.parse(gFs.readFileSync(definitionFile));
    }
    gModule.list.push({
        'uri': gConfiguration.storage.domain + '/resources/pipelines/' + id,
        'id': id,
        'label': getPipelineLabel(id, definition)
    });
    gModule.map[id] = {
        'definitionFile': definitionFile
    };
};

(function initialize() {
    console.time('pipelines.initialize');
    // Secure existance.
    var storageDirectory = gConfiguration.storage.pipelines;
    if (!gFs.existsSync(storageDirectory)) {
        gFs.mkdirSync(storageDirectory);
    }
    var files = gFs.readdirSync(storageDirectory);
    for (var index in files) {
        var file = files[index];
        var id = file.substring(0, file.indexOf('.json'));
        insertPipeline(id);
    }
    console.timeEnd('pipelines.initialize');
})();


/**
 *
 * @return Array of stored objects, that contains pipeline URI and ID.
 */
gModule.getList = function () {
    return this.list;
};

/**
 *
 * @param id Pipeline ID.
 * @return Pipeline definition as stored on HDD.
 */
gModule.getDefinition = function (id) {
    if (!this.map[id]) {
        return;
    }
    var fileContent = gFs.readFileSync(this.map[id].definitionFile);
    return JSON.parse(fileContent);
};

/**
 * Same as getDefinition, just return stream instead of JSON object.
 *
 * @param Pipeline ID.
 * @return Stream with pipeline definition file.
 */
gModule.getDefinitionStream = function (id) {
    if (!this.map[id]) {
        return;
    }
    var fileName = this.map[id].definitionFile;
    return gFs.createReadStream(fileName);
};

/**
 * Delete pipeline with given ID.
 *
 * @param id Pipeline ID.
 */
gModule.delete = function (id) {
    if (!gModule.map[id]) {
        return;
    }
    // Remove from lists.
    for (var i = 0; i < this.list.length; ++i) {
        if (this.list[i].id === id) {
            this.list.splice(i, 1);
            break;
        }
    }
    var record = this.map[id];
    delete this.map[id];
    // Delete file from disk.
    gFs.unlinkSync(record.definitionFile);
    // Update preferences.
    var pipelineIri = gConfiguration.storage.domain +
            '/resources/pipelines/' + id;
    gTemplates.onPipelineDelete(pipelineIri);
};

/**
 * Create an empty pipeline with given ID.
 *
 * @param id Pipeline ID.
 * @return A record for new pipeline as an item from getList.
 */
gModule.create = function (id) {
    if (id) {
        if (gModule.map[id]) {
            // ID colission.
            return;
        }
    } else {
        // ID not provided.
        id = gUuid.v1();
    }
    // Write the pipeline definition.
    var fileName = gConfiguration.storage.pipelines + '/' + id + '.json';
    var uri = gConfiguration.storage.domain + '/resources/pipelines/' + id;
    var newPipeline = {
        '@graph': [
            {
                '@id': uri,
                '@graph': [
                    {
                        '@id': uri,
                        '@type': [
                            'http://linkedpipes.com/ontology/Pipeline'
                        ]
                    }
                ]
            }
        ]
    };
    gFs.writeFile(fileName, JSON.stringify(newPipeline, null, 2));
    // Store into list and return reference.
    insertPipeline(id, newPipeline);
    return {
        'id': id,
        'uri': uri
    };
};

/**
 * Import existing pipeline.
 *
 * TODO Add more security checks.
 *
 * @param id
 * @param pipeline URI of pipeline to import.
 * @param callback to call with result as a parameter.
 */
gModule.import = function (id, pipeline, callback) {
    if (id) {
        if (gModule.map[id]) {
            // ID colission.
            return;
        }
    } else {
        // ID not provided.
        id = gUuid.v1();
    }
    //
    var fileName = gConfiguration.storage.pipelines + '/' + id + '.json';
    var uri = gConfiguration.storage.domain + '/resources/pipelines/' + id;
    // Download pipeline.
    gRequest(pipeline, function (error, response, body) {
        if (!error && response.statusCode >= 200 && response.statusCode < 300) {
            var pipelneObject = JSON.parse(body);
            try {
                updateResourceUri(pipelneObject, id);
            } catch (Exception) {
                callback();
                return;
            }
            gFs.writeFile(fileName, JSON.stringify(pipelneObject, null, 2), function (error) {
                if (error) {
                    console.log(error);
                    callback();
                    return;
                }
                // Sucess.
                insertPipeline(id, pipelneObject);
                callback({
                    'id': id,
                    'uri': uri
                });
            });
        } else {
            console.log('import pipeline:', pipeline);
            console.log(error);
            console.log(body);
            callback();
        }
    });
};

/**
 * Update URIs in given content to match this server.
 *
 * @param content
 * @param id Pipeline ID, used to create new pipeline URI.
 */
var updateResourceUri = function (content, id) {

    var getValue = function(resource, property) {
        var value = resource[property];
        if (Array.isArray(value)) {
            return value[0];
        } else {
            return value;
        }
    };

    var targetDomain = gConfiguration.storage.domain + '/resources/pipelines/' + id;
    targetDomain = targetDomain.replace("[^:]//", "/");
    // Storage of objects that we wan't to change @id to.
    var objects = [];
    var pipelineObject;

    // We search for all objects that we need to update.
    var findObjects = function (resource) {
        if (!resource['@type']) {
            return;
        }
        var type = resource['@type'];
        if (type.indexOf('http://linkedpipes.com/ontology/Pipeline') !== -1) {
            pipelineObject = resource;
            // Pipeine does not have any references by default.
        } else if (type.indexOf('http://linkedpipes.com/ontology/Component') !== -1) {
            objects.push(resource);
            if (resource['http://linkedpipes.com/ontology/configurationGraph']) {
                objects.push(resource['http://linkedpipes.com/ontology/configurationGraph']);
            }
            if (resource['http://linkedpipes.com/ontology/template']) {
                // Update URI - for now in a simple way by string replace
                var componentUri = getValue(resource, 'http://linkedpipes.com/ontology/template')['@id'];
                var componentId = componentUri.substring(componentUri.lastIndexOf("/") + 1);
                var newComponentUri = gConfiguration.storage.domain + '/resources/components/' + componentId;
                newComponentUri = newComponentUri.replace("[^:]//", "/");
                resource['http://linkedpipes.com/ontology/template']['@id'] = newComponentUri;
            }
        } else if (type.indexOf('http://linkedpipes.com/ontology/Connection') !== -1 ||
                type.indexOf('http://linkedpipes.com/ontology/RunAfter') !== -1) {
            objects.push(resource);
            // Reference to source and target.
            objects.push(resource['http://linkedpipes.com/ontology/sourceComponent']);
            objects.push(resource['http://linkedpipes.com/ontology/targetComponent']);
            // Vertices are optional.
            if (resource['http://linkedpipes.com/ontology/vertex']) {
                resource['http://linkedpipes.com/ontology/vertex'].forEach(function (reference) {
                    objects.push(reference);
                });
            }
        } else if (type.indexOf('http://linkedpipes.com/ontology/Vertex') !== -1) {
            objects.push(resource);
            // Vertex has no references.
        } else {
            // We do not update any other objects.
        }
    };

    var graphList;
    if (content['@graph']) {
        graphList = content['@graph'];
    } else {
        graphList = content;
    }

    graphList.forEach(function (graph) {
        objects.push(graph);
        // Search for objects.
        if (graph['@graph']) {
            graph['@graph'].forEach(function (resource) {
                findObjects(resource);
            });
        }
    });

    // Perform replace.

    var sourceDomain = pipelineObject['@id'];
    var sourceDomainLength = sourceDomain.length;

    pipelineObject['@id'] = targetDomain;
    objects.forEach(function (object) {
        if (Array.isArray(object)) {
            object[0]['@id'] = targetDomain + object[0]['@id'].substring(sourceDomainLength);
        } else {
            object['@id'] = targetDomain + object['@id'].substring(sourceDomainLength);
        }
    });
};

/**
 * Update pipeline definition file.
 *
 * @param id Pipeline ID.
 * @param content Pipeline definition as will be stored into a file.
 * @param updateUri If true then iterate ove URIs and update them to fit the local name.
 * @return False if pipeline of given ID does not exists.
 */
gModule.update = function (id, content, updateUri) {
    console.time('pipelines.update');
    // This can be also use for create!
    var fileName = gConfiguration.storage.pipelines + '/' + id + '.json';
    if (!gModule.map[id]) {
        return false;
    }
    if (updateUri) {
        updateResourceUri(content, id);
    }
    gFs.writeFile(fileName, JSON.stringify(content, null, 2));
    // Update definition ID.
    for (var index in gModule.list) {
        var item = gModule.list[index];
        if (item.id === id) {
            item.label = getPipelineLabel(id, content);
            break;
        }
    }
    // Update preferences.
    var pipelineIri = gConfiguration.storage.domain +
            '/resources/pipelines/' + id;
    gTemplates.onPipelineUpdate(content, pipelineIri);
    console.timeEnd('pipelines.update');
    return true;
};