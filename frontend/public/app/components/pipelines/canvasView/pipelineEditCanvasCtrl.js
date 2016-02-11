define([
    'jquery',
    'angular',
    'app/components/pipelines/detailDialog/pipelineDetailDialogCtrl',
    'app/components/pipelines/canvas/pipelineCanvasDirective',
    'app/components/pipelines/pipelineModelService',
    'app/components/templates/templatesRepository',
    'app/components/pipelines/configurationDialog/configurationDialogCtrl',
    'app/components/templates/selectDialog/selectTemplateDialogCtrl'
], function ($, angular, pipelineDetailDialog, pipelineCanvasDirective, pipelineServiceModule, templatesRepositoryModule, configurationDialogCtrlModule, selectTemplateDialogModule) {
    function controller($scope, $http, $location, $routeParams, $timeout, $mdDialog, $mdMedia, pipelineModel,
            templatesRepository, statusService) {

        $scope.data = {
            'uri': $routeParams.uri,
            'model': {},
            'definition': {},
            /**
             * Label visible to the user.
             */
            'label': '',
            /**
             * Drectory that can be used to convert id to model.
             */
            'idToModel': {}
        };

        $scope.status = {
            'menuVisible': false,
            'moving': false,
            'selected': '',
            'noApply': false,
            'prerequisity': {
                'active': false,
                'source': ''
            },
            'dialogOpened': false
        };

        //
        // Conneciton on canvas API.
        //

        $scope.canvasApi = {};

        $scope.canvasApi.onClick = function (id) {
            if ($scope.status.prerequisity.active) {
                // Add prerequisity connection.
                $scope.status.prerequisity.active = false;
                // This will cause call of onNewConnection that adds the connection to the model.
                $scope.canvasApi.addConnection($scope.status.prerequisity.source, null, id, null, [], 'control');
            }
        };

        $scope.canvasApi.onEmptyClick = function (x, y) {
            // Check for opened dialog.
            if ($scope.status.dialogOpened) {
                return;
            }
            if ($scope.status.prerequisity.active) {
                $scope.status.prerequisity.active = false;
                return;
            }
            // Open dialog for new component.
            $scope.status.dialogOpened = true;
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $mdDialog.show({
                controller: 'components.templates.select.dialog',
                templateUrl: 'app/components/templates/selectDialog/selectTemplateDialogView.html',
                parent: angular.element(document.body),
                hasBackdrop: false,
                clickOutsideToClose: true,
                fullscreen: useFullScreen
            }).then(function (template) {
                // Insert new component.
                var component = pipelineModel.createComponent($scope.data.model, template);
                pipelineModel.setComponentPosition(component, x, y);
                var id = $scope.canvasApi.addComponent(component, template);
                pipelineModel.setComponentUriFromId($scope.data.model, component, id);
                $scope.data.idToModel[id] = component;
                //
                $scope.status.dialogOpened = false;
            }, function () {
                $scope.status.dialogOpened = false;
            });

        };

        $scope.canvasApi.onDoubleClick = function (id) {
            $scope.onEditComponent(id);
        };

        $scope.canvasApi.onNewConnection = function (type, id, source, sourcePort, target, targetPort) {
            var connection;
            if (type === 'control') {
                connection = pipelineModel.createRunAfter($scope.data.model);
            } else if (type === 'link') {
                connection = pipelineModel.createConnection($scope.data.model);
            } else {
                console.log('Unknown connection type: ', type);
                return;
            }
            pipelineModel.setConnectionUriFromId($scope.data.model, connection, id);
            $scope.data.idToModel[id] = connection;
            // Update connection ie. set source and target.
            $scope.canvasApi.onConnectionChange(id, source, sourcePort, target, targetPort);
        };

        $scope.canvasApi.onPositionChange = function (id, x, y) {
            var model = $scope.data.idToModel[id];
            pipelineModel.setComponentPosition(model, x, y);
        };

        $scope.canvasApi.onConnectionChange = function (id, source, sourcePort, target, targetPort) {
            var model = $scope.data.idToModel[id];
            if (source) {
                pipelineModel.setConnectionSource(model, $scope.data.idToModel[source], sourcePort);
            }
            if (target) {
                pipelineModel.setConnectionTarget(model, $scope.data.idToModel[target], targetPort);
            }
        };

        $scope.canvasApi.onDelete = function (id) {
            var model = $scope.data.idToModel[id];
            delete $scope.data.idToModel[id];
            pipelineModel.removeResource($scope.data.model, model['@id'], false);
        };

        $scope.canvasApi.onMoveSelected = function (id, x, y) {
            var canvasPosition = $('#canvas').position();
            var menu = $('#componentMenu');
            menu.css('left', x + canvasPosition.left);
            menu.css('top', y + canvasPosition.top);
        };

        $scope.canvasApi.onUpdateSelection = function (id) {
            if (id) {
                var boundingBox = $scope.canvasApi.getScreenBoundingBox(id);
                //
                $scope.canvasApi.onMoveSelected(id, boundingBox.x, boundingBox.y);
                //
                var bottomLine = $('#componentMenu.bottomLine');
                bottomLine.css('top', boundingBox.height);
                //
                $scope.status.menuVisible = true;
                $scope.status.selected = id;
            } else {
                $scope.status.menuVisible = false;
                $scope.status.selected = '';
            }
            // We need to notify angular about change.
            if (!$scope.status.noApply) {
                $scope.$apply();
            }
        };

        var updateLabel = function () {
            $scope.data.label = $scope.data.definition['http://www.w3.org/2004/02/skos/core#prefLabel'];
            if (!$scope.data.label || $scope.data.label === '') {
                $scope.data.label = 'Click here to enter pipeline name.';
            }
        };

        /**
         * Load pipeline in data.model into the canvas.
         */
        var pipelineToCanvas = function () {
            $scope.status.noApply = true;
            $scope.canvasApi.loadStart();
            var uriToId = {}; // Used as local cache to quickly resolve references.

            pipelineModel.getComponents($scope.data.model).forEach(function (component) {
                var templateUri = pipelineModel.getComponentTemplateUri(component);
                var template = templatesRepository.getTemplate(templateUri);
                //
                var id = $scope.canvasApi.addComponent(component, template);
                $scope.data.idToModel[id] = component;
                //
                uriToId[component['@id']] = id;
            });
            pipelineModel.getConnections($scope.data.model).forEach(function (connection) {
                var source = uriToId[connection['http://linkedpipes.com/ontology/sourceComponent']['@id']];
                var sourcePort = connection['http://linkedpipes.com/ontology/sourceBinding'];
                var target = uriToId[connection['http://linkedpipes.com/ontology/targetComponent']['@id']];
                var targetPort = connection['http://linkedpipes.com/ontology/targetBinding'];
                var vertices = pipelineModel.getVertices($scope.data.model, connection);
                var id = $scope.canvasApi.addConnection(source, sourcePort, target, targetPort, vertices, 'link');
                $scope.data.idToModel[id] = connection;
            });
            pipelineModel.getRunAfter($scope.data.model).forEach(function (connection) {
                var source = uriToId[connection['http://linkedpipes.com/ontology/sourceComponent']['@id']];
                var target = uriToId[connection['http://linkedpipes.com/ontology/targetComponent']['@id']];
                var vertices = pipelineModel.getVertices($scope.data.model, connection);
                var id = $scope.canvasApi.addConnection(source, null, target, null, vertices, 'control');
                $scope.data.idToModel[id] = connection;
            });
            $scope.canvasApi.loadEnd();
            $scope.status.noApply = false;
        };

        /**
         * Synchronize model with canvas.
         */
        var canvasToPipeline = function () {
            // Set vertices.
            $scope.canvasApi.getConnections().forEach(function (id) {
                var connection = $scope.data.idToModel[id];
                var vertices = $scope.canvasApi.getVertices(id);
                if (vertices) {
                    // Convert vertivies to positions.
                    var positions = [];
                    vertices.forEach(function (item) {
                        positions.push(pipelineModel.createVertex(item['x'], item['y']));
                    });
                    pipelineModel.setVertices($scope.data.model, connection, positions);
                } else {
                    pipelineModel.removeVertices($scope.data.model, connection);
                }
            });
        };

        var loadPipeline = function () {
            $http.get($scope.data.uri).then(function (response) {
                $scope.data.model = pipelineModel.modelFromJsonLd(response.data);
                $scope.data.definition = pipelineModel.getPipelineDefinition($scope.data.model);
                // Update label.
                updateLabel();
                // Load pipelne.
                pipelineToCanvas();
            }, function (response) {
                statusService.getFailed({
                    'title': "Can't load the pipeline.",
                    'response': response
                });
            });
        };

        var storePipeline = function (uri, unchecked, onSucess) {
            //
            canvasToPipeline();
            //
            pipelineModel.reorganize($scope.data.model);
            var jsonld = pipelineModel.modelToJsonLd($scope.data.model);
            $http({
                method: 'PUT',
                url: uri,
                params: {'unchecked': unchecked},
                headers: {'Content-Type': 'application/json'},
                data: jsonld
            }).then(function (response) {
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
         *
         * @return Unique componnet id, that is not presented in $scope.data.idToModel.
         */
        var uuid = function () {

            // credit: http://stackoverflow.com/posts/2117523/revisions

            var result = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
                var r = Math.random() * 16 | 0;
                var v = c === 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
            // Test for unique solution.
            if ($scope.data.idToModel[result]) {
                return uuid();
            } else {
                return result;
            }
        };

        $scope.onEditComponent = function (id) {
            if (!id) {
                id = $scope.status.selected;
            }
            //
            var component = $scope.data.idToModel[id];
            var templateUri = pipelineModel.getComponentTemplateUri(component);
            var template = templatesRepository.getTemplate(templateUri);
            //
            templatesRepository.fetchTemplateConfiguration(template, function () {
                $scope.status.dialogOpened = true;
                $mdDialog.show({
                    controller: 'components.pipelines.configuration.dialog',
                    templateUrl: 'app/components/pipelines/configurationDialog/configurationDialogView.html',
                    parent: angular.element(document.body),
                    clickOutsideToClose: false,
                    fullscreen: true,
                    locals: {
                        component: component,
                        template: template,
                        data: $scope.data
                    }
                }).then(function () {
                    // Update component.
                    $scope.canvasApi.updateComponent(id, component, template);
                    $scope.status.dialogOpened = false;
                }, function () {
                    $scope.status.dialogOpened = false;
                });
            });
        };

        $scope.onDeleteComponent = function () {
            $scope.status.noApply = true;
            $scope.canvasApi.deleteComponent($scope.status.selected);
            $scope.status.noApply = false;
        };

        $scope.onCopyComponent = function () {
            var model = $scope.data.idToModel[$scope.status.selected];
            var component = pipelineModel.cloneComponent($scope.data.model, model, uuid());
            //
            var templateUri = pipelineModel.getComponentTemplateUri(model);
            var template = templatesRepository.getTemplate(templateUri);
            //
            var id = $scope.canvasApi.addComponent(component, template);
            $scope.data.idToModel[id] = component;
        };

        $scope.onPrerequisityComponent = function () {
            $scope.status.prerequisity.active = true;
            $scope.status.prerequisity.source = $scope.status.selected;
        };

        /**
         * Show pipeline detail dialog.
         *
         * @param $event
         */
        $scope.onDetail = function ($event) {
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $scope.status.dialogOpened = true;
            $mdDialog.show({
                controller: 'components.pipelines.detail.dialog',
                templateUrl: 'app/components/pipelines/detailDialog/pipelineDetailDialogView.html',
                parent: angular.element(document.body),
                targetEvent: $event,
                clickOutsideToClose: false,
                fullscreen: useFullScreen,
                locals: {
                    data: $scope.data
                }
            }).then(function () {
                // On confirmation update.
                updateLabel();
                $scope.status.dialogOpened = false;
            }, function() {
                $scope.status.dialogOpened = false;
            });
        };

        $scope.onSave = function () {
            storePipeline($scope.data.uri, true);
        };

        $scope.onSaveAndClose = function () {
            storePipeline($scope.data.uri, true, function () {
                $location.path('/pipelines').search({});
            });
        };

        $scope.onExecute = function () {
            storePipeline($scope.data.uri, true, function () {
                $http.post('/api/v1/execute?uri=' + $scope.data.uri).then(function (response) {
                    $location.path('/executions').search({});
                }, function (response) {
                    statusService.postFailed({
                        'title': "Can't start the execution.",
                        'response': response
                    });
                });
            });
        };

        $scope.onClose = function () {
            $location.path('/pipelines').search({});
        };

        $scope.onReload = function () {
            loadPipeline();
        };

        $scope.onCopy = function () {
            var id = 'created-' + (new Date()).getTime();
            $http.post('/resources/pipelines/' + id).then(function (response) {
                var newPipelineUri = response.data.uri;
                // Save pipeline under new URI.
                storePipeline(newPipelineUri, false, function () {
                    statusService.success({
                        'title': 'Pipeline has been successfully copied.'
                    });
                    //
                    $location.path('/pipelines/edit/canvas').search({'uri': newPipelineUri});
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
                $http({method: 'DELETE', url: $scope.data.uri}).then(function (response) {
                    $location.path('/pipelines').search({});
                }, function (response) {
                    statusService.deleteFailed({
                        'title': "Can't delete the pipeline.",
                        'response': response
                    });
                });
            });
        };

        var initialize = function () {
            templatesRepository.load(function () {
                loadPipeline();
            }, function (response) {
                statusService.deleteFailed({
                    'title': "Can't load components.",
                    'response': response
                });
            });
        };

        // Make sure directives are loaded first.
        $timeout(initialize, 0);
    }

    controller.$inject = ['$scope', '$http', '$location', '$routeParams', '$timeout', '$mdDialog', '$mdMedia',
        'components.pipelines.services.model', 'components.templates.services.repository', 'services.status'];

    // Function for depedency registration.
    function init(app) {
        pipelineDetailDialog(app);
        pipelineCanvasDirective(app);
        pipelineServiceModule(app);
        templatesRepositoryModule(app);
        configurationDialogCtrlModule(app);
        selectTemplateDialogModule(app);

        app.controller('components.pipelines.edit.canvas', controller);
    }
    return init;
});
