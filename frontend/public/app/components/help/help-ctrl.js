((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "angular",
            "app/modules/server-info"
        ], definition);
    }
})((angular, $serverInfo) => {
    "use strict";

    function controller($scope) {

        function initialize() {
            $serverInfo.load().then(() => {
                $scope.commit = $serverInfo.getCommit();
            });
        }

        angular.element(initialize);
    }

    controller.$inject = ["$scope"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.controller("help", controller);
    }

});
