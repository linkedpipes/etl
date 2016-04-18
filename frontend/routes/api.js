//
// Functional routes.
//

'use strict';

var gExpress = require('express');
var gConfiguration = require('./../modules/configuration');

var gApiRouter = gExpress.Router();
module.exports = gApiRouter;

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
