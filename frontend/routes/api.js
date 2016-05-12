//
// Functional routes.
//

'use strict';

var gExpress = require('express');
var gConfiguration = require('./../modules/configuration');
var gRequest = require('request'); // https://github.com/request/request

var gApiRouter = gExpress.Router();
module.exports = gApiRouter;

//
// Components as templates.
//

gApiRouter.get('/info', function (request, response) {
    response.status(200).json({
        'path': {
            'ftp': gConfiguration.executor.ftp.uri
        }
    });
});

gApiRouter.get('/proxy', function (request, response) {
    // request.query.url must contains IRI of a fragment to proxy download from.
    gRequest.get(request.query.url).on('error', function (error) {
        response.status(503).json({
            'exception': {
                'errorMessage': '',
                'systemMessage': 'Executor-monitor is offline.',
                'userMessage': 'Backend is offline.',
                'errorCode': 'CONNECTION_REFUSED'
            }});
    }).pipe(response);
});

