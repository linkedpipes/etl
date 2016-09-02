'use strict';

var gExpress = require('express');
var gRequest = require('request'); // https://github.com/request/request
var gConfiguration = require('./../modules/configuration');
var gMultiparty = require('multiparty');
var gHttp = require('http');
var gUrl = require('url');
var gStream = require('stream');

var gApiRouter = gExpress.Router();
module.exports = gApiRouter;

var gMonitorUri = gConfiguration.executor.monitor.url;

// TODO Split access via dereference and REST like with IRI.

//
// Components as templates.
//

gApiRouter.get('/components', function (request, response) {
    var options = {
        'url': gConfiguration.storage.url + '/api/v1/components/list',
        'headers': {
            'Accept': 'application/ld+json'
        }
    }
    gRequest.get(options).on('error', function (error) {
        response.status(503).json({
            'exception': {
                'errorMessage': error,
                'systemMessage': 'Executor-monitor is offline.',
                'userMessage': 'Backend is offline.',
                'errorCode': 'CONNECTION_REFUSED'
            }
        });
    }).pipe(response);
});

gApiRouter.get('/components/definition', function (request, response) {
    var options = {
        'url': gConfiguration.storage.url +
        '/api/v1/components/definition?iri=' + request.query.iri,
        'headers': {
            'Accept': 'application/ld+json'
        }
    }
    gRequest.get(options).on('error', function (error) {
        response.status(503).json({
            'exception': {
                'errorMessage': error,
                'systemMessage': 'Executor-monitor is offline.',
                'userMessage': 'Backend is offline.',
                'errorCode': 'CONNECTION_REFUSED'
            }
        });
    }).pipe(response);
});


gApiRouter.post('/components', function (request, response) {
    var url = gConfiguration.storage.url + '/api/v1/components/';
    request.pipe(gRequest.post(url, {
        'form': request.body
    }), {
        'end': false
    }).pipe(response);
});

//
// Pipelines.
//

gApiRouter.get('/pipelines', function (request, response) {
    var options = {
        'url': gConfiguration.storage.url + '/api/v1/pipelines/list',
        'headers': {
            'Accept': 'application/ld+json'
        }
    }
    gRequest.get(options).on('error', function (error) {
        response.status(503).json({
            'exception': {
                'errorMessage': error,
                'systemMessage': 'Executor-monitor is offline.',
                'userMessage': 'Backend is offline.',
                'errorCode': 'CONNECTION_REFUSED'
            }
        });
    }).pipe(response);
});

/**
 * Import a pipeline from given IRI as a new pipeline.
 *
 * @param request
 * @param response
 */
function postImportPipeline(request, response, url) {
    var form = new gMultiparty.Form();
    var importOptions = {
        'content': '',
        'type': ''
    };
    form.on('part', function (part) {
        if (part.name === 'options') {
            importOptions.type = part['headers']['content-type']
            // Read and save the configuration.
            part.on('data', function (chunk) {
                importOptions.content += chunk;
            });
        } else {
            part.resume();
        }
    });
    form.on('close', function () {
        if (importOptions.content === '') {
            importOptions.content = JSON.stringify({
                '@id': 'http://localhost/options',
                '@type': 'http://linkedpipes.com/ontology/UpdateOptions',
                'http://etl.linkedpipes.com/ontology/import': true
            });
            importOptions.options = 'appplication/ld+json';
        }
        // Get pipeline.
        gRequest.get({
            'url': request.query.pipeline,
            'headers': {
                'Accept': 'application/ld+json'
            }
        }, function (error, http, bodyPipeline) {
            if (error) {
                response.status(503).json({
                    'exception': {
                        'errorMessage': error,
                        'systemMessage': '',
                        'userMessage': 'Can not download pipeline.',
                        'errorCode': 'INVALID_INPUT'
                    }
                });
                return;
            }
            // We got options and pipeline, create a pipeline.
            var options = {
                'url': url,
                'headers': {
                    'Accept': 'application/ld+json'
                },
                'formData': {
                    'options': {
                        'value': importOptions.content,
                        'options': {
                            'contentType': importOptions.type,
                            'filename': 'options.jsonld'
                        }
                    },
                    'pipeline': {
                        'value': bodyPipeline,
                        'options': {
                            'contentType': 'application/ld+json',
                            'filename': 'pipeline.jsonld'
                        }
                    }
                }
            };
            gRequest.post(options).pipe(response);
        });
    });
    form.parse(request);
}

gApiRouter.post('/pipelines', function (request, response) {
    var url = gConfiguration.storage.url + '/api/v1/pipelines';
    if (request.query.pipeline) {
        // We need to parse the body, get the pipeline and append
        // the pipeline to the body.
        postImportPipeline(request, response, url);
    } else {
        // We can just pipe the content to the storage component.
        request.pipe(gRequest.post(url)).pipe(response);
    }
});


// TODO This is more part of the API.
gApiRouter.post('/localize', function (request, response) {
    var url = gConfiguration.storage.url + '/api/v1/pipelines/localize';
    if (request.query.pipeline) {
        // We need to parse the body, get the pipeline and append
        // the pipeline to the body.
        postImportPipeline(request, response, url);
    } else {
        // We can just pipe the content to the storage component.
        request.pipe(gRequest.post(url)).pipe(response);
    }
});

gApiRouter.get('/pipelines/:id', function (request, response) {

    // Parse IRI.
    var queryIri;
    var queryParams = '';
    var queryIndex = request.originalUrl.indexOf('?');
    if (queryIndex === -1) {
        queryIri = request.originalUrl;
    } else {
        queryIri = request.originalUrl.substring(0, queryIndex);
        queryParams = '&' + request.originalUrl.substring(queryIndex + 1);
    }

    var options = {
        'url': gConfiguration.storage.url + '/api/v1/pipelines?iri='
        + encodeURI(gConfiguration.storage.domain + queryIri) + queryParams,
        'headers': {
            'Accept': 'application/ld+json'
        }
    }

    gRequest.get(options).on('error', function (error) {
        response.status(503).json({
            'exception': {
                'errorMessage': error,
                'systemMessage': 'Executor-monitor is offline.',
                'userMessage': 'Backend is offline.',
                'errorCode': 'CONNECTION_REFUSED'
            }
        });
    }).pipe(response);

});

gApiRouter.delete('/pipelines/:id', function (request, response) {
    var url = gConfiguration.storage.url + '/api/v1/pipelines?iri='
        + encodeURI(gConfiguration.storage.domain + request.originalUrl);
    gRequest.del(url).pipe(response);
});

gApiRouter.put('/pipelines/:id', function (request, response) {
    var body = '';
    request.on('data', function (chunk) {
        body += chunk;
    });
    request.on('end', function () {
        // We need the URI without any options, suffixes etc ...
        var urlSuffix = request.originalUrl;
        var n = urlSuffix.indexOf('?');
        urlSuffix = urlSuffix.substring(0, n != -1 ? n : s.length);
        //
        var options = {
            'url': gConfiguration.storage.url + '/api/v1/pipelines?iri='
            + encodeURI(gConfiguration.storage.domain + urlSuffix),
            'headers': {},
            'formData': {
                'pipeline': {
                    'value': body,
                    'options': {
                        'contentType': 'application/ld+json',
                        'filename': 'pipeline.jsonld'
                    }
                }
            }
        };
        gRequest.put(options).on('error', function (error) {
            response.status(503).json({
                'exception': {
                    'errorMessage': error,
                    'systemMessage': 'Executor-monitor is offline.',
                    'userMessage': 'Backend is offline.',
                    'errorCode': 'CONNECTION_REFUSED'
                }
            });
        }).pipe(response);
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
            }
        });
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


// callback(success, result)
function unpack(pipelineObject, optionsAsString, callback) {
    // pipelineObject.iri
    // pipelineObject.pipeline
    var formData = {
        'options': {
            'value': optionsAsString,
            'options': {
                'contentType': 'application/ld+json',
                'filename': 'options.jsonld'
            }
        }
    };
    var headers = {
        'Accept': 'application/ld+json'
    };
    //
    var url = gConfiguration.storage.url + '/api/v1/pipelines/unpack';
    if (pipelineObject.iri) {
        url += '?iri=' + encodeURIComponent(pipelineObject.iri);
    }
    if (pipelineObject.pipeline) {
        formData['pipeline'] = {
            'value': pipelineObject.pipeline,
            'options': {
                'contentType': 'application/ld+json',
                'filename': 'pipeline.jsonld'
            }
        }
    }
    //
    console.time('  [POST] /unpack');
    gRequest.post({'url': url, 'formData': formData, 'headers': headers},
        function optionalCallback(error, httpResponse, body) {
            if (error) {
                callback(false, body);
            } else {
                callback(true, body);
            }
        });
}

gApiRouter.post('/executions', function (request, response) {

    console.log('[POST] /executions');
    console.log('  content-type: ', request.headers['content-type']);

    if (request.headers['content-type'] === undefined ||
        request.headers['content-type'].toLowerCase().indexOf('multipart/form-data') === -1) {
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
            var configuration = body;
            // Post pipeline for unpacking.
            unpack(pipelineObject, configuration, function (success, result) {
                console.timeEnd('  unpack');
                if (success === false) {
                    response.status(503).json(result);
                    return;
                }
                console.time('  stringify');
                var formData = {
                    'format': 'application/ld+json',
                    'pipeline': {
                        'value': result,
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
        // Pipe result back to caller if pipePost is true.
        if (pipePost) {
            res.pipe(response);
        }
    });
    // We use wrap to pipe multiple streams.
    var postStream = new StreamCombiner(postRequest);
    // Parse incoming data.
    var form = new gMultiparty.Form();
    var firstBoundary = true;
    var configuration = ''; // Default is empty configuration.
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
            unpack(pipelineObject,
                configuration, function (success, result) {
                    console.timeEnd('  unpack');
                    if (success === false) {
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
                    textStream.write(result);
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
            // Here we fail the multipart/request as we fail to write
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



