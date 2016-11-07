define([
    'jquery',
    'angular'
], function (jQuery, angular) {
    function controler($scope, $http, $routeParams, $mdDialog, $mdMedia,
            refreshService, jsonldService, infoService) {

        var jsonld = jsonldService.jsonld();

        var status = {
            'pipelineLoading': false,
            'pipelineLoaded': false,
            'labelsLoading': false
        };

        $scope.status = {
            'loaded': false
        };

        $scope.data = {
            'components': [],
            'componentsMap': {},
            'pipeline': null
        };

        var global = {};

        /**
         * Update changing properties in target from source.
         */
        var updateComponent = function (target, source) {
            // Store time - use direct value, so we don't have to solve
            // time zone issues.
            target.startTime = source.start;
            // Compute duration.
            if (target.endTime) {
                var startTime = Date.parse(source.start);
                var endTime = Date.parse(source.end);
                var duration = (endTime - startTime) / 1000;
                var seconds = Math.ceil((duration) % 60);
                var minutes = Math.floor((duration / (60)) % 60);
                var hours = Math.floor(duration / (60 * 60));
                target.duration = (hours < 10 ? '0' + hours : hours) +
                        ':' + (minutes < 10 ? '0' + minutes : minutes) +
                        ':' + (seconds < 10 ? '0' + seconds : seconds);
            } else {
                target.duration = '';
            }
            //
            if (typeof (source.current) !== undefined) {
                target.progress = 100 * (source.current / source.total);
            }
            //
            // Determine detail and icon type.
            switch (source.status) {
                case 'http://etl.linkedpipes.com/resources/status/mapped':
                    target.detailType = 'MAPPED';
                    target.icon = {
                        'name': 'done',
                        'style': {
                            'color': 'blue'
                        }
                    };
                    break;
                case 'http://etl.linkedpipes.com/resources/status/initializing':
                case 'http://etl.linkedpipes.com/resources/status/running':
                    target.detailType = 'RUNNING';
                    target.icon = {
                        'name': 'run',
                        'style': {
                            'color': 'blue'
                        }
                    };
                    break;
                case 'http://etl.linkedpipes.com/resources/status/finished':
                    target.detailType = 'FINISHED';
                    target.icon = {
                        'name': 'done',
                        'style': {
                            'color': 'green'
                        }
                    };
                    break;
                case 'http://etl.linkedpipes.com/resources/status/failed':
                    target.detailType = 'FAILED';
                    target.icon = {
                        'name': 'error',
                        'style': {
                            'color': 'red'
                        }
                    };
                    break;
                default:
                    target.detailType = 'NONE';
                    break;
            }
        };

        /**
         * Compute aditional properties for given component.
         */
        var decorateComponent = function (component) {
            // Prepare paths in data units.
            var exec = $routeParams.execution;
            var ftpPath = global.info.path.ftp + '/' +
                    exec.substring(exec.lastIndexOf('executions/') + 11) + '/';
            component.dataUnits.forEach(function (dataUnit) {
                dataUnit.ftp = ftpPath + dataUnit.debug;
            });
            // Get label.
            component.label = component.iri;
            //
            updateComponent(component, component);
        };

        /**
         * Load pipeline info and execute onSuccess callback when
         * the pipeline is loaded. If called multiple times other
         * calls are ignored.
         */
        var loadPipeline = function (iri, onSuccess) {
            if (status.pipelineLoading) {
                return;
            } else {
                status.pipelineLoading = true;
            }
            $http.get(iri).then(function (response) {
                var components = {};
                jsonld.iterateObjects(response.data, function (resource, graph) {
                    var types;
                    if (jQuery.isArray(resource['@type'])) {
                        types = resource['@type'];
                    } else {
                        types = [resource['@type']];
                    }
                    for (var index in types) {
                        switch (types[index]) {
                            case 'http://linkedpipes.com/ontology/Component':
                                var component = {};
                                component['label'] = jsonld.getString(resource,
                                        'http://www.w3.org/2004/02/skos/core#prefLabel');
                                component['description'] = jsonld.getString(resource,
                                        'http://purl.org/dc/terms/description');
                                components[resource['@id']] = component;
                                break;
                        }
                    }
                });
                $scope.data.pipeline = components;
                status.pipelineLoading = false;
                status.pipelineLoaded = true;
                if (onSuccess) {
                    onSuccess();
                }
            });
        };

        var selectString = function (value) {
            if (jQuery.isPlainObject(value)) {
                if (value['en']) {
                    return value['en'];
                } else if (value['']) {
                    return value[''];
                } else {
                    // TODO Use any other.
                }
            } else {
                return value;
            }
        };

        /**
         * Bind labels from the $scope.data.pipeline to the
         * $scope.data.components.
         */
        var bindLabels = function () {
            if (status.labelsLoading || !status.pipelineLoaded) {
                return;
            }
            $scope.data.components.forEach(function (component) {
                var componentInfo = $scope.data.pipeline[component.iri];
                component.label = selectString(componentInfo['label']);
                component.description = selectString(componentInfo['description']);

                if (typeof(component.description) !== 'undefined') {
                    if (component.description.length > 120) {
                        component.description = ' - ' +
                                component.description.substring(0, 116) + ' ...';
                    } else {
                        component.description = ' - ' + component.description;
                    }
                }
            });
            status.labelsLoading = false;
        };

        var parseData = function (executionJsonld) {
            console.time('Parse data');

            var pipeline = {};
            var components = {};
            var dataUnits = {};

            jsonld.iterateObjects(executionJsonld, function (resource, graph) {
                var types;
                if (jQuery.isArray(resource['@type'])) {
                    types = resource['@type'];
                } else {
                    types = [resource['@type']];
                }
                for (var index in types) {
                    switch (types[index]) {
                        case 'http://etl.linkedpipes.com/ontology/Execution':
                            pipeline.status = jsonld.getReference(resource,
                                    'http://etl.linkedpipes.com/ontology/status');
                            pipeline.pipeline = jsonld.getReference(resource,
                                    'http://etl.linkedpipes.com/ontology/pipeline');
                            if ($scope.data.pipeline === null) {
                                loadPipeline(resource['@id'] + '/pipeline', bindLabels);
                            }
                            break;
                        case 'http://linkedpipes.com/ontology/events/ExecutionBegin':
                            pipeline.start = jsonld.getString(resource,
                                    'http://linkedpipes.com/ontology/events/created');
                            break;
                        case 'http://linkedpipes.com/ontology/events/ComponentBegin':
                            var iri = jsonld.getReference(resource,
                                    'http://linkedpipes.com/ontology/component');
                            if (!components[iri]) {
                                components[iri] = {
                                    'iri': iri
                                };
                            }
                            var component = components[iri];
                            //
                            component.start = jsonld.getString(resource,
                                    'http://linkedpipes.com/ontology/events/created');
                            break;
                        case 'http://linkedpipes.com/ontology/events/ComponentEnd':
                            var iri = jsonld.getReference(resource,
                                    'http://linkedpipes.com/ontology/component');
                            if (!components[iri]) {
                                components[iri] = {
                                    'iri': iri
                                };
                            }
                            var component = components[iri];
                            //
                            component.end = jsonld.getString(resource,
                                    'http://linkedpipes.com/ontology/events/created');
                            break;
                        case 'http://linkedpipes.com/ontology/events/ComponentFailed':
                            var iri = jsonld.getReference(resource,
                                    'http://linkedpipes.com/ontology/component');
                            if (!components[iri]) {
                                components[iri] = {
                                    'iri': iri
                                };
                            }
                            var component = components[iri];
                            //
                            component.exception = jsonld.getString(resource,
                                    'http://linkedpipes.com/ontology/events/rootException');
                            component.end = jsonld.getString(resource,
                                    'http://linkedpipes.com/ontology/events/created');
                            break;
                        case 'http://linkedpipes.com/ontology/events/ExecutionEnd':
                            pipeline.end = jsonld.getString(resource,
                                    'http://linkedpipes.com/ontology/events/created');
                            break;
                        case 'http://linkedpipes.com/ontology/Component':
                            var iri = resource['@id'];
                            if (!components[iri]) {
                                components[iri] = {
                                    'iri': iri
                                };
                            }
                            var component = components[iri];
                            //
                            component.status = jsonld.getReference(resource,
                                    'http://etl.linkedpipes.com/ontology/status');
                            component.order = jsonld.getInteger(resource,
                                    'http://linkedpipes.com/ontology/executionOrder');
                            component.dataUnits = jsonld.getReferenceAll(
                                    resource, 'http://etl.linkedpipes.com/ontology/dataUnit');
                            break;
                        case 'http://etl.linkedpipes.com/ontology/DataUnit':
                            var dataunit = {
                                'iri': resource['@id'],
                                'binding': jsonld.getString(resource,
                                        'http://etl.linkedpipes.com/ontology/binding'),
                                'debug': jsonld.getString(resource,
                                        'http://etl.linkedpipes.com/ontology/debug')
                            };
                            // 005 - > ftp://localhost:2221/e4a9640d-f032-40ef-b7ab-14bc28e55f7c/005/
                            dataUnits[dataunit.iri] = dataunit;
                            break;
                    }
                }
            });

            for (var iri in components) {
                var component = components[iri];
                // Filter components.
                if (component.status === 'http://etl.linkedpipes.com/resources/status/queued') {
                    continue;
                }
                // Bind data units.
                for (var index in component.dataUnits) {
                    component.dataUnits[index] =
                            dataUnits[component.dataUnits[index]];
                }
            }

            console.timeEnd('Parse data');
            return {
                'components': components,
                'pipeline': pipeline
            };
        };

        var loadExecution = function () {
            $http.get($routeParams.execution).then(function (response) {
                var data = parseData(response.data);
                var componentsMap = {};
                var componentsArray = [];
                for (var iri in data.components) {
                    var component = data.components[iri];
                    if (component.status === 'http://etl.linkedpipes.com/resources/status/queued') {
                        continue;
                    }
                    decorateComponent(component);
                    componentsArray.push(component);
                    componentsMap[iri] = component;
                }
                // Set data.
                $scope.data.components = componentsArray;
                $scope.data.componentsMap = componentsMap;
                $scope.status.loaded = true;
                $scope.data.pipeline = data.pipeline;
                //
                bindLabels();
            });
        };

        var updateExecution = function () {
            if ($scope.data.pipeline.status === 'http://etl.linkedpipes.com/resources/status/finished'
                    || 'http://etl.linkedpipes.com/resources/status/failed') {
                return;
            }
            $http.get($routeParams.execution).then(function (response) {
                var data = parseData(response.data);
                for (var iri in data.components) {
                    var component = data.components[iri];
                    if (component.status === 'http://etl.linkedpipes.com/resources/status/queued') {
                        continue;
                    }
                    if (!$scope.data.componentsMap[iri]) {
                        // New component.
                        decorateComponent(component);
                        $scope.data.components.push(component);
                        $scope.data.componentsMap[iri] = component;
                    } else {
                        // Update existing.
                        updateComponent($scope.data.componentsMap[iri],
                                component);
                    }
                }
                $scope.data.pipeline = data.pipeline;
            });
        };

        var initialize = function () {
            infoService.fetch().then((info) => {
                global.info = info;
                loadExecution();
            });
            refreshService.set(function () {
                updateExecution();
            });
        };

        initialize();

    }

    controler.$inject = ['$scope', '$http', '$routeParams', '$mdDialog',
        '$mdMedia', 'service.refresh', 'services.jsonld', 'service.info'];

    return function init(app) {
        app.controller('components.executions.detail', controler);
    };

});
