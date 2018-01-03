define([
    "app/components/executions/log-tail/execution-log-tail-dialog"
], function (logTailDialogCtrl) {
    function controler($scope, $location, $timeout, $http, $mdDialog,
                       $mdMedia, refreshService, statusService, jsonldService) {

        var template = {
            'iri': {
                '$resource': ''
            },
            'start': {
                '$property': 'http://etl.linkedpipes.com/ontology/execution/start'
            },
            'end': {
                '$property': 'http://etl.linkedpipes.com/ontology/execution/end'
            },
            'status': {
                '$property': 'http://etl.linkedpipes.com/ontology/status'
            },
            'status-monitor': {
                '$property': 'http://etl.linkedpipes.com/ontology/statusMonitor'
            },
            'size': {
                '$property': 'http://etl.linkedpipes.com/ontology/execution/size'
            },
            'progress': {
                'current': {
                    '$property': 'http://etl.linkedpipes.com/ontology/execution/componentFinished'
                },
                'total': {
                    '$property': 'http://etl.linkedpipes.com/ontology/execution/componentToExecute'
                }
            },
            'pipeline': {
                'iri': {
                    '$property': 'http://etl.linkedpipes.com/ontology/pipeline'
                },
                '_pipeline': {
                    '$property': 'http://etl.linkedpipes.com/ontology/pipeline',
                    '$oneToOne': {
                        'labels': {
                            '$property': 'http://www.w3.org/2004/02/skos/core#prefLabel',
                            '$type': 'string'
                        }
                    }
                }
            },
            'metadata': {
                '_pipeline': {
                    '$property': 'http://etl.linkedpipes.com/ontology/pipeline',
                    '$oneToOne': {
                        '_metadata': {
                            '$property': 'http://linkedpipes.com/ontology/executionMetadata',
                            '$oneToOne': {
                                'executionType': {
                                    '$property': 'http://linkedpipes.com/ontology/execution/type'
                                },
                                'saveDebugData': {
                                    '$property': 'http://linkedpipes.com/ontology/saveDebugData'
                                },
                                'deleteWorkingData': {
                                    '$property': 'http://linkedpipes.com/ontology/deleteWorkingData'
                                },
                                '_component': {
                                    '$property': 'http://linkedpipes.com/ontology/execution/targetComponent',
                                    '$oneToOne': {
                                        'targetComponent': {
                                            'labels': {
                                                '$property': 'http://www.w3.org/2004/02/skos/core#prefLabel',
                                                '$type': 'string'
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };

        var decorator = function (execution) {
            execution['onClickUrl'] = "#/pipelines/edit/canvas?" +
                "pipeline=" + encodeURIComponent(execution.pipeline.iri) +
                "&execution=" + encodeURIComponent(execution.iri);
            //
            execution.id = execution.iri.substring(
                    execution.iri.lastIndexOf('executions/') + 11);
            // Store time - use direct value, so we don't have to solve
            // time zone issues.
            execution.startTime = execution.start;
            // Get label.
            if (execution.pipeline.labels) {
                if (execution.pipeline.labels['en']) {
                    execution.label = execution.pipeline.labels['en'];
                } else if (execution.pipeline.labels['']) {
                    execution.label = execution.pipeline.labels[''];
                } else {
                    // TODO Use any other.
                }
            } else {
                execution.label = execution.id;
            }
            // Compute duration
            if (execution.end) {
                var startTime = Date.parse(execution.start);
                var endTime = Date.parse(execution.end);
                var duration = (endTime - startTime) / 1000;
                var seconds = Math.ceil((duration) % 60);
                var minutes = Math.floor((duration / (60)) % 60);
                var hours = Math.floor(duration / (60 * 60));
                execution.duration = (hours < 10 ? '0' + hours : hours) +
                        ':' + (minutes < 10 ? '0' + minutes : minutes) +
                        ':' + (seconds < 10 ? '0' + seconds : seconds);
            } else {
                execution.duration = '';
            }
            // The information about progress may not be available at the
            // start of the execution.
            var current = execution.progress.current;
            var total = execution.progress.total;
            if (current === undefined || total === undefined ||
                    parseInt(total) === 0) {
                // Start with no progress.
                execution.progress.value = 0;
            } else {
                execution.progress.value = 100 * (current / total);
            }
            // Determine detail and icon type.
            switch (execution.status) {
                case 'http://etl.linkedpipes.com/resources/status/cancelled':
                    execution.canDelete = true;
                    execution.canCancel = false;
                    execution.icon = {
                        'name': 'done',
                        'style': {
                            'color': '#ff9900'
                        }
                    };
                    execution.detailType = 'FULL';
                    execution.canDelete = true;
                    break;
                case 'http://etl.linkedpipes.com/resources/status/queued':
                    execution.canDelete = true;
                    execution.canCancel = false;
                    execution.icon = {
                        'name': 'hourglass',
                        'style': {
                            'color': 'black'
                        }
                    };
                    execution.detailType = 'NONE';
                    break;
                case 'http://etl.linkedpipes.com/resources/status/initializing':
                case 'http://etl.linkedpipes.com/resources/status/running':
                    execution.canDelete = false;
                    execution.canCancel = true;
                    execution.icon = {
                        'name': 'run',
                        'style': {
                            'color': 'blue'
                        }
                    };
                    execution.detailType = 'PROGRESS';
                    break;
                case 'http://etl.linkedpipes.com/resources/status/finished':
                    execution.canDelete = true;
                    execution.canCancel = false;
                    execution.icon = {
                        'name': 'done',
                        'style': {
                            'color': 'green'
                        }
                    };
                    execution.detailType = 'FULL';
                    break;
                case 'http://etl.linkedpipes.com/resources/status/failed':
                    execution.canDelete = true;
                    execution.canCancel = false;
                    execution.icon = {
                        'name': 'error',
                        'style': {
                            'color': 'red'
                        }
                    };
                    execution.detailType = 'FULL';
                    break;
                case 'http://etl.linkedpipes.com/resources/status/cancelling':
                    execution.canDelete = false;
                    execution.canCancel = false;
                    execution.icon = {
                        'name': 'run',
                        'style': {
                            'color': '#ff9900'
                        }
                    };
                    execution.detailType = 'PROGRESS';
                    break;
                default:
                    execution.detailType = 'NONE';
                    break;
            }
            // The status above can be override by the status-monitor.
            switch (execution['status-monitor']) {
                case 'http://etl.linkedpipes.com/resources/status/unresponsive':
                    execution.canDelete = false;
                    execution.canCancel = false;
                    execution.icon = {
                        'name': 'help_outline',
                        'style': {
                            'color': 'orange'
                        }
                    };
                    break;
                case 'http://etl.linkedpipes.com/resources/status/dangling':
                    execution.canDelete = true;
                    execution.canCancel = false;
                    execution.icon = {
                        'name': 'help_outline',
                        'style': {
                            'color': 'red'
                        }
                    };
                    break;
                default:
                    break;
            }
            // Update metadata.

            switch(execution.metadata.executionType) {
                case 'http://linkedpipes.com/resources/executionType/Full':
                    execution.metadata.executionTypeLabel =
                            'Full execution';
                    break;
                case 'http://linkedpipes.com/resources/executionType/DebugFrom':
                    execution.metadata.executionTypeLabel =
                            'Partial execution (debug from)';
                    break;
                case 'http://linkedpipes.com/resources/executionType/DebugTo':
                    execution.metadata.executionTypeLabel =
                            'Partial execution (debug to: "' +
                            execution.metadata.targetComponent.labels[''] +
                            '")';
                    break;
                case 'http://linkedpipes.com/resources/executionType/DebugFromTo':
                    execution.metadata.executionTypeLabel =
                            'Partial execution (debug from & to: "' +
                            execution.metadata.targetComponent.labels[''] +
                            '")';
                    break;
                default:
                    // Can happen for older executions.
                    execution.metadata.executionTypeLabel = '';
                    break;
            }
            if (execution.metadata.deleteWorkingData === "true") {
                execution.metadata.executionTypeLabel += " (No working data)";
            } else {
                if (execution.metadata.saveDebugData === "false") {
                    execution.metadata.executionTypeLabel += " (No debug data)";
                }
            }
        };

        $scope.repository = jsonldService.createRepository({
            'template': template,
            'query': {
                'data': {
                    'property': '@type',
                    'operation': 'in',
                    'value': 'http://etl.linkedpipes.com/ontology/Execution'
                },
                'deleted': {
                    'property': '@type',
                    'operation': 'in',
                    'value': 'http://etl.linkedpipes.com/ontology/Deleted'
                }
            },
            'decorator': decorator,
            'url': '/resources/executions'
        });

        $scope.onExecution = function (execution) {
            $location.path('/executions/detail').search({
                'execution': execution.iri
            });
        };

        $scope.onExecute = function (execution) {
            $http.post('/resources/executions?pipeline='
                    + execution.pipeline.iri)
                    .then(function () {
                        $scope.repository.update();
                    }, function (response) {
                        statusService.httpPostFailed({
                            'title': "Can't start the execution.",
                            'response': response
                        });
                    });
        };

        $scope.onDelete = function (execution) {
            $scope.repository.delete(execution);
        };

        $scope.onCancel = function (execution) {
            var body = {"reason" : "User request."};
            $http.post('/resources/executions/cancel?id='
                + execution.id, body)
            .then(function () {
                execution.cancelling = true;
            }, function (response) {
                statusService.httpPostFailed({
                    'title': "Can't cancel the execution.",
                    'response': response
                });
            });
        };

        $scope.openMenu = function ($mdOpenMenu, ev) {
            $mdOpenMenu(ev);
        };

        $scope.openLogTail = function(execution) {
            const useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $mdDialog.show({
                'controller': 'execution.log-tail.dialog',
                'templateUrl': 'app/components/executions/log-tail/execution-log-tail-dialog.html',
                'clickOutsideToClose': false,
                'fullscreen': useFullScreen,
                'locals': {
                    'execution': execution
                }
            }).then(function () {
                // No action here.
            }, function () {
                // No action here.
            });
        };

        var initialize = function () {
            $scope.repository.load(function () {
                },
                function (response) {
                    statusService.httpGetFailed({
                        'title': "Can't load data.",
                        'response': response
                    });
                });

            refreshService.set(function () {
                $scope.repository.update();
            });
        };

        $timeout(initialize, 0);
    }

    //
    controler.$inject = ['$scope', '$location', '$timeout', '$http',
        '$mdDialog', '$mdMedia', 'service.refresh', 'services.status',
        'services.jsonld'];
    //
    function init(app) {
        // refreshService
        // repositoryService
        app.controller('components.executions.list', controler);
        logTailDialogCtrl(app);
    }

    return init;
});
