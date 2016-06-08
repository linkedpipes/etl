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
var gStream = require('stream');

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
        // Import pipeline from given URL.
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

    var body = '';
    request.on('data', function (chunk) {
        body += chunk;
    });
    request.on('end', function () {
        if (gPipelines.update(request.params.id, JSON.parse(body),
                request.query.unchecked !== 'true')) {
            response.status(200).send('');
        } else {
            response.status(500).json({
                'exception': {
                    'errorMessage': '',
                    'systemMessage': '',
                    'userMessage': 'Pipeline does not exist.',
                    'errorCode': 'ERROR'
                }});
        }


    });
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

// Class for appending stream.
var StreamCombiner = function (stream) {

    // Wrapped stream.
    this.target = stream;

    this.sources = [];

    // Index of currently streaming stream.
    this.index = -1;

    // If true then close once there are no streams.
    this.closeOnEnd = false;

    this.streaming = false;

    // If true stream on this.index ended and was closed.
    this.currentClosed = false;

    // Append given stream to the wrapped stream.
    this.append = function (stream) {
        this.sources.push(stream);
        console.log('  : append() as ', this.sources.length - 1);
        // Here we rely on single thread execution.
        if (!this.streaming) {
            this.pipeNext();
        }
    };

    this.end = function () {
        console.log('  : end()');
        this.closeOnEnd = true;
        // Here we rely on single thread execution.
        if (!this.streaming) {
            this.pipeNext();
        }
    };

    this.closeCurrent = function () {
        if (this.index > -1 && !this.currentClosed) {
            console.log('   : closing', this.index);
            var oldStream = this.sources[this.index];
            oldStream.unpipe(this.target);
            this.currentClosed = true;
        }
    };

    // Look for next stream and pipe it.
    this.pipeNext = function () {
        this.streaming = true;
        if (this.index + 1 < this.sources.length) {
            // Unpipe finished.
            this.closeCurrent();
            // Send new stream.
            console.log('   : sending ', this.index + 1, ' of ',
                    this.sources.length);

            this.index += 1;
            var stream = this.sources[this.index];
            stream.on('end', function () {
                // Check for next pipeline.
                console.log('     end');
                this.pipeNext();
            }.bind(this));
            stream.pipe(this.target, {'end': false});
            this.currentClosed = false;
        } else {
            // Unpipe finished.
            this.closeCurrent();
            // Nothing to stream.
            if (this.closeOnEnd) {
                console.log('  : closing');
                // Close the target stream.
                this.target.end();
            } else {
                console.log('  : waiting');
            }
            // Wait for next pipeline. Here we rely on a single thread
            // execution.
            this.streaming = false;
        }
    };
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
    console.log('  content-type: ', request.headers['content-type']);

    if (request.headers['content-type'] === undefined ||
            request.headers['content-type'].toLowerCase().
            indexOf('multipart/form-data') === -1) {
        // It's not multipart/form-data request.
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
        console.time('  unpack');
        var pipelineObject = {
            'iri': request.query.pipeline
        };
        var body = '';
        request.on('data', function (chunk) {
            body += chunk;
        });
        request.on('end', function () {
            if (body === '') {
                configuration = {};
            } else {
                try {
                    configuration = JSON.parse(body);
                } catch (error) {
                    console.error(error);
                    response.status(500).json({
                        'exception': {
                            'errorMessage': JSON.stringify(error),
                            'systemMessage': 'Invalid configuration',
                            'userMessage': "Invalid execution command.",
                            'errorCode': 'ERROR'
                        }
                    });
                    return;
                }
            }
            gUnpacker.unpack(pipelineObject, configuration, function (sucess, result) {
                console.timeEnd('  unpack');
                if (sucess === false) {
                    response.status(503).json(result);
                }
                console.time('  stringify');
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
                console.timeEnd('  stringify');
                // Do post on executor service.
                console.time('  POST');
                gRequest.post({
                    'url': gMonitorUri + 'executions',
                    'formData': formData,
                    'headers': {
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
        });
        return;
    }

    // This is a multipart/form-data request.
    console.log('  multipart/form-data');
    console.time('  total');
    // Open the POST request to the executor.
    var postUrl = gUrl.parse(gMonitorUri + 'executions');
    var boundaryString = '------------------------a76d7ee9b9b2b7ef';
    var post_options = {
        'host': postUrl.postname,
        'port': postUrl.port,
        'path': postUrl.path,
        'method': 'POST',
        'headers': {
            'Content-Type': 'multipart/form-data; boundary=' + boundaryString,
            'Transfer-Encoding': 'chunked',
            'Accept': 'application/json',
            'Connection': 'keep-alive'
        }
    };
    // In case of failure, we don't want to pipe the response from
    // executor-monitor, instead custom error is provided.
    var pipePost = true;
    var postRequest = gHttp.request(post_options, function (res) {
        // Pipe result bach to caller if pipePost is true.
        if (pipePost) {
            res.pipe(response);
        }
    });
    // We use wrap to pipe multiple streams.
    var postStream = new StreamCombiner(postRequest);
    // Parse incomming data.
    var form = new gMultiparty.Form();
    var firstBoundary = true;
    var configuration = '{}'; // Default is empty configuration.
    var pipelineAsString = '';
    // If true we need to unpack given pipeline.
    var unpack_pipeline = !request.query.unpacked_pipeline;
    form.on('part', function (part) {
        if (part.name === 'configuration') {
            console.log('  configuration detected');
            configuration = '';
            // Read and save the configuration.
            part.on('data', function (chunk) {
                configuration += chunk;
            });
            return;
        }
        if (part.name === 'pipeline' && unpack_pipeline) {
            console.log('  unpacking pipeline');
            // Read and save the pipeline for unpacking.
            part.on('data', function (chunk) {
                pipelineAsString += chunk;
            });
            return;
        }
        // Pipe to executor server.
        var textStream = new gStream.PassThrough();
        if (firstBoundary) {
            firstBoundary = false;
        } else {
            textStream.write('\r\n');
        }
        textStream.write('--' + boundaryString + '\r\n');
        textStream.write('Content-Disposition:' + part.headers['content-disposition'] + '\r\n');
        textStream.write('Content-Type: ' + part.headers['content-type'] + '\r\n\r\n');
        textStream.end();
        // Append header and data.
        postStream.append(textStream);
        postStream.append(part);
    });

    form.on('close', function () {
        console.timeEnd('  posting data');
        // When we here, all the data were transfered or read.
        var pipelineObject = {};
        if (request.query.pipeline !== undefined) {
            // Get pipeline based on IRI.
            pipelineObject.iri = request.query.pipeline;
        } else if (unpack_pipeline) {
            // Pipeline (frontend format) form the POST request.
            pipelineObject.pipeline = pipelineAsString;
        } else {
            // Pipeline was send as a part of request, se we just
            // need to close the request.
            var textStream = new gStream.PassThrough();
            textStream.write('\r\n--' + boundaryString + '--\r\n');
            textStream.end();
            postStream.append(textStream);
            textStream.end();
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
                    postStream.end();
                    return;
                }
                var textStream = new gStream.PassThrough();
                if (firstBoundary) {
                    firstBoundary = false;
                } else {
                    textStream.write('\r\n');
                }
                textStream.write('--' + boundaryString + '\r\n');
                textStream.write('Content-Disposition: form-data; name="pipeline"; filename="pipeline.jsonld"\r\n');
                textStream.write('Content-Type: application/octet-stream\r\n\r\n');
                textStream.write(JSON.stringify(result));
                textStream.write('\r\n--' + boundaryString + '--\r\n');
                textStream.end();
                postStream.append(textStream);
                postStream.end();
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
            postStream.end();
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
