define([], function () {
    "use-prefix";

    function controller($scope) {

        // Define the control object on shared scope.
        if ($scope.control == undefined) {
            $scope.control = {}
        }

        // Here we just use the scope to show the values from template.

    }

    controller.$inject = ['$scope'];
    return controller;
});
