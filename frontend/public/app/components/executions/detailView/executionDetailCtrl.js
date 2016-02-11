define([
    'angular'
], function (angular) {
    function controler($scope, $http, $routeParams, $mdDialog, $mdMedia, refreshService) {
        var executionUri = $routeParams.uri;

        $scope.execution = {};
        $scope.messages = [];
        $scope.files = [];
        $scope.labels = {};

        var typeDecorators = {
            'http://linkedpipes.com/ontology/events/ExecutionFailed': function (message) {
                message.error = {
                };
                message.icon = {
                    'name': 'error',
                    'style': {
                        'color': 'red'
                    }
                };
                message.help = true;
            }
        };
        typeDecorators['http://linkedpipes.com/ontology/events/InitializationFailed'] =
                typeDecorators['http://linkedpipes.com/ontology/events/ExecutionFailed'];

        var codeToStatus = {
            '100': {
                'label': 'Queued'
            },
            '200': {
                'label': 'Initializing'
            },
            '300': {
                'label': 'Failed'
            },
            '400': {
                'label': 'Running'
            },
            '500': {
                'label': 'Finished'
            },
            '600': {
                'label': 'Failed'
            },
            '700': {
                'label': 'Failed'
            }

        };


        var updateMessage = function (message) {
            // Store created time to be more acessible.
            message['created'] = message['http://linkedpipes.com/ontology/events/created'];
            // Get component label.
            if (message['http://linkedpipes.com/ontology/component']) {
                var labels = $scope.labels[message['http://linkedpipes.com/ontology/component']];
                if (labels) {
                    message['componentLabel'] = labels.labels[''];
                } else {
                    message['componentLabel'] = message['http://linkedpipes.com/ontology/component'];
                }
            } else {
                // Use pipeline if no name is provided.
                message['componentLabel'] = 'Pipeline';
            }
            // Set default icon.
            message.icon = {
                'name': 'info',
                'style': {
                    'color': 'blue'
                }
            };
            // Check decorators based on types.
            var types = [].concat(message['@type']);
            for (var index in types) {
                var type = types[index];
                if (typeDecorators[type]) {
                    typeDecorators[type](message);
                }
            }
        };

        var loadLabels = function (next) {
            $http.get(executionUri + '/labels').then(function (response) {
                $scope.labels = response.data['resources'];
                if (next) {
                    next();
                }
            }, function () {
                if (next) {
                    next();
                }
            });
        };

        $scope.onMessage = function (message) {
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $mdDialog.show({
                controller: messageController,
                templateUrl: 'messageDetail.html',
                parent: angular.element(document.body),
                locals: {
                    'message': message
                },
                clickOutsideToClose: true,
                fullscreen: useFullScreen
            });
        };

        $scope.onDebug = function (debug) {
            window.open(debug.browseUri, '_blank');
        };

        $scope.loadExecution = function (next) {
            $http.get(executionUri).then(function (response) {
                var exception = response.data["exception"];
                var data = response.data["payload"];
                // TODO Exception handling!
                $scope.execution = data;
                if (!$scope.execution.running) {
                    // Cancel refresh as execution is completed.
                    refreshService.reset();
                }
                //
                $scope.execution.status = codeToStatus[$scope.execution.statusCode];
                //
                if (next) {
                    next();
                }
            }, function (response) {
                // We retry later.
                if (next) {
                    next();
                }
            });
        };

        $scope.loadMessages = function (next) {
            $http.get(executionUri + "/messages").then(function (response) {
                var exception = response.data["exception"];
                var metadata = response.data["metadata"];
                var data = response.data["payload"];
                // TODO Exception handling!
                data.forEach(function (item) {
                    updateMessage(item);
                });
                $scope.messages = data;
                //
                if (next) {
                    next();
                }
            }, function (response) {
                // We retry later.
                if (next) {
                    next();
                }
            });
        };

        $scope.loadDebug = function (next) {
            $http.get(executionUri + "/debug").then(function (response) {
                var exception = response.data["exception"];
                var metadata = response.data["metadata"];
                var data = response.data["payload"];
                // TODO Exception handling!

                // Transform into a list of debug.
                $scope.debug = [];
                data['dataUnits'].forEach(function (dataUnit) {
                    // Check for RDF data unit.
                    var fusekiVisible = false;
                    dataUnit['types'].forEach(function (item) {
                        if (item === 'http://linkedpipes.com/ontology/dataUnit/sesame/1.0/rdf/GraphList' ||
                                item === 'http://linkedpipes.com/ontology/dataUnit/sesame/1.0/rdf/SingleGraph') {
                            fusekiVisible = true;
                        }
                    });
                    //
                    var record = {
                        'component': dataUnit['component'],
                        'binding': dataUnit['binding'],
                        'browseUri': dataUnit['browseUri'],
                        'fusekiVisible': fusekiVisible,
                        'uriFragment': dataUnit['uriFragment']
                    };
                    $scope.debug.push(record);
                });
                //
                if (next) {
                    next();
                }
            }, function (response) {
                // We retry later.
                if (next) {
                    next();
                }
            });
        };

        $scope.update = function () {
            $scope.loadExecution();
            $scope.loadMessages();
            $scope.loadDebug();
        };

        loadLabels(function () {
            $scope.loadExecution();
            $scope.loadMessages();
            $scope.loadDebug();
        });

        refreshService.set($scope.update);
    }

    controler.$inject = ['$scope', '$http', '$routeParams', '$mdDialog', '$mdMedia', 'service.refresh'];

    function messageController($mdDialog, $scope, message) {
        $scope.label = message['http://www.w3.org/2004/02/skos/core#prefLabel'];
        $scope.created = message.created;
        $scope.componentLabel = message.componentLabel;
        $scope.error = message.error;
        $scope.reason = message['http://linkedpipes.com/ontology/events/reason'];
        $scope.exception = message['http://linkedpipes.com/ontology/events/exception'];

        $scope.onClose = function () {
            $mdDialog.hide();
        };
    }

    messageController.$inject = ['$mdDialog', '$scope', 'message'];

    function init(app) {
        app.controller('components.executions.detail', controler);
    }
    return init;
});