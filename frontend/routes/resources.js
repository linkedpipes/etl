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





// callback(success, result)

gApiRouter.post('/executions', function (request, response) {


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
    unpackPipeline(pipelineObject, configuration, function (success, result) { // -> error, result
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
                    'filename': 'options.jsonld'
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
    var configurationAsString = ''; // Default is empty configuration.
    var pipelineAsString = '';
    // If true we need to unpack given pipeline.
    var unpackPipeline = !request.query.unpacked_pipeline;
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
        if (part.name === 'pipeline' && unpackPipeline) {
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
        } else if (unpackPipeline) {
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

