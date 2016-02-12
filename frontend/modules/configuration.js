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
        'pipelines': properties.get('storage.pipelines.directory').replace('\\:', ':'),
        'components': properties.get('storage.components.directory').replace('\\:', ':'),
        'domain': properties.get('domain.uri'),
        'jarPathPrefix' : properties.get('storage.components.path.prefix')
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

