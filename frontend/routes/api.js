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

// Access to components.

gApiRouter.get('/components/:type', function (request, response) {
    // Re-post to the storage.
    var iri = gConfiguration.storage.url + '/api/v1/components/';
    switch (request.params.type) {
        case 'interface':
            iri += 'interface?';
            iri += 'iri=' + encodeURIComponent(request.query.iri)
            break;
        case 'definition':
            iri += 'definition?';
            iri += 'iri=' + encodeURIComponent(request.query.iri)
            break;
        case 'effective':
            iri += 'configEffective?';
            iri += 'iri=' + encodeURIComponent(request.query.iri)
            break;
        case 'config':
            iri += 'config?';
            iri += 'iri=' + encodeURIComponent(request.query.iri)
            break;
        case 'configTemplate':
            iri += 'configTemplate?';
            iri += 'iri=' + encodeURIComponent(request.query.iri)
            break;
        case 'configDescription':
            iri += 'configDescription?';
            iri += 'iri=' + encodeURIComponent(request.query.iri)
            break;
        case 'static':
            iri += 'static?';
            iri += 'iri=' + encodeURIComponent(request.query.iri)
            iri += '&file=' + encodeURIComponent(request.query.file)
            break;
        case 'dialog':
            iri += 'dialog?';
            iri += 'iri=' + encodeURIComponent(request.query.iri)
            iri += '&file=' + encodeURIComponent(request.query.file)
            iri += '&name=' + encodeURIComponent(request.query.name)
            break;
        default:
            response.status(400).json({
                'exception': {
                    'errorMessage': '',
                    'systemMessage': '',
                    'userMessage': 'Missing resource.',
                    'errorCode': 'CONNECTION_REFUSED'
                }
            });
            return;
    }
    // Pass header options.
    var options = {
        'url': iri,
        'headers': request.headers
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

gApiRouter.get('/jars/file', function (request, response) {
    var iri = gConfiguration.storage.url + '/api/v1/jars/file?';
    iri += 'iri=' + encodeURIComponent(request.query.iri);
    //
    gRequest.get(iri).on('error', function (error) {
        response.status(503).json({
            'exception': {
                'errorMessage': '',
                'systemMessage': 'Executor-monitor is offline.',
                'userMessage': 'Backend is offline.',
                'errorCode': 'CONNECTION_REFUSED'
            }
        });
    }).pipe(response);
});

// Updates for templates

gApiRouter.post('/components/config', function (request, response) {
    var url = gConfiguration.storage.url +
        '/api/v1/components/config?iri=' +
        encodeURIComponent(request.query.iri);
    request.pipe(gRequest.post(url, {
        'form': request.body
    }), {
        'end': false
    }).pipe(response);
});

gApiRouter.post('/components/component', function (request, response) {
    var url = gConfiguration.storage.url +
        '/api/v1/components/component?iri=' +
        encodeURIComponent(request.query.iri);
    request.pipe(gRequest.post(url, {
        'form': request.body
    }), {
        'end': false
    }).pipe(response);
});

