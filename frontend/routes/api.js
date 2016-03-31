//
// Functional routes.
//

'use strict';

var gExpress = require('express');
var gRequest = require('request'); // https://github.com/request/request
var gUnpacker = require('./../modules/unpacker');
var gStream = require('stream');
var gConfiguration = require('./../modules/configuration');

var gApiRouter = gExpress.Router();
module.exports = gApiRouter;

var gMonitorUri = gConfiguration.executor.monitor.url;

//
// Components as templates.
//

gApiRouter.get('/info', function (request, response) {
    response.status(200).json({
        'path' : {
            'ftp' : gConfiguration.executor.ftp.uri
        }
    });
});

gApiRouter.get('/unpack', function (request, response) {
    response.status(200).setHeader('content-type', 'text/trig');
    gUnpacker.unpack(request.query.uri, {}, function (sucess, result) {
        if (sucess === false) {
            response.status(503).json(result);
        } else {
            response.status(200).setHeader('content-type', 'application/json');
            response.json(result);
        }
    });
});

gApiRouter.post('/execute', function (request, response) {
    var postUri = gMonitorUri + 'executions';
    gUnpacker.unpack( request.query.uri, request.body, function (sucess, result) {
        if (sucess === false) {
            response.status(503).json(result);
            return;
        }
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
        // Do post on executor service.
        gRequest.post({url: postUri, formData: formData}).on('error', function (error) {
            response.status(500).json({
               'exception' : {
                   'errorMessage': JSON.stringify(error),
                   'systemMessage': 'Executor-monitor is offline!',
                   'userMessage': "Can't connect to backend.",
                   'errorCode': 'CONNECTION_REFUSED'
               }
            });
        }).pipe(response);
    });
});
