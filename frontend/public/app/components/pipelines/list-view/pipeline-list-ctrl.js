((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "angular",
            "./pipeline-list-service"
        ], definition);
    }
})((angular, _pipelineListService) => {
    "use strict";

    function controller($scope, $mdMedia, $lpScrollWatch, service) {

        service.initialize($scope, $mdMedia);

        $scope.getTagsMatchingQuery = service.getTagsMatchingQuery;

        $scope.onExecute = service.execute;

        $scope.onExecuteWithoutDebugData = service.executeWithoutDebugData;

        $scope.onExport = service.export;

        $scope.onCreate = service.create;

        $scope.onUpload = service.redirectToUpload;

        $scope.onCopy = service.copy;

        $scope.onCopyIri = service.copyIri;

        $scope.onDelete = service.delete;

        $scope.chipsFilter = service.onChipsFilterChange;

        $scope.$watch("filter.labelSearch", service.onSearchStringChange);

        $scope.noAction = () => {
            // This is do nothing action, we need it else the menu is open
            // on click to item. This cause menu to open which together
            // with navigation break the application.
        };

        let callbackReference = null;

        callbackReference = $lpScrollWatch.registerCallback(
            service.increaseVisibleItemLimit);

        $scope.$on("$destroy", () => {
            $lpScrollWatch.unRegisterCallback(callbackReference);
        });

        function initialize() {
            $lpScrollWatch.updateReference();
            service.load();
        }

        angular.element(initialize);
    }

    controller.$inject = [
        "$scope",
        "$mdMedia",
        "$lpScrollWatch",
        "pipeline.list.service"
    ];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _pipelineListService(app);
        app.controller("components.pipelines.list", controller);
    }

});
