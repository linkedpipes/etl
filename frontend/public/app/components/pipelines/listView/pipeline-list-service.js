((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "angular",
            "./pipeline-list-repository"
        ], definition);
    }
})((vocabulary, angular, _repository) => {

    // TODO Register on destroy to delete data.

    const LP = vocabulary.LP;

    function factory($http, $location, $mdDialog, $statusService, $clipboard,
                     repository) {

        console.log("pipeline-list-service : CTOR");

        let $scope;

        function initialize(scope) {
            $scope = scope;
            $scope.isClipboardSupported = $clipboard.supported;
            $scope.filter = {
                "labelSearch": "",
                "tagsSearch": [],
                "tagsSearchString": "",
                "tagsAll": []
            };
            $scope.repository = repository.create($scope.filter);
        }

        // TODO Move to "execution-api" module.

        function executePipeline(pipeline) {
            const config = createExecutionConfiguration(true, false);
            postNewExecution(pipeline, config, $http);
        }

        function createExecutionConfiguration(saveDebug, deleteWorking) {
            const configuration = {
                "@id": "",
                "@type": LP.EXEC_OPTIONS,
            };
            configuration[LP.SAVE_DEBUG] = saveDebug;
            configuration[LP.DELETE_WORKING] = deleteWorking;
            return configuration;
        }

        function postNewExecution(pipeline, config, $http) {
            const url = "/resources/executions?pipeline=" + pipeline.iri;
            $http.post(url, config)
            .then(redirectToExecutionList)
            .catch(handleExecutionPostFailure);
        }

        // TODO Move to "navigation" module.

        function redirectToExecutionList() {
            $location.path("/executions").search({});
        }

        function handleExecutionPostFailure(response) {
            $statusService.httpPostFailed({
                "title": "Can't start the execution.",
                "response": response
            });
        }

        function executeWithoutDebugData(pipeline) {
            const config = createExecutionConfiguration(false, true);
            postNewExecution(pipeline, config)
        }

        function exportPipeline(pipeline, $event) {
            const useFullScreen = ($mdMedia("sm") || $mdMedia("xs"));
            $mdDialog.show({
                "controller": "components.pipelines.export.dialog",
                "templateUrl": "app/components/pipelines/exportDialog/pipelineExportDialogView.html",
                "parent": angular.element(document.body),
                "targetEvent": $event,
                "clickOutsideToClose": false,
                "fullscreen": useFullScreen,
                "locals": {
                    "data": {
                        "iri": pipeline.iri,
                        "label": pipeline.label
                    }
                }
            });
        }

        // TODO Move to "pipeline-api" module.

        function createPipeline() {
            const data = createPipelineCreatePostData();
            const config = createPostConfigWithJsonLd();
            const url = "/resources/pipelines/";
            $http.post(url, data, config)
            .then((response) => {
                // TODO Use JSONLD
                const iri = response.data[0]["@graph"][0]["@id"];
                redirectToPipelineDetail(iri);
            })
            .catch(handleCreatePipelineFailure);
        }

        function createPipelineCreatePostData() {
            const data = new FormData();
            const options = createEmptyPipelineCreateOptions();
            addOptionsToData(data, options);
            return data;
        }

        function createEmptyPipelineCreateOptions() {
            return {
                "@id": "http://localhost/options",
                "@type": "http://linkedpipes.com/ontology/UpdateOptions"
            };
        }

        function addOptionsToData(data, options) {
            data.append("options", new Blob([JSON.stringify(options)], {
                "type": "application/ld+json"
            }), "options.jsonld");
        }

        function createPostConfigWithJsonLd() {
            return {
                "transformRequest": angular.identity,
                "headers": {
                    // By this angular add Content-Type itself.
                    "Content-Type": undefined,
                    "accept": "application/ld+json"
                }
            };
        }

        // TODO Move to "navigation" module.

        function redirectToPipelineDetail(pipelineIri) {
            $location.path("/pipelines/edit/canvas").search({
                "pipeline": pipelineIri
            });
        }

        function handleCreatePipelineFailure(response) {
            $statusService.httpPostFailed({
                "title": "Can't create the pipeline.",
                "response": response
            });
        }

        // TODO Move to "navigation" module.

        function redirectToPipelineUpload() {
            $location.path("/pipelines/upload").search({});
        }

        function copyPipelineIriToClipboard(pipeline) {
            $clipboard.copyText(pipeline.iri)
        }

        // TODO Move to "pipeline-api" module.

        function copyPipeline(pipeline) {
            const data = createPipelineCopyPostData();
            const config = createPostConfigWithJsonLd();
            const url = "/resources/pipelines?pipeline=" + pipeline.iri;
            $http.post(url, data, config)
            .then(handleCopyPipelineSuccess)
            .catch(reportCopyPipelineFailure);
        }

        function createPipelineCopyPostData() {
            const data = new FormData();
            const options = createCopyPipelineOptions();
            addOptionsToData(data, options);
            return data;
        }

        function createCopyPipelineOptions() {
            return {
                "@id": "http://localhost/options",
                "@type": "http://linkedpipes.com/ontology/UpdateOptions",
                "http://etl.linkedpipes.com/ontology/local": true
            };
        }

        function handleCopyPipelineSuccess() {
            $statusService.success({
                "title": "Pipeline has been successfully copied."
            });
            repository.update($scope.repository);
        }

        function reportCopyPipelineFailure(response) {
            $statusService.httpPostFailed({
                "title": "Can't create the pipeline.",
                "response": response
            });
        }

        // TODO Move to "pipeline-api" module.

        function deletePipeline(pipeline, event) {
            const dialogText = "Would you like to delete pipeline '"
                + pipeline.label + "'?";

            const confirmDialog =
                $mdDialog.confirm()
                .title(dialogText)
                .ariaLabel("Delete pipeline.")
                .targetEvent(event)
                .ok("Delete pipeline")
                .cancel("Cancel");

            $mdDialog.show(confirmDialog).then(() => {
                repository.delete(pipeline, $scope.repository);
            });
        }

        function onChipsFilterChange() {
            repository.onFilterChanged($scope.repository, "chips");
        }

        function onSearchStringChange() {
            repository.onFilterChanged($scope.repository, "label");
        }

        function loadPipelines() {
            repository.load($scope.repository);
        }

        function getTagsMatchingQuery(query) {
            query = query.toLowerCase();
            return $scope.filter.tagsAll.filter((item) => {
                return item.toLowerCase().indexOf(query) !== -1;
            });
        }

        function increaseVisibleItemLimit() {
            repository.increaseVisibleItemLimit($scope.repository);
        }

        return {
            "initialize": initialize,
            "execute": executePipeline,
            "executeWithoutDebugData": executeWithoutDebugData,
            "export": exportPipeline,
            "create": createPipeline,
            "redirectToUpload": redirectToPipelineUpload,
            "copy": copyPipeline,
            "copyIri": copyPipelineIriToClipboard,
            "delete": deletePipeline,
            "onChipsFilterChange": onChipsFilterChange,
            "onSearchStringChange": onSearchStringChange,
            "load": loadPipelines,
            "getTagsMatchingQuery": getTagsMatchingQuery,
            "increaseVisibleItemLimit": increaseVisibleItemLimit
        };
    }

    factory.$inject = [
        "$http",
        "$location",
        "$mdDialog",
        "services.status",
        "clipboard",
        "pipeline.list.repository"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _repository(app);
        app.factory("pipeline.list.service", factory);
    }
});