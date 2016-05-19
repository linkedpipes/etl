//
// Route for resource management.
//

'use strict';

var gExpress = require('express');
var gTemplates = require('./../modules/templates');
var gPipelines = require('./../modules/pipelines');
var gRequest = require('request'); // https://github.com/request/request
var gConfiguration = require('./../modules/configuration');
var gUnpacker = require('./../modules/unpacker');

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

gApiRouter.post('/pipelines', function (request, response) {
    var record = gPipelines.create();
    response.json(record);
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

gApiRouter.post('/executions', function (request, response) {
    var postUri = gMonitorUri + 'executions';
    // Unpack and create an execution.
    console.time('Execution start');
    console.time('  Unpack');
    gUnpacker.unpack(request.query.pipeline, request.body, function (sucess, result) {
        console.timeEnd('  Unpack');

        if (sucess === false) {
            response.status(503).json(result);
            return;
        }
        console.time('  Stringify');
        var formData = {
            format: 'application/ld+json',
            file: {
                value: JSON.stringify(result),
                options: {
                    contentType: 'application/octet-stream',
                    filename: 'file.jsonld'
                }
            }
        };
        console.timeEnd('  Stringify');
        // Do post on executor service.
        console.time('  POST');
        gRequest.post({url: postUri, formData: formData}).on('error', function (error) {
            response.status(500).json({
                'exception': {
                    'errorMessage': JSON.stringify(error),
                    'systemMessage': 'Executor-monitor is offline!',
                    'userMessage': "Can't connect to backend.",
                    'errorCode': 'CONNECTION_REFUSED'
                }
            });
        }).on('response', function (response) {
            console.timeEnd('  POST');
            console.timeEnd('Execution start');
        }).pipe(response);
    });
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

gApiRouter.get('/executions/:id/logs', function (request, response) {
    var uri = gMonitorUri + 'executions/' + request.params.id + '/logs';
    pipeGet(uri, response);
});

