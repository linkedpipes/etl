((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "angular",
            "jsonld",
            "./pipeline-import-dialog-repository",
            "./../pipeline-api"
        ], definition);
    }
})((angular, jsonld, _repository, pipelineApi) => {
    "use strict";

    function factory($http, repository, $templates, $status) {

        let $scope;

        function initialize(scope) {
            $scope = scope;
            $scope.url = "";
            $scope.file = undefined;
            $scope.pipeline = "";
            $scope.pipelineLoaded = false;
            $scope.pipelineFilter = {
                "labelSearch": ""
            };
            $scope.activeTab = 0;
            $scope.importing = false;
            $scope.updateTemplates = false;
            $scope.repository = repository.create($scope.pipelineFilter);
        }

        function onImport($mdDialog) {
            let promise;
            switch ($scope.activeTab) {
                case 0:
                    $scope.importing = true;
                    promise = loadFromIri();
                    break;
                case 1:
                    $scope.importing = true;
                    promise = importFile();
                    break;
                case 2:
                    $scope.importing = true;
                    promise = importLocalPipeline();
                    break;
                default:
                    console.error("Invalid active tab: ", $scope.activeTab);
                    return;
            }
            promise.then((pipeline) => {
                $mdDialog.hide({"pipeline": pipeline});
            }).catch((error) => {
                $mdDialog.cancel();
                $status.httpError("Can't import the pipeline.", error);
            });
        }

        function loadFromIri() {
            return pipelineApi.asLocalFromIri(
                $http, $scope.url, $scope.updateTemplates)
                .then((pipeline) => {
                    // Update templates.
                    return new Promise((resolve, reject) => {
                        $templates.forceLoad()
                            .then(() => resolve(pipeline))
                            .catch(reject);
                    });
                });
        }

        function importFile() {
            return pipelineApi.asLocalFromFile(
                $http, $scope.url, $scope.updateTemplates)
                .then((pipeline) => {
                    // Update templates.
                    return new Promise((resolve, reject) => {
                        $templates.forceLoad()
                            .then(() => resolve(pipeline))
                            .catch(reject);
                    });
                });
        }

        function importLocalPipeline() {
            // Import from IRI on local machine.
            if ($scope.pipeline === undefined || $scope.pipeline === "") {
                // Do nothing as no pipeline is selected.
                $status.error("No pipeline selected for import.");
            } else {
                return pipelineApi.loadLocal($http, $scope.pipeline);
            }
        }

        function onPipelineTab() {
            if ($scope.pipelineLoaded) {
                return;
            } else {
                repository.load($scope.repository).then(() => {
                    $scope.pipelineLoaded = true;
                }).catch(angular.noop).then(() => $scope.$apply());
            }
        }

        /**
         * Check on pipeline change, make sure that only one pipeline
         * can be selected at a time.
         */
        function onPipelineClick(item) {
            if (item.selected) {
                $scope.repository.data.forEach(function (item) {
                    item.selected = false;
                });
                $scope.pipeline = item;
            } else {
                $scope.pipeline = undefined;
            }
            item.selected = true;
        }

        function onCancel($mdDialog) {
            $mdDialog.cancel();
        }

        function onSearchStringChange() {
            repository.onFilterChanged($scope.repository, $scope.pipeline);
        }

        function increaseVisibleItemLimit() {
            repository.increaseVisibleItemLimit($scope.repository);
        }

        return {
            "initialize": initialize,
            "onImport": onImport,
            "onPipelineTab": onPipelineTab,
            "onPipelineClick": onPipelineClick,
            "onCancel": onCancel,
            "onSearchStringChange": onSearchStringChange,
            "increaseVisibleItemLimit": increaseVisibleItemLimit
        };
    }

    factory.$inject = [
        "$http",
        "pipeline.import.dialog.repository",
        "template.service",
        "services.status"
    ];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _repository(app);
        app.factory("pipeline.import.dialog.service", factory);
    }
});