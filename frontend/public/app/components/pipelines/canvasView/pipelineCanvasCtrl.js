define([
    'jquery',
    'app/components/canvas/canvasDirective',
    'app/components/canvas/pipelineCanvas',
    'app/components/canvas/executionProgress',
    'app/components/pipelineEditDirective/pipelineEditDirective',
    'app/components/pipelines/configurationDialog/configurationDialogCtrl',
    'app/components/templates/templatesRepository',
    'app/components/pipelines/pipelineModelService',
    'app/components/templates/selectDialog/selectTemplateDialogCtrl',
    'app/components/pipelines/importDialog/pipelineImportDialogCtrl',
    'app/components/pipelines/detailDialog/pipelineDetailDialogCtrl'
], function (
        jQuery,
        pipelineCanvasDirective,
        canvasPipelineFactory,
        executionProgressFactory,
        pipelineEditDirective,
        componentDialogCtrl,
        templatesRepositoryFactory,
        pipelineModelService,
        selectTemplateDialog,
        importPipelineDialog,
        pipelineDetailDialog
        ) {
    function controler(
            $scope,
            $mdDialog,
            $mdMedia,
            $location,
            $http,
            $routeParams,
            $timeout,
            templateService,
            statusService,
            refreshService,
            jsonldService,
            infoService,
            pipelineService,
            executionModel,
            pipelineCanvas,
            executionCanvas
            // TODO Update names, check for factories and service.
            ) {

        $scope.canvas = {};

        $scope.pipelineEdit = {};

        $scope.data = {
            'pipelineLabel': '',
            /**
             * Type of visible tools.
             */
            'tools': {
                'edit': false,
                'execFinished': false
            }
        };

        var data = {
            'pipeline': {
                'iri': $routeParams.pipeline,
                /**
                 * JSON-LD object with pipeline.
                 */
                'model': void 0,
                /**
                 * Object of pipeline resource.
                 */
                'resource': void 0
            },
            'execution': {
                'iri': $routeParams.execution,
                'model': void 0,
                'update': true
            }
        };

        function onComponentDetail(cell, component) {
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            var comFacade = pipelineService.component;
            var templateIri = comFacade.getTemplateIri(component);
            var template = templateService.getTemplate(templateIri);
            // TODO Replace with premise.
            templateService.fetchTemplateConfiguration(template, function () {
                $mdDialog.show({
                    'controller': 'components.pipelines.configuration.dialog',
                    'templateUrl': 'app/components/pipelines/configurationDialog/configurationDialogView.html',
                    'clickOutsideToClose': false,
                    'fullscreen': useFullScreen,
                    'locals': {
                        'component': component,
                        'template': template,
                        'data': data.pipeline
                    }
                }).then(function () {
                    // Notify about change in the
                    $scope.canvas.getPaper().trigger('lp:component:changed',
                            component['@id'], component);
                }, function () {
                    // No action here.
                });
            });
        }

        function onDebug(component) {
            if (component === undefined) {
                console.log('No component selected!');
                return;
            }
            //
            storePipeline(data.pipeline.iri, true, function () {
                executePipeline(createExecuteConfiguration({
                    'to': component['@id']}), function () {
                    $location.path('/executions').search({});
                });
            });
        }

        function onAddComponent(x, y, source) {
            var filter = {};
            if (source !== undefined) {
                var comFacade = pipelineService.component;
                var templateIri = comFacade.getTemplateIri(source.component);
                filter.source = {
                    'templateIri': templateIri,
                    'binding': source.port
                };
            }
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $mdDialog.show({
                'controller': 'components.templates.select.dialog',
                'templateUrl': 'app/components/templates/selectDialog/selectTemplateDialogView.html',
                'parent': angular.element(document.body),
                'hasBackdrop': false,
                'clickOutsideToClose': true,
                'fullscreen': useFullScreen,
                'locals': {
                    'filter': filter
                }
            }).then(function (result) {
                // TODO Result of insertComponent is not a component.
                // So we need to rename the property.
                var newComponent = pipelineCanvas.insertComponent(
                        result['component'], x, y);
                if (source !== undefined) {
                    pipelineCanvas.insertConnection(
                            source.component, source.port,
                            newComponent.component, result['portBinding'],
                            []);
                }
            }, function () {
                // No action here.
            });
        }

        function onImportPipeline(x, y) {
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $mdDialog.show({
                'controller': 'components.pipelines.import.dialog',
                'templateUrl': 'app/components/pipelines/importDialog/pipelineImportDialogView.html',
                'parent': angular.element(document.body),
                'hasBackdrop': false,
                'clickOutsideToClose': true,
                'fullscreen': useFullScreen
            }).then(function (result) {
                pipelineCanvas.insertPipeline(result['pipeline'], x, y);
            }, function () {
                // No operation here.
            });
        }

        /**
         * Load pipeline name from definition.
         */
        var updateLabel = function () {
            $scope.data.pipelineLabel = pipelineService.getLabel(
                    data.pipeline.model);
            if ($scope.data.pipelineLabel === undefined
                    || $scope.data.pipelineLabel === '') {
                $scope.data.pipelineLabel = data.pipeline.resource['@id'];
            }
        };

        /**
         * Load pipeline from data.pipeline.iri into the canvas. Return
         * a promise.
         */
        function loadPipeline() {
            return $http.get(data.pipeline.iri).then(function (response) {
                data.pipeline.model = pipelineService.fromJsonLd(response.data);
                data.pipeline.resource = pipelineService.getPipeline(
                        data.pipeline.model);
                pipelineCanvas.loadPipeline(data.pipeline.model);
                updateLabel();
            });
        }

        /**
         * Load execution from data.execution.iri.
         */
        function loadExecution() {
            if (data.execution.iri === undefined) {
                data.execution.update = false;
                return;
            }
            if (data.execution.model.isFinished()) {
                return;
            }
            // Load execution and convert into a model.
            return $http.get(data.execution.iri).then(function (response) {
                data.execution.model.loadJsonLd(response.data);
                executionCanvas.update();
                // Check if execution is finished and disable refresh.
                if (data.execution.model.isFinished()) {
                    data.execution.update = false;
                    $scope.data.tools.execFinished = true;
                }
            });
        }

        /**
         * Load data for this page.
         */
        function loadData() {
            console.time('pipelineCanvasCtrl.loadData');
            loadPipeline()
                    .then(infoService.wait)
                    .then(loadExecution)
                    .then(function () {
                        console.timeEnd('pipelineCanvasCtrl.loadData');
                        // Initialize refresh here.
                        refreshService.set(function update() {
                            if (data.execution.update) {
                                loadExecution();
                            }
                        });
                    }, function (message) {
                        statusService.deleteFailed({
                            'title': "Can't load data.",
                            'response': message
                        });
                    });
        }

        function editMode() {
            $scope.data.tools.edit = true;
            $scope.data.tools.execFinished = false;
            //
            $scope.canvas.setInteractive(true);
            executionCanvas.setEnabled(false);
            $scope.pipelineEdit.setEnabled(true);
        }

        $timeout(function () {

            // Wait for the end of the initialization.

            data.execution.model = executionModel.create(jsonldService);

            pipelineCanvas.bind(
                    $scope.canvas);

            executionCanvas.bind(
                    $scope.canvas,
                    pipelineCanvas,
                    data.execution.model);

            $scope.pipelineEdit.bind(
                    $scope.canvas,
                    pipelineCanvas);

            // Connect API.
            $scope.pipelineEdit.API.onEdit = onComponentDetail;
            $scope.pipelineEdit.API.onDebug = onDebug;
            $scope.pipelineEdit.API.onAddComponent = onAddComponent;
            $scope.pipelineEdit.API.onImportPipeline = onImportPipeline;
            $scope.pipelineEdit.API.onMapping = function (component) {
                return executionCanvas.switchMapping(component['@id']);
            };
            $scope.pipelineEdit.API.mappingAvailable = function (component) {
                if (data.execution.iri === undefined) {
                    return;
                } else {
                    return executionCanvas.isMappingAvailable(
                            component['@id']);
                }
            };
            // Set mode based on the input.
            // TODO This should each component do on it own.
            if (data.execution.iri === undefined) {
                // Go to edit mode.
                editMode();
            } else {
                // Execution mode "running".
                $scope.data.tools.edit = false;
                $scope.data.tools.execFinished = false;
                //
                $scope.canvas.setInteractive(false);
                executionCanvas.setEnabled(true);
                $scope.pipelineEdit.setEnabled(false);
            }

            // TODO replace with promise.
            templateService.load(function () {
                loadData();
            }, function (response) {
                statusService.deleteFailed({
                    'title': "Can't load templates pipeline.",
                    'response': response
                });
            });
        }, 0);

        /**
         * Save current pipeline to ginve URI.
         * TODO Replace onSucess with premise.
         */
        var storePipeline = function (uri, unchecked, onSucess) {
            var jsonld = pipelineCanvas.storePipeline();
            $http({
                'method': 'PUT',
                'url': uri,
                'params': {'unchecked': unchecked},
                'headers': {'Content-Type': 'application/json'},
                'data': jsonld
            }).then(function () {
                statusService.success({
                    'title': "Pipeline saved."
                });
                if (onSucess) {
                    onSucess();
                }
            }, function (response) {
                statusService.putFailed({
                    'title': "Can't save the pipeline.",
                    'response': response
                });
            });
        };

        /**
         * Button actions.
         */
        $scope.onSave = function () {
            storePipeline(data.pipeline.iri, true);
        };

        /**
         * Create a configuration object for executePipeline function.
         *
         * TODO Update to JSON-LD format.
         */
        var createExecuteConfiguration = function (parametr) {
            var config = {};

            // Run-to arguments.
            if (parametr['to']) {
                config['execute_to'] = parametr['to'];
            }

            if (data.execution.iri === undefined) {
                // No information about execution.
                return config;
            }

            // Run-from.

            // TODO Move to executionProgress
            var mapping = {};
            var components = data.execution.model.getComponents();
            // Add components.
            for (var iri in components) {
                var component = components[iri];
                //
                if (parametr['to'] === iri) {
                    continue;
                }
                // Make sure we should (and can) use mapping
                // and if so use it.
                if (data.execution.model.mapping.isUsedForExecution(component)) {
                    mapping[iri] = component['iri'];
                }
            }
            //
            config['mapping'] = {
                'execution': data.execution.model.getIri(),
                'components': mapping
            };
            return config;
        };

        var executePipeline = function (configuration, onSucess) {
            $http.post('/resources/executions?pipeline=' + data.pipeline.iri,
                    configuration)
                    .then(function () {
                        if (onSucess) {
                            onSucess();
                        }
                    }, function (response) {
                        statusService.postFailed({
                            'title': "Can't start the execution.",
                            'response': response
                        });
                    });
        };

        $scope.onExecute = function () {
            storePipeline(data.pipeline.iri, true, function () {
                executePipeline(createExecuteConfiguration({}), function () {
                    $location.path('/executions').search({});
                });
            });
        };

        /**
         * Button action.
         */
        $scope.onDownload = function () {
            var jsonld = pipelineCanvas.storePipeline();
            saveAs(new Blob([JSON.stringify(jsonld, null, 2)],
                    {'type': 'text/json'}),
                    $scope.data.pipelineLabel + '.jsonld');
        };

        $scope.onDowloadNoCredentials = function ($event) {
            var jsonld = pipelineCanvas.storePipeline();
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            // Dialog is used to display progress.
            // TODO Introduce general progress dialog and move
            // logic to another service / function.
            $mdDialog.show({
                'controller': 'components.pipelines.export.dialog',
                'templateUrl': 'app/components/pipelines/exportDialog/pipelineExportDialogView.html',
                'parent': angular.element(document.body),
                'targetEvent': $event,
                'clickOutsideToClose': false,
                'fullscreen': useFullScreen,
                'locals': {
                    'data': {
                        'iri': $scope.data.iri,
                        'label': $scope.data.pipelineLabel,
                        'pipeline': jsonld
                    }
                }
            }).then(function (result) {
                // No action here.
            }, function () {
                // No action here.
            });
        };

        $scope.onCopy = function () {
            var id = 'created-' + (new Date()).getTime();
            $http.post('/resources/pipelines/' + id).then(function (response) {
                var newPipelineUri = response.data.uri;
                // Save pipeline under new IRI.
                storePipeline(newPipelineUri, false, function () {
                    statusService.success({
                        'title': 'Pipeline has been successfully copied.'
                    });
                    //
                    $location.path('/pipelines/edit/canvas').search(
                            {'pipeline': newPipelineUri});
                });
            }, function (response) {
                statusService.postFailed({
                    'title': "Can't create new pipeline.",
                    'response': response
                });
            });
        };

        $scope.onDelete = function (event) {
            var confirm = $mdDialog.confirm()
                    .title('Would you like to delete this pipeline?')
                    .ariaLabel('Delete pipeline.')
                    .targetEvent(event)
                    .ok('Delete pipeline')
                    .cancel('Cancel');
            $mdDialog.show(confirm).then(function () {
                // Delete pipeline.
                $http({
                    'method': 'DELETE',
                    'url': data.pipeline.iri
                }).then(function () {
                    $location.path('/pipelines').search({});
                }, function (response) {
                    statusService.deleteFailed({
                        'title': "Can't delete the pipeline.",
                        'response': response
                    });
                });
            });
        };

        $scope.onOpenMenu = function ($mdOpenMenu, event) {
            $mdOpenMenu(event);
        };

        $scope.onClose = function () {
            // Use execution IRI to determine where to go after close.
            if (data.execution.iri === undefined) {
                $location.path('/pipelines').search({});
            } else {
                $location.path('/executions').search({});
            }
        };

        $scope.onEditMode = function () {
            editMode();
        };

        /**
         * Show pipeline detail dialog.
         */
        $scope.onPipelineDetail = function ($event) {
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $mdDialog.show({
                'controller': 'components.pipelines.detail.dialog',
                'templateUrl': 'app/components/pipelines/detailDialog/pipelineDetailDialogView.html',
                'parent': angular.element(document.body),
                'targetEvent': $event,
                'clickOutsideToClose': false,
                'fullscreen': useFullScreen,
                'locals': {
                    // TODO Update dialog and argumetn passing.
                    'data': {
                        'definition': data.pipeline.resource
                    }
                }
            }).then(function () {
                // Update component view.
                updateLabel();
            }, function () {
            });
        };

    }

    controler.$inject = [
        '$scope',
        '$mdDialog',
        '$mdMedia',
        '$location',
        '$http',
        '$routeParams',
        '$timeout',
        'components.templates.services.repository',
        'services.status',
        'service.refresh',
        'services.jsonld',
        'service.info',
        'components.pipelines.services.model',
        'models.execution',
        'canvas.pipeline',
        'canvas.execution'
    ];

    return function init(app) {
        app.controller('components.pipeline.canvas.view', controler);
        pipelineCanvasDirective(app);
        canvasPipelineFactory(app);
        executionProgressFactory(app);
        pipelineEditDirective(app);
        componentDialogCtrl(app);
        templatesRepositoryFactory(app);
        pipelineModelService(app);
        selectTemplateDialog(app);
        importPipelineDialog(app);
        pipelineDetailDialog(app);
    };

});