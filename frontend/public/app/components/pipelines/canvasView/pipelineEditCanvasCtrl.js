define([
    'jquery',
    'angular',
    'app/components/pipelines/detailDialog/pipelineDetailDialogCtrl',
    'app/components/pipelines/canvas/pipelineCanvasDirective',
    'app/components/pipelines/pipelineModelService',
    'app/components/templates/templatesRepository',
    'app/components/pipelines/configurationDialog/configurationDialogCtrl',
    'app/components/templates/selectDialog/selectTemplateDialogCtrl'
], function ($, angular, pipelineDetailDialog, pipelineCanvasDirective
        , pipelineServiceModule, templatesRepositoryModule
        , configurationDialogCtrlModule, selectTemplateDialogModule) {

    function controller($scope, $http, $location, $routeParams, $timeout,
            $mdDialog, $mdMedia, pipelineModel, templates, statusService,
            jsonldService) {

        var jsonld = jsonldService.jsonld();

        var pplFacade = pipelineModel;
        var conFacade = pipelineModel.connection;
        var comFacade = pipelineModel.component;

        $scope.data = {
            'iri': $routeParams.pipeline,
            'model': {},
            'definition': {},
            /**
             * Label visible to the user.
             */
            'label': '',
            /**
             * Dictionary that can be used to convert ID to model.
             */
            'idToModel': {},
            /**
             * Dictionary used to convert URI to ID.
             */
            'iriToId': {}
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

        $scope.canvasApi = {};

        $scope.canvasApi.onClick = function (id) {
            // Check if we should add prerequisite connection.
            if ($scope.status.prerequisite.active) {
                $scope.status.prerequisite.active = false;
                // This will cause call of onNewConnection that adds
                // the connection to the model.
                $scope.canvasApi.addConnection(
                        $scope.status.prerequisite.source, null, id, null, [],
                        'control');
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

        $scope.canvasApi.onNewConnection = function (type, id, source,
                sourcePort, target, targetPort) {
            var connection;
            if (type === 'control') {
                connection = conFacade.createRunAfter($scope.data.model);
            } else if (type === 'link') {
                connection = conFacade.createConnection($scope.data.model);
            } else {
                console.error('Unknown connection type: ', type);
                return;
            }
            conFacade.setIriFromId($scope.data.model, connection, id);
            $scope.data.idToModel[id] = connection;
            // Update connection ie. set source and target.
            $scope.canvasApi.onConnectionChange(id, source, sourcePort, target,
                    targetPort);
        };

        $scope.canvasApi.onPositionChange = function (id, x, y) {
            comFacade.setPosition($scope.data.idToModel[id], x, y);
        };

        $scope.canvasApi.onConnectionChange = function (id, source, sourcePort,
                target, targetPort) {
            var connection = $scope.data.idToModel[id];
            if (source) {
                conFacade.setSource(connection, $scope.data.idToModel[source],
                        sourcePort);
            }
            if (target) {
                conFacade.setTarget(connection, $scope.data.idToModel[target],
                        targetPort);
            }
        };

        $scope.canvasApi.onDelete = function (id) {
            var model = $scope.data.idToModel[id];
            // Remove component mapping if is presented.
            disableMappingOnChange(id, false);
            if (data.execution.mapping) {
                delete data.execution.mapping[model['@id']];
            }
            // Remove references from maps.
            delete $scope.data.idToModel[id];
            delete $scope.data.iriToId[model['@id']];
            // Remove from pipeline definition.
            pplFacade.delete($scope.data.model, model['@id'], false);
        };

        $scope.canvasApi.onConnectionToEmpty = function (id, x, y) {
            // Get information about the object.
            var connection = $scope.data.idToModel[id];
            // Remvoe it.
            $scope.canvasApi.onDelete(id);
            // Prepare filter.
            var sourceBinding = conFacade.getSourceBinding(connection);
            var sourceUri = conFacade.getSource(connection);
            var sourceComponent = pipelineModel.getResource(
                    $scope.data.model, sourceUri);
            var templateIri = comFacade.getTemplateIri(sourceComponent);
            var filter = {
                'source': {
                    'binding': sourceBinding,
                    'templateIri': templateIri
                }
            };
            // Let user select component.
            selectComponent(filter, function (template) {
                // Insert component and add connection.
                var source = $scope.data.iriToId[sourceUri];
                var sourcePort = sourceBinding;
                var target = insertComponent(template, x, y);
                var targetPort = template['portBinding'];
                $scope.canvasApi.addConnection(source, sourcePort, target,
                        targetPort, [], 'link');
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
                $scope.canvasApi.onMoveSelected(id, boundingBox.x,
                        boundingBox.y, boundingBox.width, boundingBox.height);
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
         * @param template
         * @param x
         * @param y
         * @return Component model ID in the, key to idToModel.
         */
        var insertComponent = function (template, x, y) {
            var component = pipelineModel.createComponent(
                    $scope.data.model, template['component']);
            comFacade.setPosition(component, x, y);
            var id = $scope.canvasApi.addComponent(component,
                    template['component'], pipelineModel.component);
            comFacade.setIriFromId($scope.data.model, component, id);
            //
            $scope.data.idToModel[id] = component;
            $scope.data.iriToId[component['@id']] = id;
            return id;
        };

        /**
         * Load pipeline name from definition.
         */
        var updateLabel = function () {
            $scope.data.label = pplFacade.getLabel($scope.data.model);
            if (!$scope.data.label || $scope.data.label === '') {
                $scope.data.label = $scope.data.definition['@id'];
            }
        };

        /**
         * Disable mapping and set mapping as unavailable. Also propagate
         * the changes to all follow up components.
         */
        var disableMappingOnChange = function (iri) {
            var mapping = data.execution.mapping[iri];
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
            var connections = pplFacade.getConnections($scope.data.model);
            connections.forEach(function (connection) {
                var source = conFacade.getSource(connection);
                var target = conFacade.getTarget(connection);
                if (source === iri) {
                    disableMappingOnChange(target);
                }
            });
        };

        /**
         * Dissable mapping for the component and propagate changes
         * to all follow up components.
         */
        var disableComponentMapping = function (iri) {
            var mapping = data.execution.mapping[iri];
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
            var connections = pplFacade.getConnections($scope.data.model);
            connections.forEach(function (connection) {
                var source = conFacade.getSource(connection);
                var target = conFacade.getTarget(connection);
                if (source === iri) {
                    disableComponentMapping(target);
                }
            });
        };

        /**
         * Enable component mapping on given component. Also activate
         * mapping on all previous components.
         */
        var enableComponentMapping = function (iri) {
            var mapping = data.execution.mapping[iri];
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
            var connections = pplFacade.getConnections($scope.data.model);
            connections.forEach(function (connection) {
                var source = conFacade.getSource(connection);
                var target = conFacade.getTarget(connection);
                if (target === iri) {
                    enableComponentMapping(source);
                }
            });
        };

        /**
         * Load pipeline from data.model into the canvas.
         */
        var pipelineToCanvas = function () {
            console.time('pipelineToCanvas');
            $scope.status.noApply = true;
            $scope.canvasApi.loadStart();
            var iriToId = {};
            // We also need to deterine the left top corner.
            var leftTopX = null;
            var leftTopY = null;
            //
            console.time('components');
            var components = pplFacade.getComponents($scope.data.model);
            components.forEach(function (component) {
                var template = templates.getTemplate(
                        comFacade.getTemplateIri(component));
                //
                var id = $scope.canvasApi.addComponent(
                        component, template, pipelineModel.component);
                $scope.data.idToModel[id] = component;
                // Store position.
                var x = comFacade.getX(component);
                var y = comFacade.getY(component);
                if (leftTopX === null) {
                    leftTopX = x;
                    leftTopY = y;
                } else {
                    leftTopX = Math.min(leftTopX, x);
                    leftTopY = Math.min(leftTopY, y);
                }
                //
                iriToId[component['@id']] = id;
            });
            console.timeEnd('components');
            //
            console.time('connections');
            var connections = pipelineModel.getConnections($scope.data.model);
            connections.forEach(function (connection) {
                var vertices = conFacade.getVerticesView(
                        $scope.data.model, connection);
                var id = $scope.canvasApi.addConnection(
                        iriToId[conFacade.getSource(connection)],
                        conFacade.getSourceBinding(connection),
                        iriToId[conFacade.getTarget(connection)],
                        conFacade.getTargetBinding(connection),
                        vertices, 'link');
                $scope.data.idToModel[id] = connection;
            });
            //
            var runAfter = pipelineModel.getRunAfter($scope.data.model);
            runAfter.forEach(function (connection) {
                var vertices = conFacade.getVerticesView(
                        $scope.data.model, connection);
                var id = $scope.canvasApi.addConnection(
                        iriToId[conFacade.getSource(connection)],
                        null,
                        iriToId[conFacade.getTarget(connection)],
                        null, vertices, 'control');
                $scope.data.idToModel[id] = connection;
            });
            console.timeEnd('connections');
            $scope.canvasApi.loadEnd();
            $scope.status.noApply = false;
            // We need to move the screen in posite direction
            // to get to the position. Also substract something to get
            // the component inside the view.
            $scope.canvasApi.setScreen(-(leftTopX - 50), -(leftTopY - 50));
            //
            $scope.data.iriToId = iriToId;
            console.timeEnd('pipelineToCanvas');
        };

        /**
         * Synchronize model with canvas.
         */
        var canvasToPipeline = function () {
            // Set vertices.
            $scope.canvasApi.getConnections().forEach(function (id) {
                var connection = $scope.data.idToModel[id];
                var verticesView = $scope.canvasApi.getVertices(id);
                if (verticesView) {
                    var verticies = [];
                    verticesView.forEach(function (item) {
                        verticies.push(conFacade.createVertex(
                                item['x'], item['y']));
                    });
                    conFacade.setVertices($scope.data.model, connection,
                            verticies);
                } else {
                    conFacade.setVertices($scope.data.model, connection, []);
                }
            });
        };

        var loadPipeline = function (onSucess) {
            $http.get($scope.data.iri).then(function (response) {
                $scope.data.model = pplFacade.fromJsonLd(response.data);
                $scope.data.definition = pplFacade.getPipeline($scope.data.model);
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

        var storePipeline = function (iri, unchecked, onSucess) {
            //
            canvasToPipeline();
            //
            var jsonld = pipelineModel.toJsonLd($scope.data.model);
            $http({
                'method': 'PUT',
                'url': iri,
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
         * Create and return unique componnet id, that is not presented in
         * $scope.data.idToModel.
         */
        var uuid = function () {
            // credit: http://stackoverflow.com/posts/2117523/revisions
            var result = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(
                    /[xy]/g, function (c) {
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
            $http.post('/api/v1/execute?uri=' + $scope.data.iri, configuration)
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

        /**
         * Create a configuration object for executePipeline function.
         */
        var createExecuteConfiguration = function (parametr) {
            var config = {};

            // Run-to arguments.
            if (parametr['to']) {
                config['execute_to'] = parametr['to'];
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
                    // Make sure we should (and can) use mapping
                    // and if so use it.
                    if (item.available && item.enabled && !item.changed) {
                        components[uri] = item['target'];
                    }
                }
                //
                if (data.execution.id !== null) {
                    config['mapping'] = {
                        'execution': data.execution.iri,
                        'components': components
                    };
                }
            }
            return config;
        };

        /**
         * Set visual appeareace for component based on given mapping.
         */
        var setMappingVisual = function (mapping) {
            switch (mapping['status']) {
                case 'http://etl.linkedpipes.com/resources/status/queued':
                case 'http://etl.linkedpipes.com/resources/status/initializing':
                case 'http://etl.linkedpipes.com/resources/status/running':
                    break;
                case 'http://etl.linkedpipes.com/resources/status/finished':
                    if (mapping.enabled) {
                        $scope.canvasApi.updateComponentVisual(mapping['viewId'],
                                {'stroke': {'color': '#388E3C', 'width': 5}});
                    } else {
                        $scope.canvasApi.updateComponentVisual(mapping['viewId'],
                                {'stroke': {'color': 'gray', 'width': 3}});
                    }
                    break;
                case 'http://etl.linkedpipes.com/resources/status/mapped':
                    if (mapping.enabled) {
                        $scope.canvasApi.updateComponentVisual(mapping['viewId'],
                                {'stroke': {'color': '#00796B', 'width': 5}});
                    } else {
                        $scope.canvasApi.updateComponentVisual(mapping['viewId'],
                                {'stroke': {'color': 'gray', 'width': 3}});
                    }
                    break;
                case 'http://etl.linkedpipes.com/resources/status/failed':
                    $scope.canvasApi.updateComponentVisual(mapping['viewId'],
                            {'stroke': {'color': 'red', 'width': 5}});
                    break;
                default:
                    console.error('Unknwon status:', mapping['status']);
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
                //
                data.execution = {
                    'iri': $routeParams.execution,
                    'mapping': {}
                };
                //
                jsonld.iterateObjects(response.data, function (resource, graph) {
                    if (resource['@type'].indexOf('http://linkedpipes.com/ontology/Component') === -1) {
                        return;
                    }
                    var iri = resource['@id'];
                    var view = $scope.data.iriToId[iri];
                    if (typeof (view) === 'undefined') {
                        return;
                    }
                    var status = jsonld.getReference(resource,
                            'http://etl.linkedpipes.com/ontology/status');
                    var available = status === 'http://etl.linkedpipes.com/resources/status/finished' ||
                            status === 'http://etl.linkedpipes.com/resources/status/mapped';
                    var mapping = {
                        'viewId': view,
                        'target': iri,
                        'status': status,
                        'available': available,
                        'enabled': true,
                        'changed': false
                    };
                    //
                    setMappingVisual(mapping);
                    data.execution.mapping[iri] = mapping;
                });
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
            var templateIri = comFacade.getTemplateIri(component);
            var template = templates.getTemplate(templateIri);
            //
            templates.fetchTemplateConfiguration(template, function () {
                $scope.status.dialogOpened = true;
                $mdDialog.show({
                    'controller': 'components.pipelines.configuration.dialog',
                    'templateUrl': 'app/components/pipelines/configurationDialog/configurationDialogView.html',
                    'parent': angular.element(document.body),
                    'clickOutsideToClose': false,
                    'fullscreen': true,
                    'locals': {
                        'component': component,
                        'template': template,
                        'data': $scope.data
                    }
                }).then(function () {
                    // Invalidate mapping if presented as we want to
                    // disable mapping, if a component has changed.
                    disableMappingOnChange(component['@id']);
                    // Update component.
                    $scope.canvasApi.updateComponent(id, component, template,
                            comFacade);
                    // Update size and position of the selection menu.
                    var boundingBox = $scope.canvasApi.getScreenBoundingBox(id);
                    $scope.canvasApi.onMoveSelected(id,
                            boundingBox.x, boundingBox.y,
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
            var component = pplFacade.cloneComponent($scope.data.model, model, uuid());
            //
            var templateIri = comFacade.getTemplateIri(model);
            var template = templates.getTemplate(templateIri);
            //
            var id = $scope.canvasApi.addComponent(
                    component, template, comFacade);
            $scope.data.idToModel[id] = component;
        };

        $scope.onDebugToComponent = function () {
            var component = $scope.data.idToModel[$scope.status.selected];
            if (!component) {
                console.log('No component selected!');
                return;
            }
            //
            storePipeline($scope.data.iri, true, function () {
                executePipeline(createExecuteConfiguration({
                    'to': component['@id']}), function () {
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
         */
        $scope.onDetail = function ($event) {
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $scope.status.dialogOpened = true;
            $mdDialog.show({
                'controller': 'components.pipelines.detail.dialog',
                'templateUrl': 'app/components/pipelines/detailDialog/pipelineDetailDialogView.html',
                'parent': angular.element(document.body),
                'targetEvent': $event,
                'clickOutsideToClose': false,
                'fullscreen': useFullScreen,
                'locals': {
                    'data': $scope.data
                }
            }).then(function () {
                // Update component view.
                updateLabel();
                $scope.status.dialogOpened = false;
            }, function () {
                $scope.status.dialogOpened = false;
            });
        };

        $scope.onSave = function () {
            storePipeline($scope.data.iri, true);
        };

        $scope.onSaveAndClose = function () {
            storePipeline($scope.data.iri, true, function () {
                $location.path('/pipelines').search({});
            });
        };

        $scope.onExecute = function () {
            storePipeline($scope.data.iri, true, function () {
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
                    'url': $scope.data.iri
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

        $scope.onExport = function ($event) {
            //
            canvasToPipeline();
            //
            var jsonld = pipelineModel.toJsonLd($scope.data.model);

            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'));
            $scope.status.dialogOpened = true;
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
                        'label': $scope.data.label,
                        'pipeline': jsonld
                    }
                }
            }).then(function (result) {
                $scope.status.dialogOpened = false;
            }, function () {
                $scope.status.dialogOpened = false;
            });
        };

        var initialize = function () {
            templates.load(function () {
                loadPipeline(loadExecution);
            }, function (response) {
                statusService.deleteFailed({
                    'title': "Can't load pipeline.",
                    'response': response
                });
            });
        };

        // Make sure directives are loaded first.
        $timeout(initialize, 0);
    }

    controller.$inject = ['$scope', '$http', '$location', '$routeParams',
        '$timeout', '$mdDialog', '$mdMedia',
        'components.pipelines.services.model',
        'components.templates.services.repository', 'services.status',
        'services.jsonld'];

    return function init(app) {
        pipelineDetailDialog(app);
        pipelineCanvasDirective(app);
        pipelineServiceModule(app);
        templatesRepositoryModule(app);
        configurationDialogCtrlModule(app);
        selectTemplateDialogModule(app);

        app.controller('components.pipelines.edit.canvas', controller);
    };

});
