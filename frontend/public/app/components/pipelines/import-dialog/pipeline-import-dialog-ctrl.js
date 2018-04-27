((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "jsonld",
            "./pipeline-import-dialog-service"
        ], definition);
    }
})((jsonld, _pipelineDialogService) => {
    "use strict";

    function controller($scope, $mdDialog, $lpScrollWatch, service) {

        service.initialize($scope);

        $scope.$watch("pipelineFilter.labelSearch",
            service.onSearchStringChange);

        $scope.onPipelineTab = service.onPipelineTab;

        $scope.onImport = () => service.onImport($mdDialog);

        $scope.onCancel = () => service.onCancel($mdDialog);

        let callbackReference = null;

        callbackReference = $lpScrollWatch.registerCallback((byButton) => {
            service.increaseVisibleItemLimit();
            if (!byButton) {
                // This event come outside of Angular scope.
                $scope.$apply();
            }
        });

        $scope.$on("$destroy", () => {
            $lpScrollWatch.unRegisterCallback(callbackReference);
        });

        function initialize() {
            $lpScrollWatch.updateReference();
        }

        angular.element(initialize);

    }

    controller.$inject = [
        "$scope",
        "$mdDialog",
        "$lpScrollWatch",
        "pipeline.import.dialog.service"
    ];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _pipelineDialogService(app);
        app.controller("components.pipelines.import.dialog", controller);
    }

});
