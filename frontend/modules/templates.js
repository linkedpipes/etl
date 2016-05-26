//
// Manages templates - shoudl be standalone component.
//

'use strict';

var gFs = require('fs');
var gConfiguration = require('./configuration');

var gModule = {
    'list': [],
    'data': {},
    // For each pipeline IRI contains dictionary of followup.
    'pipelines': {}
};

module.exports = gModule;

var constructUri = function (name) {
    return gConfiguration.storage.domain + '/resources/components/' + name;
};

var addComponent = function (name, path) {
    var dataItem = {
        // Contains JSON ld configuration.
        'definition': '',
        // Path to the JAR file.
        'jar': '',
        // Path to the configuration.
        'configuration': '',
        // A shortcut to the Component resource.
        'resource': {}
    };
    var pathDefinition;
    // Scan directory for files.
    gFs.readdirSync(path).forEach(function (file) {
        if (file.indexOf('.jar') > 0) {
            dataItem['jar'] = path + file;
        } else if (file === 'configuration.jsonld') {
            dataItem['configuration'] = path + file;
        } else if (file === 'definition.jsonld') {
            pathDefinition = path + file;
        } else if (file === 'dialog.js') {
            if (!dataItem['dialog']) {
                dataItem['dialog'] = {};
            }
            dataItem['dialog']['js'] = path + file;
        } else if (file === 'dialog.html') {
            if (!dataItem['dialog']) {
                dataItem['dialog'] = {};
            }
            dataItem['dialog']['html'] = path + file;
        }
    });

    // Parse the definition file.
    var definition;
    try {
        definition = JSON.parse(String(gFs.readFileSync(pathDefinition)));
    } catch (e) {
        console.log("Can't load definition for:", name, "from:", pathDefinition);
        throw e;
    }
    dataItem['definition'] = definition;

    // Scan entities.
    var dialogItem;
    definition['@graph'].forEach(function (item) {
        var resource = item;
        var types = [].concat(resource['@type']);
        if (types.indexOf('http://linkedpipes.com/ontology/Component') > -1) {
            // Add jar file.
            item['http://linkedpipes.com/ontology/jarUri'] = gConfiguration.storage.jarPathPrefix + dataItem['jar'];
            var configurationUri = gConfiguration.storage.domain + '/resources/components/' + name + '/configuration';
            item['http://linkedpipes.com/ontology/configurationGraph'] = configurationUri;
            // Construct item list.
            item['http://linkedpipes.com/resources/component'] = resource['@id'];
            resource['@id'] = constructUri(name);
            definition['@id'] = resource['@id'] + '/graph';
            // Add reference to the dialog.
            if (dataItem['dialog']) {
                dialogItem = {
                    '@id': resource['@id'] + '/dialog',
                    '@type': [
                        'http://linkedpipes.com/ontology/Dialog'
                    ],
                    'http://linkedpipes.com/ontology/js': {
                        '@id': resource['@id'] + '/dialog.js'
                    },
                    'http://linkedpipes.com/ontology/html': {
                        '@id': resource['@id'] + '/dialog.html'
                    }
                };
                item['http://linkedpipes.com/ontology/dialog'] = [{
                        '@id': dialogItem['@id']
                    }];
            }
            dataItem['resource'] = resource;
        }
    });
    if (dialogItem) {
        definition['@graph'].push(dialogItem);
    }
    // Add to lists.
    gModule.list.push(dataItem.definition);
    gModule.data[name] = dataItem;
};

var updatePipeline = function (pipelineObject, pipelineUri) {
    for (var graphIndex in pipelineObject['@graph']) {
        var graph = pipelineObject['@graph'][graphIndex];
        if (graph['@id'] !== pipelineUri) {
            continue;
        }
        // Parse definition graph.
        var components = {};
        var connection = [];
        graph['@graph'].forEach(function (resource) {
            if (!resource['@id']) {
                return;
            }
            if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') !== -1) {
                components[resource['@id']] = resource['http://linkedpipes.com/ontology/template']['@id'];
            } else if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Connection') !== -1) {
                connection.push({
                    'source': resource['http://linkedpipes.com/ontology/sourceComponent']['@id'],
                    'target': resource['http://linkedpipes.com/ontology/targetComponent']['@id']
                });
            }
        });
        // Create pipeline record.
        var pipelineRecord = {};
        for (var index in connection) {
            var source = connection[index]['source'];
            var target = connection[index]['target'];
            //
            var sourceUri = components[source];
            var targetUri = components[target];
            //
            if (!pipelineRecord[sourceUri]) {
                pipelineRecord[sourceUri] = {};
            }
            if (!pipelineRecord[sourceUri][targetUri]) {
                pipelineRecord[sourceUri][targetUri] = 1;
            } else {
                pipelineRecord[sourceUri][targetUri] += 1;
            }
        }
        //
        gModule.pipelines[pipelineUri] = pipelineRecord;
    }
};

/**
 * From gModule.pipelines rebuild gModule.followup.
 */
var rebuilFollowUp = function () {
    var followup = {};
    for (var key in gModule.pipelines) {
        var pipeline = gModule.pipelines[key];
        for (var sourceUri in pipeline) {
            if (followup[sourceUri]) {
                // Merge followup list of two components.
                for (var targetKey in pipeline[sourceUri]) {
                    if (!followup[sourceUri][targetKey]) {
                        followup[sourceUri][targetKey] = 0;
                    }
                    followup[sourceUri][targetKey] += pipeline[sourceUri][targetKey];
                }
            } else {
                if (pipeline[sourceUri]) {
                    followup[sourceUri] = pipeline[sourceUri];
                } else {
                    followup[sourceUri] = {};
                }
            }
        }
    }
    //
    for (var name in gModule.data) {
        var item = gModule.data[name];
        var resource = item['resource'];
        var references = [];
        if (followup[resource['@id']]) {
            var followupList = followup[resource['@id']];
            for (var iri in followupList) {
                var number = followupList[iri];
                var object = {
                    '@id': iri + '/reference',
                    '@type': ['http://linkedpipes.com/ontology/Refrence'],
                    'http://linkedpipes.com/ontology/reference': iri,
                    'http://linkedpipes.com/ontology/followUpCount': number
                };
                item['definition']['@graph'].push(object);
                references.push({'@id': iri + '/reference'});
            }
            resource['http://linkedpipes.com/ontology/followup'] = references;
        }
    }
};

(function initialize() {
    console.time('templates.initialize');
    var componentDirectory = gConfiguration.storage.components;
    // We use sych call here as we want this to be processed before
    // the code will continue.
    var componentFiles = gFs.readdirSync(componentDirectory);
    componentFiles.forEach(function (directory) {
        var path = componentDirectory + '/' + directory + '/';
        addComponent(directory, path);
    });
    // Scan pipelines.
    var pipelineDirectory = gConfiguration.storage.pipelines;
    var pipelineFiles = gFs.readdirSync(pipelineDirectory);
    pipelineFiles.forEach(function (fileName) {
        var path = pipelineDirectory + '/' + fileName;
        try {
            var pipeline = JSON.parse(gFs.readFileSync(path));
        } catch (err) {
            console.error('Can not read pipeline from: ', path);
            console.info('Exception:' , err.message, err.stack);
            return;
        }
        var pipelineIri =
                gConfiguration.storage.domain +
                '/resources/pipelines/' +
                fileName.substring(0, fileName.indexOf('.json'));
        updatePipeline(pipeline, pipelineIri);
    });
    // Build follow up index.
    rebuilFollowUp();
    console.timeEnd('templates.initialize');
})();

/**
 * Update definition based on chanegs in a pipeline.
 */
gModule.onPipelineUpdate = function (pipelineObject, pipelineIri) {
    updatePipeline(pipelineObject, pipelineIri);
    rebuilFollowUp();
};

gModule.onPipelineDelete = function (pipelineIri) {
    delete gModule.pipelines[pipelineIri];
    rebuilFollowUp();
};


gModule.getList = function () {
    return this.list;
};

gModule.getDefinition = function (name) {
    if (!gModule.data[name]) {
        return;
    }
    return gModule.data[name].definition;
};

gModule.getConfigurationString = function (name) {
    if (!gModule.data[name]) {
        return;
    }
    var path = gModule.data[name].configuration;
    if (!gFs.existsSync(path)) {
        return;
    }
    var fileContent = gFs.readFileSync(path);
    return String(fileContent);
};

gModule.getDialogJs = function (name) {
    if (!gModule.data[name] || !gModule.data[name].dialog) {
        return;
    }
    var path = gModule.data[name].dialog.js;
    if (!gFs.existsSync(path)) {
        return;
    }
    var fileContent = gFs.readFileSync(path);
    return String(fileContent);
};

gModule.getDialogHtml = function (name) {
    if (!gModule.data[name] || !gModule.data[name].dialog) {
        return;
    }
    var path = gModule.data[name].dialog.html;
    if (!gFs.existsSync(path)) {
        return;
    }
    var fileContent = gFs.readFileSync(path);
    return String(fileContent);
};
