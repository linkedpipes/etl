((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "angular",
            "./execution-list-service"
        ], definition);
    }
})((angular, _executionListService) => {
    "use strict";

    function controller($scope, $lpScrollWatch, service, $refresh) {

        service.initialize($scope);

        $scope.onExecute = service.execute;

        $scope.onCancel = service.cancel;

        $scope.onOpenLogTail = service.openLogTail;

        $scope.onDelete = service.delete;

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
            $refresh.set(service.update);
        }

        angular.element(initialize);
    }

    controller.$inject = [
        "$scope",
        "$lpScrollWatch",
        "execution.list.service",
        "service.refresh"
    ];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _executionListService(app);
        app.controller("components.executions.list", controller);
    }

});
