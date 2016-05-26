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
var gMultiparty = require('multiparty');
var gHttp = require('http');
var gUrl = require('url');


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
        // Update exisitng pipeline.
        // TODO Body must contains pipeline definition !
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

    console.log('[POST] /executions');
    console.log('content: ', request.headers['content-type']);

    if (request.headers['content-type'] === undefined ||
            !request.headers['content-type'].toLowerCase().
            startsWith('multipart/form-data')) {

        console.log('request.query', request.query);

        if (request.query.pipeline === undefined) {
            response.status(500).json({
                'exception': {
                    'systemMessage': 'Invalid request, missing pipeline, no multipart/form-data.',
                    'userMessage': "Invalid request.",
                    'errorCode': 'ERROR'
                }
            });
            return;
        }
        // This is not a multipart request, but we have the pipeline
        // IRI in the URL. The content of the body
        // is considered to be a configuration.
        console.time('  Unpack');
        var pipelineObject = {
            'iri': request.query.pipeline
        };
        gUnpacker.unpack(pipelineObject, request.body, function (sucess, result) {
            console.timeEnd('  Unpack');
            if (sucess === false) {
                response.status(503).json(result);
                return;
            }
            console.time('  Stringify');
            var formData = {
                'format': 'application/ld+json',
                'pipeline': {
                    'value': JSON.stringify(result),
                    'options': {
                        'contentType': 'application/octet-stream',
                        'filename': 'pipeline.jsonld'
                    }
                }
            };
            console.timeEnd('  Stringify');
            // Do post on executor service.
            console.time('  POST');
            gRequest.post({
                'url': gMonitorUri + 'executions',
                'formData': formData,
                'headers' : {
                    'Accept': 'application/json'
                }
            }).on('error', function (error) {
                response.status(500).json({
                    'exception': {
                        'errorMessage': JSON.stringify(error),
                        'systemMessage': 'Executor-monitor is offline!',
                        'userMessage': "Can't connect to backend.",
                        'errorCode': 'CONNECTION_REFUSED'
                    }
                });
            }).on('response', function (res) {
                console.timeEnd('  POST');
            }).pipe(response);
        });
        return;
    }

    // This is a multipart/form-data request.
    console.log('  multipart/form-data');
    console.time('  total');
    // Open the POST request to the executor.
    var postUrl = gUrl.parse(gMonitorUri + 'executions');
    var boundary = '------------------------a76d7ee9b9b2b7ef';
    var post_options = {
        host: postUrl.postname,
        port: postUrl.port,
        path: postUrl.path,
        method: 'POST',
        headers: {
            'Content-Type': 'multipart/form-data; boundary=' + boundary,
            'Transfer-Encoding': 'chunked',
            'Accept': 'application/json',
            'Connection': 'keep-alive'
        }
    };
    // In case of failure, we don't want to pipe the response from
    // executor-monitor, instead custom error is provided.
    var pipePost = true;
    var postRequest = gHttp.request(post_options, function (res) {
        if (pipePost) {
            res.pipe(response);
        }
    });
    // Parse incomming data.
    var form = new gMultiparty.Form();
    var firstBoundary = true;
    var configuration = '{}'; // Default is empty configuration.
    var pipelineAsString = '';
    form.on('part', function (part) {
        if (part.name === 'configuration') {
            configuration = '';
            // Read and save the configuration.
            part.on('data', function (chunk) {
                configuration += chunk;
            });
            return;
        }
        if (part.name === 'pipeline' && request.query.unpack_pipeline) {
            // Read and save the pipeline.
            part.on('data', function (chunk) {
                pipelineAsString += chunk;
            });
            return;
        }
        // Pipe to executor server.
        if (firstBoundary) {
            firstBoundary = false;
        } else {
            postRequest.write('\r\n');
        }
        postRequest.write('--' + boundary + '\r\n');
        postRequest.write('Content-Disposition:' + part.headers['content-disposition'] + '\r\n');
        postRequest.write('Content-Type: ' + part.headers['content-type'] + '\r\n\r\n');
        part.pipe(postRequest, {'end': false});
    });

    form.on('close', function () {
        console.timeEnd('  posting data');
        // When we here, all the data were transfered or read.
        var pipelineObject = {};
        if (request.query.unpack_pipeline !== undefined) {
            pipelineObject.pipeline = pipelineAsString;
        } else if (request.query.pipeline !== undefined) {
            pipelineObject.iri = request.query.pipeline;
        } else {
            // Pipeline send as a part of request.
            postRequest.write('\r\n--' + boundary + '--\r\n');
            postRequest.end();
            console.timeEnd('  total');
            return;
        }
        // Unpack the pipeline.
        try {
            console.time('  unpack');
            gUnpacker.unpack(pipelineObject,
                    JSON.parse(configuration), function (sucess, result) {
                console.timeEnd('  unpack');
                if (sucess === false) {
                    console.log('unpack:fail');
                    pipePost = false;
                    response.status(503).json(result);
                    postRequest.end();
                    return;
                }
                if (firstBoundary) {
                    firstBoundary = false;
                } else {
                    postRequest.write('\r\n');
                }
                postRequest.write('--' + boundary + '\r\n');
                postRequest.write('Content-Disposition: form-data; name="pipeline"; filename="pipeline.jsonld"\r\n');
                postRequest.write('Content-Type: application/octet-stream\r\n\r\n');
                postRequest.write(JSON.stringify(result));
                postRequest.write('\r\n--' + boundary + '--\r\n');
                postRequest.end();
                console.timeEnd('  total');
            });
        } catch (err) {
            console.timeEnd('  unpack');
            console.log('unpack:exception', err, err.stack);
            pipePost = false;
            response.status(500).json({
                'exception': {
                    'systemMessage': err.message,
                    'userMessage': "Can't start pipeline.",
                    'errorCode': 'ERROR'
                }
            });
            // Here we fail the multiplart/request as we fail to write
            // the closing boundary.
            postRequest.end();
            console.timeEnd('  total');
        }
    });

    console.time('  posting data');
    form.parse(request);
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

