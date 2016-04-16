//
// Route for resource management.
//

'use strict';

var gExpress = require('express');
var gTemplates = require('./../modules/templates');
var gPipelines = require('./../modules/pipelines');
var gRequest = require('request'); // https://github.com/request/request
var gConfiguration = require('./../modules/configuration');

var gApiRouter = gExpress.Router();
module.exports = gApiRouter;

var gMonitorUri = gConfiguration.executor.monitor.url;

//
// Components as templates.
//

gApiRouter.get('/components', function (request, response) {
    var value = {
        '@graph': gTemplates.getList()
    };
    response.json(value);
});

gApiRouter.get('/components/:name', function (request, response) {
    var value = gTemplates.getDefinition(request.params.name);
    if (value) {
        response.json(value);
    } else {
        response.status(404).send('');
    }
});

gApiRouter.get('/components/:name/:type', function (request, response) {
    var type = request.params.type;
    if (type === 'configuration') {
        var value = gTemplates.getConfigurationString(request.params.name);
        if (value) {
            response.status(200).setHeader('content-type', 'application/json');
            response.send(value);
        } else {
            response.status(404).send('');
        }
    } else if (type === 'dialog.js') {
        var value = gTemplates.getDialogJs(request.params.name);
        if (value) {
            response.status(200).setHeader('content-type', 'text/javascript');
            response.send(value);
        } else {
            response.status(404).send('');
        }
    } else if (type === 'dialog.html') {
        var value = gTemplates.getDialogHtml(request.params.name);
        if (value) {
            response.status(200).setHeader('content-type', 'text/html');
            response.send(value);
        } else {
            response.status(404).send('');
        }
    } else {
        response.status(400).json({
            'exception': {
                'errorMessage': '',
                'systemMessage': 'Type: "' + type + '".',
                'userMessage': "Invalid request.",
                'errorCode': 'INVALID_INPUT'
            }});
    }
});

//
// Pipelines.
//

gApiRouter.get('/pipelines', function (request, response) {
    var value = {
        '@graph': gPipelines.getList()
    };
    response.json(value);
});

gApiRouter.get('/pipelines/:id', function (request, response) {
    var id = request.params.id;
    var content = gPipelines.getDefinitionStream(id);
    if (content) {
        response.status(200).setHeader('content-type', 'application/json');
        content.pipe(response);
    } else {
        response.status(500).json({
            'exception': {
                'errorMessage': '',
                'systemMessage': '',
                'userMessage': 'Pipeline "' + id + '" does not exists.',
                'errorCode': 'ERROR'
            }});
    }
});

gApiRouter.delete('/pipelines/:id', function (request, response) {
    gPipelines.delete(request.params.id);
    response.status(200);
    response.send('');
});

gApiRouter.post('/pipelines', function (request, response) {
    var record = gPipelines.create();
    response.json(record);
});

gApiRouter.post('/pipelines/:id', function (request, response) {
    if (request.query.pipeline) {
        // Import existing pipeline.
        gPipelines.import(request.params.id, request.query.pipeline, function (record) {
            if (record) {
                response.json(record);
            } else {
                response.status(500).setHeader('content-type', 'application/json');
                response.json({
                    'exception': {
                        'errorMessage': '',
                        'systemMessage': '',
                        'userMessage': 'Import failed.',
                        'errorCode': 'INVALID_INPUT'
                    }});
            }
        });
    } else {
        // Create a new empty pipeline.
        var record = gPipelines.create(request.params.id);
        if (record) {
            response.json(record);
        } else {
            response.status(500).setHeader('content-type', 'application/json');
            response.json({
                'exception': {
                    'errorMessage': '',
                    'systemMessage': '',
                    'userMessage': 'Given id is already used.',
                    'errorCode': 'INVALID_INPUT'
                }});
        }
    }
});

gApiRouter.put('/pipelines/:id', function (request, response) {
    // We may need to modify the URIs in pipeline.
    if (request.query.unchecked === 'true') {
        gPipelines.update(request.params.id, request.body, false);
    } else {
        gPipelines.update(request.params.id, request.body, true);
    }
    response.status(200);
    response.send('');
});

//
// Executions.
//

var pipeGet = function (uri, response) {
    gRequest.get(uri).on('error', function (error) {
        response.status(503).json({
            'exception': {
                'errorMessage': '',
                'systemMessage': 'Executor-monitor is offline.',
                'userMessage': 'Backend is offline.',
                'errorCode': 'CONNECTION_REFUSED'
            }});
    }).pipe(response);
};

gApiRouter.get('/executions', function (request, response) {
    var uri = gMonitorUri + 'executions?';
    if (request.query.changedSince !== undefined) {
        uri += 'changedSince=' + request.query.changedSince;
    }
    pipeGet(uri, response);
});

gApiRouter.get('/executions/:id', function (request, response) {
    var uri = gMonitorUri + 'executions/' + request.params.id;
    pipeGet(uri, response);
});

gApiRouter.delete('/executions/:id', function (request, response) {
    var uri = gMonitorUri + 'executions/' + request.params.id;
    gRequest.del(uri).pipe(response);
});

gApiRouter.get('/executions/:id/pipeline', function (request, response) {
    var uri = gMonitorUri + 'executions/' + request.params.id + '/pipeline';
    pipeGet(uri, response);
});

gApiRouter.get('/executions/:id/messages', function (request, response) {
    var uri = gMonitorUri + 'executions/' + request.params.id + '/messages';
    pipeGet(uri, response);
});

gApiRouter.get('/executions/:id/logs', function (request, response) {
    var uri = gMonitorUri + 'executions/' + request.params.id + '/logs';
    pipeGet(uri, response);
});

gApiRouter.get('/executions/:id/labels', function (request, response) {
    var uri = gMonitorUri + 'executions/' + request.params.id + '/labels';
    pipeGet(uri, response);
});


gApiRouter.get('/executions/:id/debug', function (request, response) {
    var uriDebug = gMonitorUri + 'executions/' + request.params.id + '/debug';
    var uriLabels = gMonitorUri + 'executions/' + request.params.id + '/labels';
    gRequest(uriDebug, function (error, res, body) {
        if (error) {
            response.status(503).setHeader('content-type', 'application/json');
            response.send({
                'exception': {
                    'errorMessage': '',
                    'systemMessage': 'Executor-monitor is offline.',
                    'userMessage': 'Backend is offline.',
                    'errorCode': 'CONNECTION_REFUSED'
                }});
            return;
        }
        // Check for backend error code.
        if (res.statusCode !== 200) {
            response.status(404).setHeader('content-type', 'application/json');
            response.send({
                'exception': {
                    'errorMessage': '',
                    'systemMessage': 'Error response from backend: ' + response.statusCode,
                    'userMessage': 'Invalid request.',
                    'errorCode': 'INVALID_INPUT'
                }});
            return;
        }
        var debugInfo = JSON.parse(body);
        // Try to load labels.
        gRequest(uriLabels, function (error, res, body) {
            // TODO: Error handling

            var executionId = request.params.id;

            var labels;
            if (body) {
                labels = JSON.parse(body)['resources'];
            }
            if (!labels) {
                // Server might be offline - by default consider there are no labels.
                labels = {};
            }
            // Convert body to output object.
            var dataUnits = [];
            for (var index in debugInfo['dataUnits']) {
                var dataUnit = debugInfo['dataUnits'][index];
                var newDataUnit = {
                    'uriFragment': dataUnit['uriFragment'],
                    'componentUri': dataUnit['componentUri'],
                    'uri': dataUnit['iri'],
                    'browseUri': gConfiguration.executor.ftp.uri + '/' + executionId + '/' + dataUnit['uriFragment']
                };
                // Update labels.
                if (dataUnit['componentUri'] in labels) {
                    // TODO Set language -> we can transfer this to the client, also for messages etc ..
                    newDataUnit['component'] = labels[dataUnit['componentUri']]['labels'][''];
                } else {
                    // Missing label.
                    newDataUnit['component'] = dataUnit['componentUri'];
                }
                //
                dataUnits.push(newDataUnit);
            }

            var output = {
                'dataUnits': dataUnits
            };
            response.json({
                'metadata': '',
                'payload': output
            });
        });
    });
});

gApiRouter.post('/executions', function (request, response) {
    var uri = gMonitorUri + 'executions';
    // Redirect to the API for upload.
    var postRequest = gRequest.post(uri);
    request.pipe(postRequest);
    postRequest.pipe(response);
});

//
// Processes.
//

gApiRouter.get('/processes', function (request, response) {
    var uri = gMonitorUri + 'processes';
    pipeGet(uri, response);
});

gApiRouter.delete('/processes/:id', function (request, response) {
    var uri = gMonitorUri + 'processes/' + request.params.id;
    gRequest.del(uri).pipe(response);
});

gApiRouter.post('/processes/fuseki/:execution/:dataUnit', function (request, response) {
    var uri = gMonitorUri + '/processes/fuseki/' + request.params.execution + '/' + request.params.dataUnit;
    var postRequest = gRequest.post(uri);
    request.pipe(postRequest);
    postRequest.pipe(response);
});

