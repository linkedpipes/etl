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
            'components': []
        };

        var decorator = function (component) {
            // Prepare paths in data units.
            var exec = $routeParams.execution;
            var ftpPath = infoService.get().path.ftp + '/' +
                    exec.substring(exec.lastIndexOf('executions/') + 11) + '/';
            component.dataUnits.forEach(function (dataUnit) {
                dataUnit.ftp = ftpPath + dataUnit.debug;
            });
            // Convert times.
            component.startTime = Date.parse(component.start);
            if (component.end) {
                component.endTime = Date.parse(component.end);
            }
            // Get label.
            component.label = component.iri;
            // Compute duration.
            if (component.endTime) {
                var duration = (component.endTime - component.startTime) / 1000;
                var seconds = Math.ceil((duration) % 60);
                var minutes = Math.floor((duration / (60)) % 60);
                var hours = Math.floor(duration / (60 * 60));
                component.duration = (hours < 10 ? '0' + hours : hours) +
                        ':' + (minutes < 10 ? '0' + minutes : minutes) +
                        ':' + (seconds < 10 ? '0' + seconds : seconds);
            } else {
                component.duration = '';
            }
            //
//            if (component.progress) {
//                component.progress.value = 100 *
//                        (component.progress.current / component.progress.total);
//            }
            // Determine detail and icon type.
            switch (component.status) {
                case 'http://etl.linkedpipes.com/resources/status/initializing':
                case 'http://etl.linkedpipes.com/resources/status/running':
                    component.detailType = 'RUNNING';
                    component.icon = {
                        'name': 'run',
                        'style': {
                            'color': 'blue'
                        }
                    };
                    break;
                case 'http://etl.linkedpipes.com/resources/status/finished':
                    component.detailType = 'FINISHED';
                    component.icon = {
                        'name': 'done',
                        'style': {
                            'color': 'green'
                        }
                    };
                    break;
                case 'http://etl.linkedpipes.com/resources/status/failed':
                    component.detailType = 'FAILED';
                    component.icon = {
                        'name': 'error',
                        'style': {
                            'color': 'red'
                        }
                    };
                    break;
                default:
                    component.detailType = 'NONE';
                    break;
            }
        };

        var loadPipeline = function (iri, onSuccess) {
            if (status.pipelineLoading) {
                return;
            }
            $http.get(iri).then(function (response) {

                console.time('Loading pipeline');

                var labels = {};

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
                                labels[resource['@id']] = jsonld.getString(resource,
                                        'http://www.w3.org/2004/02/skos/core#prefLabel');
                                break;
                        }
                    }
                });

                console.timeEnd('Loading pipeline');

                $scope.data.pipeline = {
                    'labels': labels
                };
                status.pipelineLoading = false;
                status.pipelineLoaded = true;
                if (onSuccess) {
                    onSuccess();
                }
            });
        };

        var bindLabels = function () {
            if (status.labelsLoading || !status.pipelineLoaded) {
                return;
            }

            console.time('Loading labels');

            $scope.data.components.forEach(function (component) {
                component.labels = $scope.data.pipeline.labels[component.iri];
                if (component.labels) {
                    if (jQuery.isPlainObject(component.labels)) {
                        if (component.labels['en']) {
                            component.label = component.labels['en'];
                        } else if (component.labels['']) {
                            component.label = component.labels[''];
                        } else {
                            // TODO Use any other.
                        }
                    } else {
                        component.label = component.labels;
                    }
                }
            });

            console.timeEnd('Loading labels');

            status.labelsLoading = false;
        };

        var loadExecution = function () {

            $http.get($routeParams.execution).then(function (response) {
                var data = {};
                var components = {};
                var dataUnits = {};

                console.time('Loading execution');

                jsonld.iterateObjects(response.data, function (resource, graph) {
                    var types;
                    if (jQuery.isArray(resource['@type'])) {
                        types = resource['@type'];
                    } else {
                        types = [resource['@type']];
                    }
                    for (var index in types) {
                        switch (types[index]) {
                            case 'http://etl.linkedpipes.com/ontology/Execution':
                                data.status = jsonld.getReference(resource,
                                        'http://etl.linkedpipes.com/ontology/status');
                                data.pipeline = jsonld.getReference(resource,
                                        'http://etl.linkedpipes.com/ontology/pipeline');
                                if (!$scope.data.pipeline) {
                                    loadPipeline(resource['@id'] + '/pipeline', bindLabels);
                                }
                                break;
                            case 'http://linkedpipes.com/ontology/events/ExecutionBegin':
                                data.start = jsonld.getString(resource,
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
                                component.order = jsonld.getInteger(resource,
                                        'http://linkedpipes.com/ontology/order');
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
                                data.end = jsonld.getString(resource,
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

                var componentsArray = [];
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
                    // Call decorator.
                    decorator(component);
                    // Store into an array.
                    componentsArray.push(component);
                }

                console.timeEnd('Loading execution');

                // Set data.
                $scope.data.components = componentsArray;
                $scope.status.loaded = true;

                console.log('pipeline   : ', data);
                console.log('components : ', $scope.data.components);

                // InitializationFailed
                // ExecutionCancelled
                // ComponentFailed

                bindLabels();
            });
        };

        infoService.wait(loadExecution);

    }

    controler.$inject = ['$scope', '$http', '$routeParams', '$mdDialog',
        '$mdMedia', 'service.refresh', 'services.jsonld', 'service.info'];

    return function init(app) {
        app.controller('components.executions.detail', controler);
    };

});