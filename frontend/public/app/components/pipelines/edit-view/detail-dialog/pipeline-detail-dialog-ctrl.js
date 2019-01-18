((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "angular",
            "./pipeline-detail-dialog-service",
            "app/services/designService"
        ], definition);
    }
})((angular, _service, _designService) => {
    "use strict";

    function controller($scope, $mdDialog, service, pipeline) {

        $scope.onCancel = () => service.cancel($mdDialog);
        $scope.onSave = () => service.save($mdDialog);

        $scope.$on("$routeChangeStart", function($event, next, current) {
            $mdDialog.cancel();
        });

        service.initialize($scope, pipeline);
    }

    controller.$inject = [
        "$scope", "$mdDialog", "pipeline.detail.dialog.service", "data"
    ];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _service(app);
        _designService(app);
        app.controller("components.pipelines.detail.dialog", controller);
    }

});
