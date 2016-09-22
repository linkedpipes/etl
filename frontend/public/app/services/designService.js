"use strict";

/**
 * Provide functionality to enable improve user experience with pipeline
 * designer.
 */
define([], function () {

    console.log('design.function');

    var _data = {};

    var _status = {
        'ready': false,
        'loading': false
    };

    const INFO_SELECTOR = {
        'property': '@type',
        'operation': 'in',
        'value': 'http://linkedpipes.com/ontology/PipelineInformation'
    };

    const INFO_TEMPLATE = {
        'tags': {
            '$property': 'http://etl.linkedpipes.com/ontology/tag'
        },
        'followup': {
            '$property': 'http://etl.linkedpipes.com/ontology/followup',
            '$oneToMany': {
                'source': {
                    '$property': 'http://etl.linkedpipes.com/ontology/source'
                },
                'target': {
                    '$property': 'http://etl.linkedpipes.com/ontology/target'
                },
                'frequency': {
                    '$property': 'http://etl.linkedpipes.com/ontology/frequency'
                }
            }
        },
    };

    function update(data, $http, jsonldService, callback) {
        if (_status.loading) {
            return;
        } else {
            _status.loading = true;
        }

        $http.get('resources/pipelines/info').then(function (response) {
            var parsedResponse = jsonldService.toJson(response.data,
                INFO_SELECTOR, INFO_TEMPLATE)[0];

            if (Array.isArray(parsedResponse.tags)) {
                data.tags = parsedResponse.tags;
            } else {
                data.tags = [parsedResponse.tags];
            }

            data.followup = {};
            if (parsedResponse.followup) {
                parsedResponse.followup.forEach((item) => {
                    if (data.followup[item['source']] === undefined) {
                        data.followup[item['source']] = {};
                    }
                    data.followup[item['source']][item['target']] =
                        item['frequency'];
                });
            }
            //
            _status.ready = true;
            _status.loading = false;
            if (callback) {
                callback();
            }
        });
    }

    function getTemplatePriority(data, source, target) {
        if (data.followup[source] === undefined) {
            return 0;
        }
        var priority = data.followup[source][target];
        if (priority === undefined) {
            return 0;
        } else {
            return priority;
        }
    }

    function factoryFunction($http, jsonldService) {

        console.log('design.factory');

        var service = {
            'update': update.bind(null, _data, $http, jsonldService),
            'getTags': () => _data.tags,
            'getTemplatePriority': getTemplatePriority.bind(null, _data),
            'initialize': function (callback) {
                if (_status.ready) {
                    if (callback) {
                        callback();
                    }
                } else {
                    this.update(callback);
                }
            }
        };

        return service;
    }

    factoryFunction.$inject = ['$http', 'services.jsonld'];

    return function init(app) {
        app.factory('service.pipelineDesign', factoryFunction);
    };

});
