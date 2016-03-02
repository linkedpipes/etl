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
        'jar': '',
        'definition': '',
        'configuration': ''
    };
    var listItem = {
        'inputs': [],
        'outputs': [],
        'keyword': [],
        'followup': {},
        'type': ''
    };
    var pathDefinition;
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

    // Create dialog record.
    if (dataItem['dialog']) {
        listItem['dialog'] = {
            'js': gConfiguration.storage.domain + '/resources/components/' + name + '/dialog.js',
            'html': gConfiguration.storage.domain + '/resources/components/' + name + '/dialog.html'
        };
    }

    var definition;
    try {
        definition = JSON.parse(String(gFs.readFileSync(pathDefinition)));
    } catch (e) {
        console.log("Can't load definition for:", name, "from:", pathDefinition);
        throw e;
    }
    dataItem['definition'] = definition;

    // We required JSON-LD to be in expanded form!

    var ports = [];
    var portsDefinition = {};
    definition['@graph'].forEach(function (item) {
        var resource = item;
        var types = [].concat(resource['@type']);
        if (types.indexOf('http://linkedpipes.com/ontology/Component') > -1) {
            // Add jar file.
            item['http://linkedpipes.com/ontology/jarUri'] = gConfiguration.storage.jarPathPrefix + dataItem['jar'];
            var configurationUri = gConfiguration.storage.domain + '/resources/components/' + name + '/configuration';
            item['http://linkedpipes.com/ontology/configurationGraph'] = configurationUri;
            // Construct item list.
            listItem['id'] = constructUri(name);
            listItem['label'] = resource['http://www.w3.org/2004/02/skos/core#prefLabel'];
            listItem['color'] = resource['http://linkedpipes.com/ontology/color'];
            if (resource['http://linkedpipes.com/ontology/componentType']) {
                listItem['type'] = resource['http://linkedpipes.com/ontology/componentType']['@id'];
            }
            listItem['configurationUri'] = configurationUri;
            if (resource['http://linkedpipes.com/ontology/port']) {
                ports = [].concat(resource['http://linkedpipes.com/ontology/port']);
            }
            // Keyword list.
            if (resource['http://linkedpipes.com/ontology/keyword']) {
                listItem['keyword'] = resource['http://linkedpipes.com/ontology/keyword'];
            }
        }
        if (types.indexOf('http://linkedpipes.com/ontology/Port') > -1) {
            portsDefinition[resource['@id']] = resource;
        }
    });
    // Add ports.
    ports.forEach(function (portRef) {
        var resource = portsDefinition[portRef['@id']];
        var types = [].concat(resource['@type']);
        var port = {
            'label': resource['http://www.w3.org/2004/02/skos/core#prefLabel'],
            'binding': resource['http://linkedpipes.com/ontology/binding'],
            'type': []
        };
        // Exclude lp types ..
        types.forEach(function (type) {
            if (type === "http://linkedpipes.com/ontology/Port") {
                return;
            }
            if (type === "http://linkedpipes.com/ontology/Output") {
                listItem['outputs'].push(port);
                return;
            }
            if (type === "http://linkedpipes.com/ontology/Input") {
                listItem['inputs'].push(port);
                return;
            }
            port['type'].push(String(type));
        });
    });
    // Add to lists.
    gModule.list.push(listItem);
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
        var connection =[];
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
    gModule.list.forEach(function(item) {
        if (followup[item['id']]) {
            item['followup'] = followup[item['id']];
        }
    });
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
        var pipeline = JSON.parse(gFs.readFileSync(path));
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
gModule.onPipelineUpdate = function(pipelineObject, pipelineIri) {
    updatePipeline(pipelineObject, pipelineIri);
    rebuilFollowUp();
};

gModule.onPipelineDelete = function(pipelineIri) {
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
