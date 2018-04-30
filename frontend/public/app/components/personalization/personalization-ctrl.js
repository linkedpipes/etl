((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "angular",
            "./personalization-service"
        ], definition);
    }
})((angular, _personalizationService) => {
    "use strict";

    function controller($scope, service) {

        $scope.onDiscard = service.load;

        $scope.onSave = service.save;

        function initialize() {
            service.initialize($scope);
            service.load();
        }

        angular.element(initialize);
    }

    controller.$inject = ['$scope', 'personalization.service'];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _personalizationService(app);
        app.controller("personalization", controller);
    }

});
