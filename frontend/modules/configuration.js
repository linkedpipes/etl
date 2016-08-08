//
// Contains server configuration.
//

'use strict';

var gFs = require('fs');
var gPropertiesReader = require('properties-reader');

var properties = gPropertiesReader(process.env.configFileLocation);

// Construct configuraiton object.
module.exports = {
    'frontend': {
        'port': properties.get('frontend.webserver.port')
    },
    'storage': {
        'url' : properties.get('storage.uri'),
        'domain': properties.get('domain.uri')
    },
    'executor': {
        'monitor': {
            'url': properties.get('executor-monitor.webserver.uri')
        },
        'ftp': {
            'uri': properties.get('executor-monitor.ftp.uri')
        }
    }
};

// TODO Add check and error handling!

