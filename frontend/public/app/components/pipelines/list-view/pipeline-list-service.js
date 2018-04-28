((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "angular",
            "./pipeline-list-repository",
            "./../pipeline-api"
        ], definition);
    }
})((vocabulary, angular, _repository, pipelineApi) => {

    function factory($http, $location, $mdDialog, $statusService, $clipboard,
                     repository) {

        let $scope;
        let $mdMedia;

        function initialize(scope, mdMedia) {
            $scope = scope;
            $mdMedia = mdMedia;
            $scope.isClipboardSupported = $clipboard.supported;
            $scope.filter = {
                "labelSearch": "",
                "tagsSearch": [],
                "tagsSearchString": "",
                "tagsAll": []
            };
            $scope.repository = repository.create($scope.filter);
        }

        function executePipeline(pipeline) {
            pipelineApi.execute($http, pipeline)
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
            pipelineApi.executeWithoutDebugData($http, pipeline)
            .then(redirectToExecutionList)
            .catch(handleExecutionPostFailure);
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

        function createPipeline() {
            pipelineApi.create($http)
            .then(redirectToPipelineDetail)
            .catch(handleCreatePipelineFailure);
        }

        // TODO Move to "navigation" module.

        function redirectToPipelineDetail(response) {
            const iri = response.data[0]["@graph"][0]["@id"];
            $location.path("/pipelines/edit/canvas").search({
                "pipeline": iri
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

        function copyPipeline(pipeline) {
            pipelineApi.copy($http, pipeline)
            .then(handleCopyPipelineSuccess)
            .catch(reportCopyPipelineFailure);
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