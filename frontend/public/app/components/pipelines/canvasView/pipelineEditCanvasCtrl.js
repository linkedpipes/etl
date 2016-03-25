define([
    'jquery',
    'angular',
    'app/components/pipelines/detailDialog/pipelineDetailDialogCtrl',
    'app/components/pipelines/canvas/pipelineCanvasDirective',
    'app/components/pipelines/pipelineModelService',
    'app/components/templates/templatesRepository',
    'app/components/pipelines/configurationDialog/configurationDialogCtrl',
    'app/components/templates/selectDialog/selectTemplateDialogCtrl'
], function ($, angular, pipelineDetailDialog, pipelineCanvasDirective, pipelineServiceModule, templatesRepositoryModule
        , configurationDialogCtrlModule, selectTemplateDialogModule) {
    function controller($scope, $http, $location, $routeParams, $timeout, $mdDialog, $mdMedia, pipelineModel,
            templatesRepository, statusService) {

        $scope.data = {
            'uri': $routeParams.pipeline,
            'model': {},
            'definition': {},
            /**
             * Label visible to the user.
             */
            'label': '',
            /**
             * Drectory that can be used to convert ID to model.
             */
            'idToModel': {},
            /**
             * Dictionary used to convert URI to ID.
             */
            'uriToId': {}
        };

        $scope.status = {
            'menuVisible': false,
            'moving': false,
            'selected': '',
            'noApply': false,
            'prerequisite': {
                'active': false,
                'source': ''
            },
            'dialogOpened': false,
            'mappingVisible': false
        };

        var data = {
            'execution': {
                'id': null,
                'mapping': {}
            }
        };

        //
        // Connection on canvas API.
        //

        $scope.canvasApi = {};

        $scope.canvasApi.onClick = function (id) {
            // Check if we should add prerequisite connection.
            if ($scope.status.prerequisite.active) {
                $scope.status.prerequisite.active = false;
                // This will cause call of onNewConnection that adds the connection to the model.
                $scope.canvasApi.addConnection($scope.status.prerequisite.source, null, id, null, [], 'control');
            }
        };

        $scope.canvasApi.onEmptyClick = function (x, y) {
            // Check for opened dialog.
            if ($scope.status.dialogOpened) {
                return;
            }
            if ($scope.status.prerequisite.active) {
                $scope.status.prerequisite.active = false;
                return;
            }
            selectComponent({}, function (template) {
                insertComponent(template, x, y);
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
            // Remove component mapping if is presented.
            disableMappingOnChange(id, false);
            if (data.execution.mapping && data.execution.mapping[model['@id']]) {
                delete data.execution.mapping[model['@id']];
            }
            // Remove references from maps.
            delete $scope.data.idToModel[id];
            delete $scope.data.uriToId[model['@id']];
            // Remove from pipeline definition.
            pipelineModel.removeResource($scope.data.model, model['@id'], false);
        };

        $scope.canvasApi.onConnectionToEmpty = function (id, x, y) {
            // Get information about the object.
            var connection = $scope.data.idToModel[id];
            // Remvoe it.
            $scope.canvasApi.onDelete(id);
            // Prepare filter.
            var sourceBinding = connection['http://linkedpipes.com/ontology/sourceBinding'];
            var sourceUri = connection['http://linkedpipes.com/ontology/sourceComponent']['@id'];
            var sourceComponent = pipelineModel.getResource($scope.data.model, sourceUri);
            var templateUri = pipelineModel.getComponentTemplateUri(sourceComponent);
            var filter = {
                'source': {
                    'binding': sourceBinding,
                    'templateUri': templateUri
                }
            };
            selectComponent(filter, function (template) {
                // Insert component and add connection.
                var source = $scope.data.uriToId[sourceUri];
                var sourcePort = sourceBinding;
                var target = insertComponent(template, x, y);
                var targetPort = template['portBinding'];
                $scope.canvasApi.addConnection(source, sourcePort, target, targetPort, [], 'link');
            });
        };

        $scope.canvasApi.onMoveSelected = function (id, x, y, width, height) {
            var canvasPosition = $('#canvas').position();
            var menu = $('#componentMenu');
            menu.css('left', x + canvasPosition.left);
            menu.css('top', y + canvasPosition.top);
            if (width && height) {
                var bottomMenu = menu.find('.bottomLine');
                bottomMenu.css('top', (height - 45) + 'px');
            }
        };

        $scope.canvasApi.onUpdateSelection = function (id) {
            if (id) {
                var component = $scope.data.idToModel[id];
                if (!component) {
                    console.log('No component selected!');
                    return;
                }
                var boundingBox = $scope.canvasApi.getScreenBoundingBox(id);
                // Move the menu to the right position and also
                // initialize size.
                $scope.canvasApi.onMoveSelected(id, boundingBox.x, boundingBox.y,
                        boundingBox.width, boundingBox.height);
                //
                var menu = $('#componentMenu');
                menu.find('bottomLine').css('top', boundingBox.height);
                // Check for debugging.
                if (data.execution.mapping[component['@id']]) {
                    var mapping = data.execution.mapping[component['@id']];
                    // available, enabled, changed
                    if (mapping.available && !mapping.changed) {
                        $scope.status.mappingVisible = true;
                    } else {
                        $scope.status.mappingVisible = false;
                    }
                } else {
                    $scope.status.mappingVisible = false;
                }
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

        var selectComponent = function (filter, onSucess) {
            // Open dialog for new component.
            $scope.status.dialogOpened = true;
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $mdDialog.show({
                controller: 'components.templates.select.dialog',
                templateUrl: 'app/components/templates/selectDialog/selectTemplateDialogView.html',
                parent: angular.element(document.body),
                hasBackdrop: false,
                clickOutsideToClose: true,
                fullscreen: useFullScreen,
                locals: {
                    'filter': filter
                }
            }).then(function (result) {
                if (onSucess) {
                    onSucess(result);
                }
                //
                $scope.status.dialogOpened = false;
            }, function () {
                $scope.status.dialogOpened = false;
            });
        };

        /**
         *
         * @param {type} template
         * @param {type} x
         * @param {type} y
         * @return Component model ID in the, key to idToModel.
         */
        var insertComponent = function (template, x, y) {
            var component = pipelineModel.createComponent($scope.data.model, template['component']);
            pipelineModel.setComponentPosition(component, x, y);
            var id = $scope.canvasApi.addComponent(component, template['component']);
            pipelineModel.setComponentUriFromId($scope.data.model, component, id);
            //
            $scope.data.idToModel[id] = component;
            $scope.data.uriToId[component['@id']] = id;
            return id;
        };

        /**
         * Load pipeline name from definition.
         */
        var updateLabel = function () {
            $scope.data.label = $scope.data.definition['http://www.w3.org/2004/02/skos/core#prefLabel'];
            if (!$scope.data.label || $scope.data.label === '') {
                $scope.data.label = $scope.data.definition['@id'];
            }
        };

        /**
         * Disable mapping and set mapping as unavailbale.
         */
        var disableMappingOnChange = function (uri) {
            var mapping = data.execution.mapping[uri];
            if (!mapping) {
                return;
            }
            // Hyde button for the user.
            $scope.status.mappingVisible = false;
            //
            mapping.changed = true;
            mapping.enabled = false;
            // Update visuals.
            setMappingVisual(mapping);
            // Propagation.
            var connections = pipelineModel.getConnections($scope.data.model);
            connections.forEach(function (connection) {
                if (connection['http://linkedpipes.com/ontology/sourceComponent']['@id'] === uri) {
                    disableMappingOnChange(connection['http://linkedpipes.com/ontology/targetComponent']['@id']);
                }
            });
        };

        /**
         * Dissable mapping for the component.
         */
        var disableComponentMapping = function (uri) {
            var mapping = data.execution.mapping[uri];
            if (!mapping) {
                return;
            }
            if (!mapping.enabled) {
                // Already disabled.
                return;
            }
            //
            mapping.enabled = false;
            // Update visuals.
            setMappingVisual(mapping);
            // Propagation.
            var connections = pipelineModel.getConnections($scope.data.model);
            connections.forEach(function (connection) {
                if (connection['http://linkedpipes.com/ontology/sourceComponent']['@id'] === uri) {
                    disableComponentMapping(connection['http://linkedpipes.com/ontology/targetComponent']['@id']);
                }
            });
        };

        /**
         * If suitable enable component mapping.
         */
        var enableComponentMapping = function (uri) {
            var mapping = data.execution.mapping[uri];
            if (!mapping) {
                return;
            }
            if (mapping.enabled) {
                // Already enabled.
                return;
            }
            //
            if (!mapping.changed) {
                mapping.enabled = true;
            }
            // Update visuals.
            setMappingVisual(mapping);
            // Propagation.
            var connections = pipelineModel.getConnections($scope.data.model);
            connections.forEach(function (connection) {
                if (connection['http://linkedpipes.com/ontology/targetComponent']['@id'] === uri) {
                    enableComponentMapping(connection['http://linkedpipes.com/ontology/sourceComponent']['@id']);
                }
            });
        };

        /**
         * Load pipeline in data.model into the canvas.
         */
        var pipelineToCanvas = function () {
            $scope.status.noApply = true;
            $scope.canvasApi.loadStart();
            var uriToId = {};
            // We also need to deterine the left top corner.
            var leftTopX = null;
            var leftTopY = null;
            //
            pipelineModel.getComponents($scope.data.model).forEach(function (component) {
                var templateUri = pipelineModel.getComponentTemplateUri(component);
                var template = templatesRepository.getTemplate(templateUri);
                //
                var id = $scope.canvasApi.addComponent(component, template);
                $scope.data.idToModel[id] = component;
                // Store position.
                if (leftTopX === null) {
                    leftTopX = component['http://linkedpipes.com/ontology/x'];
                    leftTopY = component['http://linkedpipes.com/ontology/y'];
                } else {
                    leftTopX = Math.min(leftTopX,
                            component['http://linkedpipes.com/ontology/x']);
                    leftTopY = Math.min(leftTopY,
                            component['http://linkedpipes.com/ontology/y']);
                }
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
            // We need to move the screen in posite direction
            // to get to the position. Also substract something to get
            // the component inside the view.
            $scope.canvasApi.setScreen(- (leftTopX - 50), -(leftTopY - 50));
            //
            $scope.data.uriToId = uriToId;
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

        var loadPipeline = function (onSucess) {
            $http.get($scope.data.uri).then(function (response) {
                $scope.data.model = pipelineModel.modelFromJsonLd(response.data);
                $scope.data.definition = pipelineModel.getPipelineDefinition($scope.data.model);
                // Update label.
                updateLabel();
                // Load pipelne.
                pipelineToCanvas();
                //
                if (onSucess) {
                    onSucess();
                }
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

        var executePipeline = function (configuration, onSucess) {
            $http.post('/api/v1/execute?uri=' + $scope.data.uri, configuration).then(function (response) {
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

        /**
         * Create a configuration object for executePipeline function.
         */
        var createExecuteConfiguration = function (parametr) {
            var requestData = {
                'execution': {},
                'mapping': []
            };
            // Run-to arguments.
            if (parametr['to']) {
                requestData.execution.to = parametr['to'];
            }
            // Run-from arguments.
            if (data.execution.mapping) {
                var components = {};
                // Add components.
                for (var uri in data.execution.mapping) {
                    var item = data.execution.mapping[uri];
                    // We can't map component we are debugging to.
                    if (parametr['to'] === uri) {
                        continue;
                    }
                    // Make sure we should (and can) use mapping and if so use it.
                    if (item.available && item.enabled && !item.changed) {
                        components[uri] = item['target'];
                    }
                }
                //
                if (data.execution.id !== null) {
                    requestData.mapping.push({
                        'id': data.execution.id,
                        'components': components
                    });
                }
            }
            return requestData;
        };

        /**
         * Set visual appeareace for component with given id with given status.
         */
        var setMappingVisual = function (mapping) {
            // available, enabled, changed
            switch (mapping['status']) {
                case 'QUEUED':
                case 'RUNNING':
                case 'SKIPPED':
                    break;
                case 'FINISHED':
                    if (mapping.enabled) {
                        $scope.canvasApi.updateComponentVisual(mapping['viewId'],
                                {'stroke': {'color': '#388E3C', 'width': 5}});
                    } else {
                        $scope.canvasApi.updateComponentVisual(mapping['viewId'],
                                {'stroke': {'color': 'gray', 'width': 3}});
                    }
                    break;
                case 'MAPPED':
                    if (mapping.enabled) {
                        $scope.canvasApi.updateComponentVisual(mapping['viewId'],
                                {'stroke': {'color': '#00796B', 'width': 5}});
                    } else {
                        $scope.canvasApi.updateComponentVisual(mapping['viewId'],
                                {'stroke': {'color': 'gray', 'width': 3}});
                    }
                    break;
                case 'FAILED':
                    $scope.canvasApi.updateComponentVisual(mapping['viewId'],
                            {'stroke': {'color': 'red', 'width': 5}});
                default:
                    console.log('Unknwon status:', mapping['status']);
                    return;
            }
        };

        /**
         * Load and display information from execution, should be used only if pipeline was filly loaded.
         */
        var loadExecution = function () {
            if (!$routeParams.execution) {
                return;
            }
            $http.get($routeParams.execution).then(function (response) {
                var execution = response.data.payload;
                //
                data.execution = {
                    'id': execution['id'],
                    'instance': execution,
                    'mapping': {}
                };
                //
                for (var uri in execution.components) {
                    var component = execution.components[uri];
                    if ($scope.data.uriToId[uri]) {
                        //
                        var mapping = {
                            'viewId': $scope.data.uriToId[uri],
                            'target': uri,
                            'status': component['status'],
                            'available': component['status'] === 'FINISHED' || component['status'] === 'MAPPED',
                            'enabled': true,
                            'changed': false
                        };
                        setMappingVisual(mapping);
                        data.execution.mapping[uri] = mapping;
                    }
                }
            }, function (response) {
                statusService.putFailed({
                    'title': "Can't load execution.",
                    'response': response
                });
            });
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
                    // Invalidate mapping if presented as we want to disable mapping,
                    // if a component has changed.
                    disableMappingOnChange(component['@id']);
                    // Update component.
                    $scope.canvasApi.updateComponent(id, component, template);
                    // Update size and position of the selection menu.
                    var boundingBox = $scope.canvasApi.getScreenBoundingBox(id);
                    $scope.canvasApi.onMoveSelected(id, boundingBox.x, boundingBox.y,
                            boundingBox.width, boundingBox.height);
                    //
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

        $scope.onDebugToComponent = function () {
            var component = $scope.data.idToModel[$scope.status.selected];
            if (!component) {
                console.log('No component selected!');
                return;
            }
            //
            storePipeline($scope.data.uri, true, function () {
                executePipeline(createExecuteConfiguration({'to': component['@id']}), function () {
                    $location.path('/executions').search({});
                });
            });
        };

        $scope.onMappingComponent = function () {
            var id = $scope.status.selected;
            var component = $scope.data.idToModel[id];
            if (!component && data.execution.mapping[component['@id']]) {
                console.log('No component selected!');
                return;
            }
            //
            var mapping = data.execution.mapping[component['@id']];
            if (!mapping.available) {
                return;
            }
            if (mapping.enabled) {
                disableComponentMapping(component['@id']);
            } else {
                enableComponentMapping(component['@id']);
            }
        };

        $scope.onPrerequisiteComponent = function () {
            $scope.status.prerequisite.active = true;
            $scope.status.prerequisite.source = $scope.status.selected;
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
            }, function () {
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
                executePipeline(createExecuteConfiguration({}), function () {
                    $location.path('/executions').search({});
                });
            });
        };

        $scope.onClose = function () {
            $location.path('/pipelines').search({});
        };

        $scope.onReload = function () {
            loadPipeline(loadExecution);
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
                    $location.path('/pipelines/edit/canvas').search({'pipeline': newPipelineUri});
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

        $scope.onOpenMenu = function ($mdOpenMenu, event) {
            $mdOpenMenu(event);
        };

        var initialize = function () {
            templatesRepository.load(function () {
                loadPipeline(loadExecution);
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
