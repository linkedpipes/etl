define([
    'jquery',
    'jsonld',
    'app/components/canvas/canvasDirective',
    'app/components/canvas/pipelineCanvas',
    'app/components/canvas/executionProgress',
    'app/components/pipelineEditDirective/pipelineEditDirective',
    'app/components/pipelines/pipelineModelService',
    'app/components/templates/selectDialog/templateSelectDialog',
    'app/components/pipelines/importDialog/pipelineImportDialogCtrl',
    'app/components/pipelines/detailDialog/pipelineDetailDialogCtrl',
    'app/components/templates/detailDialog/templateDetailDialog',
    'app/components/instances/detailDialog/instanceDetailDialog'
], function (jQuery, jsonld,
             pipelineCanvasDirective,
             canvasPipelineFactory,
             executionProgressFactory,
             pipelineEditDirective,
             pipelineModelService,
             selectTemplateDialog,
             importPipelineDialog,
             pipelineDetailDialog,
             templateDetailDialog,
             instanceDetailDialog) {
    function controler($scope,
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
                       executionCanvas,
                       indexPage,
                       pipelineDesign
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
                // If true we are in edit mode else we are in executino mode.
                'edit': false,
                // True when execution is finished.
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
                'update': true,
                'hasWorkingData': true
            }
        };

        function onComponentDetail(cell, component) {
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            var comFacade = pipelineService.component;
            var templateIri = comFacade.getTemplateIri(component);
            var template = templateService.getTemplate(templateIri);
            $mdDialog.show({
                'controller': 'instance.detail.dialog',
                'templateUrl': 'app/components/instances/detailDialog/instanceDetailDialog.html',
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
        }

        function onCreateComponent(cell, component) {
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            var comFacade = pipelineService.component;
            var templateIri = comFacade.getTemplateIri(component);
            var template = templateService.getTemplate(templateIri);
            $mdDialog.show({
                'controller': 'template.detail.dialog',
                'templateUrl': 'app/components/templates/detailDialog/templateDetailDialog.html',
                'clickOutsideToClose': false,
                'fullscreen': useFullScreen,
                'locals': {
                    'component': component,
                    'template': template,
                    'pipeline': data.pipeline
                }
            }).then(function () {
                // Notify about change in the component as
                // inherited properties may changed.
                $scope.canvas.getPaper().trigger('lp:component:changed',
                    component['@id'], component);
            }, function () {
                // No action here.
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
                    'to': component['@id']
                }, false), function () {
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
                'templateUrl': 'app/components/templates/selectDialog/templateSelectDialog.html',
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
                // Add the configuration.
                const templateIri = jsonld.r.getIRI(newComponent.component,
                    "http://linkedpipes.com/ontology/template");
                templateService.fetchNewConfig(templateIri).then((config) => {
                    const configuration = jQuery.extend(true, [], config);
                    const configIri =
                        jsonld.r.getId(newComponent.component) +
                        '/configuration';
                    jsonld.r.setIRIs(newComponent.component,
                        "http://linkedpipes.com/ontology/configurationGraph",
                        configIri);
                    data.pipeline.model.graphs[configIri] = configuration;
                });
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

        function onEnableDisable(component) {
            var comFacade = pipelineService.component;
            if (comFacade.isDisabled(component)) {
                comFacade.setDisabled(component, false);
            } else {
                comFacade.setDisabled(component, true);
            }
            // Notify all about change in the component.
            $scope.canvas.getPaper().trigger('lp:component:changed',
                component['@id'], component);
        };

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
            const iri = data.pipeline.iri + '&templates=false&mappings=false'
            return $http.get(iri).then(function (response) {
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
            .then(infoService.fetch)
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

        // Switch to edit mode.
        function editMode() {
            $scope.data.tools.edit = true;
            //
            $scope.canvas.setInteractive(true);
            executionCanvas.setEnabled(false);
            $scope.pipelineEdit.setEnabled(true);
            // Update color.
            indexPage.color = '#2196F3';
        }

        function executionMode() {
            $scope.data.tools.edit = false;
            // For begginign we assume pipeline is running.
            $scope.data.tools.execFinished = false;
            //
            $scope.canvas.setInteractive(false);
            executionCanvas.setEnabled(true);
            $scope.pipelineEdit.setEnabled(false);
            // Update color.
            indexPage.color = '#FF9800';
        }

        //
        var readyComponents = 0;

        function executionHasWorkingDate() {
            return data.execution.model.hasWorkingData();
        }

        $scope.pipelineEdit.onLink = function () {

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
            $scope.pipelineEdit.API.onEnableDisable = onEnableDisable;
            $scope.pipelineEdit.API.mappingAvailable = function (component) {
                if (data.execution.iri === undefined) {
                    return;
                } else {
                    return executionCanvas.isMappingAvailable(
                        component['@id']);
                }
            };
            $scope.pipelineEdit.API.debugFromAvailable = function () {
                return executionHasWorkingDate();
            };
            $scope.pipelineEdit.API.createTemplate = function (component) {
                const templateIri = jsonld.r.getIRI(component,
                    'http://linkedpipes.com/ontology/template');
                return templateService.getSupportControl(
                    templateService.getTemplate(templateIri));
            };
            $scope.pipelineEdit.API.onCreateComponent = onCreateComponent;

            readyComponents++;
            initialize();
        };

        function initialize() {
            if (readyComponents !== 2) {
                return;
            }

            // Set mode based on the input.
            // TODO This should each component do on it own.
            if (data.execution.iri === undefined) {
                // Go to edit mode.
                editMode();
            } else {
                // Execution mode "running".
                executionMode();
            }
            // Update pipeline design information.
            pipelineDesign.update();
            // TODO replace with promise.
            templateService.load().then(function () {
                loadData();
            }, function (response) {
                statusService.deleteFailed({
                    'title': "Can't load templates pipeline.",
                    'response': response
                });
            });
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

            readyComponents++;
            initialize();
        });

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
         * Parameters:
         *  to - an IRI (as string) of component to run to
         *
         */
        var createExecuteConfiguration = function (parameters, withoutDebug) {
            var config = {
                '@id': '',
                '@type': 'http://etl.linkedpipes.com/ontology/ExecutionOptions'
            };

            if (withoutDebug) {
                config['http://linkedpipes.com/ontology/saveDebugData'] = false;
                config['http://linkedpipes.com/ontology/deleteWorkingData'] = true;
            } else {
                config['http://linkedpipes.com/ontology/saveDebugData'] = true;
                config['http://linkedpipes.com/ontology/deleteWorkingData'] = false;
            }

            // Run-to arguments.
            if (parameters['to']) {
                config['http://etl.linkedpipes.com/ontology/runTo'] =
                {'@id': parameters['to']}
            }

            // We will continue only if there is some mapping from
            // another execution.
            if (data.execution.iri === undefined ||
                !executionHasWorkingDate()) {
                // No information about execution or there are no working data.
                return config;
            }
            // Create object with mapping and reference it from
            // the configuration.
            var execution = {
                //'@id': '',
                'http://etl.linkedpipes.com/ontology/execution': {'@id': data.execution.iri},
                'http://etl.linkedpipes.com/ontology/mapping': []
            };
            config['http://etl.linkedpipes.com/ontology/executionMapping']
                = execution;

            var components = data.execution.model.getComponents();
            // Add components.
            for (var iri in components) {
                var component = components[iri];
                if (parameters['to'] === iri) {
                    // There is no mapping.
                    continue;
                }
                // Make sure we should (and can) use mapping and if so use it.
                if (data.execution.model.mapping.isUsedForExecution(component)) {
                    execution['http://etl.linkedpipes.com/ontology/mapping'].push({
                        //'@id': '',
                        'http://etl.linkedpipes.com/ontology/mappingSource': {'@id': iri},
                        'http://etl.linkedpipes.com/ontology/mappingTarget': {'@id': component['iri']}
                    });
                }
            }
            //
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
                executePipeline(createExecuteConfiguration({}, false),
                    function () {
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
                        'pipeline': jQuery.extend(true, {}, jsonld)
                    }
                }
            }).then(function (result) {
                // No action here.
            }, function () {
                // No action here.
            });
        };

        $scope.onExecuteWithoutDebugData = function () {
            storePipeline(data.pipeline.iri, true, function () {
                executePipeline(createExecuteConfiguration({}, true),
                    function () {
                        $location.path('/executions').search({});
                    });
            });
        };

        $scope.onCopy = function () {
            //
            var jsonld = pipelineCanvas.storePipeline();
            var data = new FormData();
            data.append('pipeline', new Blob([JSON.stringify(jsonld)], {
                type: "application/ld+json"
            }), 'pipeline.jsonld');
            //
            var options = {
                '@id': 'http://localhost/options',
                '@type': 'http://linkedpipes.com/ontology/UpdateOptions',
                'http://etl.linkedpipes.com/ontology/local': true
            };
            data.append('options', new Blob([JSON.stringify(options)], {
                type: "application/ld+json"
            }), 'options.jsonld');
            //
            var config = {
                'transformRequest': angular.identity,
                'headers': {
                    // By this angular add Content-Type itself.
                    'Content-Type': undefined,
                    'accept': 'application/ld+json'

                }
            }
            $http.post('./resources/pipelines', data, config)
            .success(function (data, status, headers) {
                statusService.success({
                    'title': 'Pipeline has been successfully copied.'
                });
                // The response is a reference.
                // TODO Use JSONLD service to get the value !!
                var newPipelineUri = data[0]['@graph'][0]['@id'];
                //
                statusService.success({
                    'title': 'Pipeline has been successfully copied.'
                });
                $location.path('/pipelines/edit/canvas').search(
                    {'pipeline': newPipelineUri});
            })
            .error(function (data, status, headers) {
                statusService.postFailed({
                    'title': "Can't create new pipeline.",
                    'response': data
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

        /**
         * For all loaders on the pipeline update the activity state
         * (disable/enable).
         *
         * @param disable
         */
        function setLoadersDisabled(disable) {
            const comFacade = pipelineService.component;
            const components = pipelineService.getComponents(data.pipeline.model);
            for (let i = 0; i < components.length; ++i) {
                const component = components[i];
                // Check for loader type.
                const templateIri = comFacade.getTemplateIri(component);
                const template = templateService.getJarTemplate(templateIri);
                if (template.type.indexOf("http://etl.linkedpipes.com/ontology/component/type/Loader") === -1 &&
                    template.type.indexOf("http://etl.linkedpipes.com/ontology/component/type/Executor") === -1) {
                    continue
                }
                // Check state.
                if (comFacade.isDisabled(component) === disable) {
                    continue;
                }
                //
                comFacade.setDisabled(component, disable);
                $scope.canvas.getPaper().trigger('lp:component:changed',
                    component['@id'], component);
            }
        }

        $scope.onDisableAllLoaders = function() {
            setLoadersDisabled(true);
        };

        $scope.onEnableAllLoaders = function() {
            setLoadersDisabled(false);
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
        'template.service',
        'services.status',
        'service.refresh',
        'services.jsonld',
        'service.info',
        'components.pipelines.services.model',
        'models.execution',
        'canvas.pipeline',
        'canvas.execution',
        'indexPage',
        'service.pipelineDesign'
    ];

    return function init(app) {
        app.controller('components.pipeline.canvas.view', controler);
        pipelineCanvasDirective(app);
        canvasPipelineFactory(app);
        executionProgressFactory(app);
        pipelineEditDirective(app);
        pipelineModelService(app);
        selectTemplateDialog(app);
        importPipelineDialog(app);
        pipelineDetailDialog(app);
        templateDetailDialog(app);
        instanceDetailDialog(app);
    };

});

