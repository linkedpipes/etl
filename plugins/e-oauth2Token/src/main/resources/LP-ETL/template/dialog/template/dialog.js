define([], function () {
    "use strict";

    function controller($scope) {

        if ($scope.dialog === undefined) {
            $scope.dialog = {};
        }

    }

    controller.$inject = ['$scope'];
    return controller;
});
